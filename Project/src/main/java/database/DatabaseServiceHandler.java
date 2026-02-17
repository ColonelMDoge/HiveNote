package database;

import java.awt.*;
import java.sql.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import logging.LoggerUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import oracle.ucp.jdbc.PoolDataSource;

public class DatabaseServiceHandler {
    private static PoolDataSource poolDataSource = null;
    private final static String DB_USER = System.getenv("DB_USER");
    private final static String DB_PASSWORD = System.getenv("DB_PASSWORD");
    private final static String CONNECT_STRING = System.getenv("CONNECT_STRING");
    private final static String CONN_FACTORY_CLASS_NAME = "oracle.jdbc.replay.OracleConnectionPoolDataSourceImpl";
    private static final Logger logger = LoggerUtil.getLogger(DatabaseServiceHandler.class);

    public PoolDataSource getPoolDataSource() {
        if (poolDataSource == null) {
            poolDataSource = PoolDataSourceFactory.getPoolDataSource();
            try {
                poolDataSource.setConnectionFactoryClassName(CONN_FACTORY_CLASS_NAME);
                poolDataSource.setURL("jdbc:oracle:thin:@" + CONNECT_STRING);
                poolDataSource.setUser(DB_USER);
                poolDataSource.setPassword(DB_PASSWORD);
                poolDataSource.setConnectionPoolName("JDBC_UCP_POOL");
                poolDataSource.setLoginTimeout(5);
                logger.info("PoolDataSource created.");
                return poolDataSource;
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "There was an exception creating the PoolDataSource: ", e);
            }
        }
        return poolDataSource;
    }

    public void testConnection() {
        try (Connection ignored = getPoolDataSource().getConnection()) {
            logger.info("Connected to the HiveNote database.");
        } catch (SQLException ex) {
            logger.severe("Could not connect to database: " + ex.getMessage());
            throw new RuntimeException("Could not connect to the HiveNote database.");
        }
    }

    public HashMap<String, Set<String>> preLoadData() {
        HashMap<String, Set<String>> data = new HashMap<>();
        String statement = """
                SELECT c.COURSE_CODE, t.TAG_NAME
                FROM COURSE c
                LEFT JOIN TAG t ON c.COURSE_ID = t.COURSE_ID
                """;
        try (Connection conn = getPoolDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(statement);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String courseName = rs.getString(1);
                String tagName = rs.getString(2);
                data.computeIfAbsent(courseName, k -> new HashSet<>()).add(tagName);
                if (tagName != null) {
                    data.get(courseName).add(tagName);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "There was an exception preloading data: ", e);
            return null;
        }
        logger.info("Preloading data complete.");
        return data;
    }

    public boolean insertCourse(String course, String name) {
        String statement = """
                INSERT INTO COURSE (COURSE_CODE, COURSE_NAME) VALUES (?, ?)
                """;
        try (Connection conn = poolDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {
            ps.setString(1, course.toUpperCase());
            ps.setString(2, name);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Course " + course + " inserted successfully.");
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "There was an exception inserting a course!", e);
        }
        return false;
    }

    public boolean dropCourse(String course) {
        String statement = """
                DELETE FROM COURSE WHERE COURSE_CODE = ?;
                """;
        try (Connection conn = getPoolDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {
            ps.setString(1, course.toUpperCase());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info(String.format("Dropped course: %s from the database", course.toUpperCase()));
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "There was an exception deleting a course!", e);
        }
        return false;
    }

    public boolean insertTag(String course_code, String tag) {
        String statement = """
                INSERT INTO TAG (TAG_NAME, COURSE_ID)
                SELECT ?, c.COURSE_ID FROM COURSE c
                WHERE c.COURSE_CODE = ?
                """;
        try (Connection conn = getPoolDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {
            ps.setString(1, tag.toUpperCase());
            ps.setString(2, course_code.toUpperCase());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info(String.format("Inserted tag: \"%s\" with course code: \"%s\" into the database.", tag.toUpperCase(), course_code.toUpperCase()));
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "There was an exception inserting a tag!", e);
        }
        return false;
    }

    public boolean dropTag(String course, String tag) {
        String statement = """
                DELETE t FROM TAG t
                JOIN COURSE course ON course.COURSE_ID = t.COURSE_ID
                WHERE t.TAG_NAME = ? AND  course.COURSE_CODE = ?
                """;
        try (Connection conn = getPoolDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {
            ps.setString(1, tag.toUpperCase());
            ps.setString(2, course.toUpperCase());
            ps.executeUpdate();
            logger.info(String.format("Dropped tag: %s from the database", tag.toUpperCase()));
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "There was an exception deleting a tag!", e);
        }
        return false;
    }

    public String retrieveCourseCodeById(long noteID) {
        String statement = """
                SELECT c.COURSE_CODE FROM COURSE c
                JOIN NOTE n ON n.COURSE_ID = c.COURSE_ID
                WHERE n.NOTE_ID = ?
                """;
        try (Connection conn = getPoolDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {
            ps.setLong(1, noteID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
                return null;
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "There was an exception retrieving a course code from note ID!", e);
        }
        return null;
    }
    private void noteTagJunctionLinker(Connection conn, long note_id, long tag_id) throws SQLException {
        String statement = """
                INSERT INTO NOTE_TAG_JUNCTION (NOTE_ID, TAG_ID) VALUES (?,?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(statement)) {
            ps.setLong(1, note_id);
            ps.setLong(2, tag_id);
            ps.executeUpdate();
            logger.info(String.format("Successfully linked note ID %d to tag ID %d", note_id, tag_id));
        }
    }

    private long retrieveTagId(Connection conn, String course, String tag) throws SQLException {
        String statement = """
                SELECT t.TAG_ID FROM TAG t
                JOIN COURSE c ON c.COURSE_ID = t.COURSE_ID
                WHERE t.TAG_NAME = ? AND c.COURSE_CODE = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(statement)) {
            ps.setString(1, tag.toUpperCase());
            ps.setString(2, course.toUpperCase());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                long id = rs.getLong(1);
                logger.info(String.format("Retrieved tag \"%s\" with corresponding course \"%s\" and an ID of %d", tag, course, id));
                rs.close();
                return id;
            }
            rs.close();
            return -1;
        }
    }

    public long uploadToDB(Note note) {
        long note_id = -1;
        String courseStatement = """
               SELECT COURSE_ID FROM COURSE WHERE COURSE_CODE = ?
               """;
        String statement = """
               INSERT INTO NOTE
               (USER_ID, NOTE_TITLE, CREATED_AT, UPDATED_AT, NOTE_SUMMARY, COURSE_ID)
               VALUES (?,?,?,?,?,?)
               """;

        try (Connection conn = getPoolDataSource().getConnection()) {
            long COURSE_ID;
            try (PreparedStatement cPS = conn.prepareStatement(courseStatement)) {
                cPS.setString(1, note.COURSE_CODE());
                ResultSet cRS = cPS.executeQuery();
                cRS.next();
                COURSE_ID = cRS.getLong(1);
                cRS.close();
            }
            try(PreparedStatement ps = conn.prepareStatement(statement, new String[]{"NOTE_ID"})) {
                ps.setString(1, note.USER_ID());
                ps.setString(2, note.NOTE_TITLE());
                ps.setTimestamp(3, Timestamp.from(Instant.now()));
                ps.setTimestamp(4, Timestamp.from(Instant.now()));
                ps.setString(5, note.NOTE_SUMMARY());
                ps.setLong(6, COURSE_ID);
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    note_id = rs.getLong(1);
                    for (String tag : note.TAGS()) {
                        long tag_id = retrieveTagId(conn, note.COURSE_CODE(), tag);
                        noteTagJunctionLinker(conn, note_id, tag_id);
                    }
                }
                rs.close();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "There was an exception inserting a note!", e);
            return -1;
        }
        String attachmentStatement = """
                INSERT INTO ATTACHMENT (NOTE_ID, FILE_NAME, FILE_BLOB)
                VALUES (?,?,?)
                """;
        try (Connection conn = getPoolDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(attachmentStatement)) {
            for (Attachment attachment : note.ATTACHMENTS()) {
                ps.setLong(1, note_id);
                ps.setString(2, attachment.fileName());
                ps.setBytes(3, attachment.data());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "There was an exception inserting an attachment!", e);
            return -1;
        }
        logger.info("Note successfully uploaded to the database.");
        return note_id;
    }

    public List<byte[]> retrieveBlob(long noteID) {
        String statement = """
                SELECT FILE_BLOB FROM ATTACHMENT WHERE NOTE_ID = ?
                """;
        List<byte[]> blobs = new ArrayList<>();
        try (Connection conn = getPoolDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {
            ps.setLong(1, noteID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                blobs.add(rs.getBytes(1));
            }
            rs.close();
            return blobs;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "There was an exception retrieving a blob!", e);
        }
        logger.warning("Blob could not be retrieved from the database.");
        return null;
    }

    public EmbedBuilder retrieveIDByCourseAndTag(String course, String tag) {
        String statement = """
                SELECT DISTINCT n.NOTE_ID, n.NOTE_TITLE
                FROM NOTE n
                JOIN NOTE_TAG_JUNCTION nt ON nt.NOTE_ID = n.NOTE_ID
                JOIN TAG t ON t.TAG_ID = nt.TAG_ID
                WHERE n.COURSE_ID = (
                    SELECT COURSE_ID
                    FROM COURSE
                    WHERE COURSE_CODE = ?
                )
                AND (? IS NULL OR t.TAG_NAME = ?)
                ORDER BY n.NOTE_ID
                """;
        try (Connection conn = getPoolDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {
            ps.setString(1, course);
            if (tag == null) {
                ps.setNull(2, Types.VARCHAR);
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(2, tag);
                ps.setString(3, tag);
            }
            return formEmbedFromResultSet(ps.executeQuery());
        } catch (SQLException e) {
            logger.log(Level.WARNING, "There was an exception retrieving note IDs!", e);
        }
        return null;
    }

    private EmbedBuilder formEmbedFromResultSet(ResultSet rs) throws SQLException {
        EmbedBuilder embed = new EmbedBuilder();
        StringBuilder builder = new StringBuilder();
        while(rs.next()) {
            builder.append("ID: ")
                    .append(rs.getLong(1))
                    .append(". Title: ")
                    .append(rs.getString(2))
                    .append("\n");
        }
        if (builder.toString().isEmpty()) {
            rs.close();
            return embed;
        }
        embed.setTitle("Fetched Notes by ID");
        embed.setColor(new Color(235, 171, 0));
        embed.addField("Fetched IDs:", builder.toString(), true);
        rs.close();
        return embed;
    }

    public Note retrieveByNoteID(long noteID) {
        String statement = """
                SELECT n.USER_ID, n.NOTE_TITLE, n.CREATED_AT, n.UPDATED_AT, n.NOTE_SUMMARY, c.COURSE_CODE, c.COURSE_NAME
                FROM NOTE n
                JOIN COURSE c
                    ON n.course_id = c.course_id
                WHERE n.note_id = ?
                """;
        String sqlStatement = """
                SELECT t.TAG_NAME FROM TAG t
                JOIN NOTE_TAG_JUNCTION nt ON nt.TAG_ID = t.TAG_ID\s
                WHERE nt.NOTE_ID = ? ORDER BY t.TAG_NAME
                """;
        String attachmentStatement = """
                SELECT FILE_NAME, FILE_BLOB FROM ATTACHMENT WHERE NOTE_ID = ?
                """;
        try (Connection conn = getPoolDataSource().getConnection();
             PreparedStatement sql = conn.prepareStatement(sqlStatement);
             PreparedStatement ps = conn.prepareStatement(statement);
             PreparedStatement as = conn.prepareStatement(attachmentStatement)) {

            List<String> tags = new ArrayList<>();
            sql.setLong(1, noteID);
            ResultSet sqlRS = sql.executeQuery();
            while (sqlRS.next()) {
                tags.add(sqlRS.getString(1));
            }

            List<Attachment> blobs = new ArrayList<>();
            as.setLong(1, noteID);
            ResultSet asRS = as.executeQuery();
            while (asRS.next()) {
                blobs.add(new Attachment(asRS.getString(1), asRS.getBytes(2)));
            }
            asRS.close();
            sqlRS.close();

            ps.setLong(1, noteID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Note note = new Note(
                        noteID,
                        rs.getString(1),
                        rs.getString(2),
                        rs.getTimestamp(3).toInstant().atOffset(ZoneOffset.UTC),
                        rs.getTimestamp(4).toInstant().atOffset(ZoneOffset.UTC),
                        rs.getString(5),
                        rs.getString(6),
                        rs.getString(7),
                        blobs,
                        tags
                );
                rs.close();
                return note;
            }
            rs.close();
            return null;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "There was an exception getting a note!", e);
        }
        return null;
    }

    public void updateNote(long noteID, ModalMapping newCourseCode, ModalMapping newAttachments, ModalMapping newTitle, ModalMapping newSummary, ModalMapping newTags) {
        String updateNoteSQL = """
        UPDATE NOTE n
        SET NOTE_TITLE   = COALESCE(?, n.NOTE_TITLE),
            NOTE_SUMMARY = COALESCE(?, n.NOTE_SUMMARY),
            UPDATED_AT   = ?,
            COURSE_ID    = COALESCE(
                               (SELECT c.COURSE_ID
                                FROM COURSE c
                                WHERE c.COURSE_CODE = ?),
                               n.COURSE_ID
                           )
        WHERE n.NOTE_ID = ?
        """;

        String deleteAttachmentsSQL = "DELETE FROM ATTACHMENT WHERE NOTE_ID = ?";
        String insertAttachmentsSQL = """
        INSERT INTO ATTACHMENT (NOTE_ID, FILE_BLOB, FILE_NAME)
        VALUES (?, ?, ?)
        """;

        String deleteTagsSQL = "DELETE FROM NOTE_TAG_JUNCTION WHERE NOTE_ID = ?";

        String insertTagSQL = """
        INSERT INTO NOTE_TAG_JUNCTION (NOTE_ID, TAG_ID)
        SELECT ?, t.TAG_ID
        FROM TAG t
        WHERE t.TAG_NAME = ?
        """;

        try (Connection conn = getPoolDataSource().getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(updateNoteSQL)) {
                try {
                    if (newTitle == null) ps.setNull(1, Types.VARCHAR);
                    else ps.setString(1, newTitle.getAsString());

                    if (newSummary == null) ps.setNull(2, Types.VARCHAR);
                    else ps.setString(2, newSummary.getAsString());

                    ps.setTimestamp(3, Timestamp.from(Instant.now()));

                    if (newCourseCode.getAsStringList().isEmpty()) ps.setNull(4, Types.VARCHAR);
                    else ps.setString(4, newCourseCode.getAsStringList().get(0));

                    ps.setLong(5, noteID);
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
                ps.executeUpdate();
                logger.info("Descriptors successfully updated!");
            }
            if (!newAttachments.getAsAttachmentList().isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(deleteAttachmentsSQL)) {
                    ps.setLong(1, noteID);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(insertAttachmentsSQL)) {
                    for (Message.Attachment a : newAttachments.getAsAttachmentList()) {
                        ps.setLong(1, noteID);
                        ps.setBytes(2, AttachmentConvertor.convertToBytes(a));
                        ps.setString(3, a.getFileName());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                logger.info("Attachments successfully updated!");
            }

            if (!newTags.getAsStringList().isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(deleteTagsSQL)) {
                    ps.setLong(1, noteID);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(insertTagSQL)) {
                    for (String tag : newTags.getAsStringList()) {
                        ps.setLong(1, noteID);
                        ps.setString(2, tag);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                logger.info("Tags successfully updated!");

            }
            logger.info("Note updated successfully.");
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to update note!", e);
        }
    }
    public boolean deleteNote(long noteID) {
        String statement = "DELETE FROM NOTE WHERE NOTE_ID = ?";
        try (Connection conn = getPoolDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {
            ps.setLong(1, noteID);
            ps.executeUpdate();
            logger.info("Note successfully deleted.");
            return true;
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to delete note!", e);
        }
        return false;
    }
}