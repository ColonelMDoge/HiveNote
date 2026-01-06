package database;

import java.awt.*;
import java.io.*;
import java.sql.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import logging.LoggerUtil;
import net.dv8tion.jda.api.EmbedBuilder;
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
                logger.info("PoolDataSource created.");
                return poolDataSource;
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "There was an exception creating the PoolDataSource: ", e);
            }
        }
        return poolDataSource;
    }

    public void insertTag(String course, String tag) {
        String statement = """
                INSERT INTO HIVENOTE_TAG (TAG_NAME, COURSE_CODE) VALUES (?, ?)
                """;
        try (Connection conn = getPoolDataSource().getConnection(); PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, tag.toUpperCase());
                ps.setString(2, course.toUpperCase());
                ps.executeUpdate();
                logger.info(String.format("Inserted tag: \"%s\" with course code: \"%s\" into the database.", tag.toUpperCase(), course.toUpperCase()));
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "There was an exception inserting a tag!", e);
        }
    }

    public void dropTag(String course, String tag) {
        String statement = """
                DELETE FROM HIVENOTE_TAG WHERE TAG_NAME = ? AND COURSE_CODE = ?
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
    }

    private void noteTagJunctionLinker(Connection conn, long note_id, long tag_id) throws SQLException {
        String statement = """
                INSERT INTO NOTE_TAGS (NOTE_ID, TAG_ID) VALUES (?,?)
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
                SELECT TAG_ID FROM HIVENOTE_TAG WHERE TAG_NAME = ? AND COURSE_CODE = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(statement)) {
            ps.setString(1, tag.toUpperCase());
            ps.setString(2, course.toUpperCase());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                long id = rs.getLong(1);
                logger.info(String.format("Retrieved tag \"%s\" with corresponding course \"%s\" and an ID of %d", tag, course, id));
                return id;
            }
            return -1;
        }
    }

    public void uploadToDB(Note note) {
        String statement = """
               INSERT INTO HIVENOTE_NOTE
               (USER_ID, NOTE_TITLE, COURSE_CODE, CREATED_AT, UPDATED_AT, NOTE_SUMMARY, FILE_BLOB, FILE_NAME)
               VALUES (?,?,?,?,?,?,?,?)
               """;
        try (Connection conn = getPoolDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(statement, new String[]{"NOTE_ID"})) {
            ps.setString(1, note.USER_ID());
            ps.setString(2, note.NOTE_TITLE());
            ps.setString(3, note.COURSE_CODE());
            ps.setTimestamp(4, Timestamp.from(Instant.now()));
            ps.setTimestamp(5, Timestamp.from(Instant.now()));
            ps.setString(6, note.NOTE_SUMMARY());
            ps.setBytes(7, note.FILE_BLOB());
            ps.setString(8, note.FILE_NAME());
            ps.executeUpdate();

            logger.info("Note successfully uploaded to the database.");

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                long note_id = rs.getLong(1);
                for (String tag : note.TAGS()) {
                    long tag_id = retrieveTagId(conn, note.COURSE_CODE(), tag);
                    noteTagJunctionLinker(conn, note_id, tag_id);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "There was an exception inserting a note!", e);
        }
    }

    public byte[] retrieveBlob(long noteID) {
        String statement = """
                SELECT FILE_BLOB FROM HIVENOTE_NOTE WHERE NOTE_ID = ?
                """;
        try (Connection conn = getPoolDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {
            ps.setLong(1, noteID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBytes(1);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "There was an exception retrieving a blob!", e);
        }
        logger.warning("Blob could not be retrieved from the database.");
        return null;
    }

    public EmbedBuilder retrieveIDByCourseAndTag(String course, String tag) {
        String statement = """
                SELECT DISTINCT hnn.NOTE_ID, hnn.NOTE_TITLE
                FROM HIVENOTE_NOTE hnn
                JOIN NOTE_TAGS nt ON nt.NOTE_ID = hnn.NOTE_ID
                JOIN HIVENOTE_TAG hnt ON hnt.TAG_ID = nt.TAG_ID
                WHERE hnt.COURSE_CODE = ? AND (? IS NULL OR hnt.TAG_NAME = ?)
                ORDER BY hnn.NOTE_ID
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
            return embed;
        }
        embed.setTitle("Fetched Notes by ID");
        embed.setColor(new Color(235, 171, 0));
        embed.addField("Fetched IDs:", builder.toString(), true);
        return embed;
    }

    private Note formNoteFromResultSet(ResultSet rs, List<String> tags) throws SQLException {
        if (rs.next()) {
            String fileName = rs.getString("FILE_NAME");
            byte[] blobBytes = rs.getBytes("FILE_BLOB");

            return new Note(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4),
                    rs.getTimestamp(5).toInstant().atOffset(ZoneOffset.UTC),
                    rs.getTimestamp(6).toInstant().atOffset(ZoneOffset.UTC),
                    rs.getString(7),
                    blobBytes,
                    fileName,
                    tags
            );
        }
        return null;
    }

    public Note retrieveByNoteID(long noteID) {
        String statement = """
                SELECT * FROM HIVENOTE_NOTE WHERE NOTE_ID = ?
                """;
        String sqlStatement = """
                SELECT t.TAG_NAME FROM HIVENOTE_TAG t\s
                JOIN NOTE_TAGS nt ON nt.TAG_ID = t.TAG_ID\s
                WHERE nt.NOTE_ID = ? ORDER BY t.TAG_NAME
                """;
        try (Connection conn = getPoolDataSource().getConnection();
             PreparedStatement sql = conn.prepareStatement(sqlStatement)) {
            List<String> tags = new ArrayList<>();

            sql.setLong(1, noteID);
            ResultSet sqlRS = sql.executeQuery();
            while (sqlRS.next()) {
                tags.add(sqlRS.getString(1));
            }
            PreparedStatement ps = conn.prepareStatement(statement);
            ps.setLong(1, noteID);
            return formNoteFromResultSet(ps.executeQuery(), tags);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "There was an exception getting a note!", e);
        }
        return null;
    }

    public boolean changeTitle(long noteID, String newTitle) {
        String statement = """
                UPDATE HIVENOTE_NOTE SET NOTE_TITLE = ?, UPDATED_AT = ? WHERE NOTE_ID = ?
                """;
        try (Connection conn = getPoolDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {
            ps.setString(1, newTitle);
            ps.setTimestamp(2, Timestamp.from(Instant.now()));
            ps.setLong(3, noteID);
            ps.executeUpdate();
            logger.info("Title successfully updated.");
            return true;
        } catch (SQLException e) {
            logger.log(Level.WARNING, "There was a problem updating the title of a note!", e);
        }
        return false;
    }

    public boolean changeSummary(long noteID, String newSummary) {
        String statement = """
                UPDATE HIVENOTE_NOTE SET NOTE_SUMMARY = ?, UPDATED_AT = ? WHERE NOTE_ID = ?
                """;
        try (Connection conn = getPoolDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {
            ps.setString(1, newSummary);
            ps.setTimestamp(2, Timestamp.from(Instant.now()));
            ps.setLong(3, noteID);
            ps.executeUpdate();
            logger.info("Summary successfully updated.");
            return true;
        } catch (SQLException e) {
            logger.log(Level.WARNING, "There was a problem updating the summary of a note!", e);
        }
        return false;
    }

    public boolean changeFile(long noteID, byte[] newFile, String newFileName) {
        String statement = """
                UPDATE HIVENOTE_NOTE SET FILE_BLOB = ?, FILE_NAME = ?, UPDATED_AT = ? WHERE NOTE_ID = ?
                """;
        try (Connection conn = getPoolDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {
            ps.setBytes(1, newFile);
            ps.setString(2, newFileName);
            ps.setTimestamp(3, Timestamp.from(Instant.now()));
            ps.setLong(4, noteID);
            ps.executeUpdate();
            logger.info("File successfully updated.");
            return true;
        } catch (SQLException e) {
            logger.log(Level.WARNING, "There was a problem updating the file of a note!", e);
        }
        return false;
    }
}