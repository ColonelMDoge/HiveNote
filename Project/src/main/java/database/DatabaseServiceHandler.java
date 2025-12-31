package database;

import java.io.*;
import java.sql.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import logging.LoggerUtil;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import oracle.ucp.jdbc.PoolDataSource;

public class DatabaseServiceHandler {
    private final PoolDataSource poolDataSource;
    private final static String DB_USER = System.getenv("DB_USER");
    private final static String DB_PASSWORD = System.getenv("DB_PASSWORD");
    private final static String CONNECT_STRING = System.getenv("CONNECT_STRING");
    private final static String CONN_FACTORY_CLASS_NAME = "oracle.jdbc.replay.OracleConnectionPoolDataSourceImpl";
    private static final Logger logger = LoggerUtil.getLogger(DatabaseServiceHandler.class);

    public DatabaseServiceHandler() {
        this.poolDataSource = PoolDataSourceFactory.getPoolDataSource();
        try {
            poolDataSource.setConnectionFactoryClassName(CONN_FACTORY_CLASS_NAME);
            poolDataSource.setURL("jdbc:oracle:thin:@" + CONNECT_STRING);
            poolDataSource.setUser(DB_USER);
            poolDataSource.setPassword(DB_PASSWORD);
            poolDataSource.setConnectionPoolName("JDBC_UCP_POOL");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "There was an exception creating the PoolDataSource: ", e);
        }

    }
    public void testConnection() {
        try (Connection conn = poolDataSource.getConnection()) {
            logger.info("Connected to the database.");
            System.out.println(conn.getSchema());

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Could not connect to the database - SQLException occurred: ", e);
        }
    }

    public void insertTag(String course, String tag) {
        String statement = """
                INSERT INTO HIVENOTE_TAG (TAG_NAME, COURSE_CODE) VALUES (?, ?)
                """;
        try (Connection conn = poolDataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(statement)) {
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
        try (Connection conn = poolDataSource.getConnection();
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
               INSERT INTO HIVENOTE_NOTE\s
               (USER_ID, NOTE_TITLE, COURSE_CODE, UPDATED_AT, NOTE_SUMMARY, FILE, FILE_NAME)\s
               VALUES (?,?,?,?,?,?,?)
               """;
        try (Connection conn = poolDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement, new String[]{"NOTE_ID"});
             FileInputStream fis = new FileInputStream(note.FILE())) {
            ps.setString(1, note.USER_ID());
            ps.setString(2, note.NOTE_TITLE());
            ps.setString(3, note.COURSE_CODE());
            ps.setTimestamp(4, Timestamp.valueOf(OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime()));
            ps.setString(5, note.NOTE_SUMMARY());
            ps.setBlob(6, fis);
            ps.setString(7, note.FILE().getName());
            ps.executeUpdate();

            logger.info("Note successfully uploaded to the database.");

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                long note_id = rs.getLong("NOTE_ID");
                for (String tag : note.TAGS()) {
                    long tag_id = retrieveTagId(conn, note.COURSE_CODE(), tag);
                    noteTagJunctionLinker(conn, note_id, tag_id);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "There was an exception inserting a note!", e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "There was an exception converting a note to an input stream!", e);
        }
    }

    public void retrieveByCourseAndTag(String course, String tag) {
        testConnection();
    }

    private Note formNoteFromResultSet(ResultSet rs, List<String> tags) throws SQLException {
        if (rs.next()) {
            String fileName = rs.getString("FILE_NAME");
            Blob blob = rs.getBlob("FILE");
            InputStream inputStream = blob.getBinaryStream();
            try (FileOutputStream fis = new FileOutputStream("src/main/java/data/" + fileName)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) > 0) {
                    fis.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "There was an exception reading file!", e);
            }

            File file = new File("src/main/java/data/" + fileName);
            file.deleteOnExit();
            return new Note(
                    rs.getString("USER_ID"),
                    rs.getString("NOTE_TITLE"),
                    rs.getString("COURSE_CODE"),
                    rs.getDate("CREATED_AT").toInstant().atOffset(ZoneOffset.UTC),
                    rs.getDate("UPDATED_AT").toInstant().atOffset(ZoneOffset.UTC),
                    rs.getString("NOTE_SUMMARY"),
                    file,
                    fileName,
                    tags
            );
        } else return null;
    }

    public Note retrieveByNoteID(String noteID) {
        String statement = """
                SELECT * FROM HIVENOTE_NOTE WHERE NOTE_ID = ?
                """;
        String sqlStatement = """
                SELECT t.TAG_NAME FROM HIVENOTE_TAG t\s
                JOIN NOTE_TAGS nt ON nt.TAG_ID = t.TAG_ID\s
                WHERE nt.NOTE_ID = ? ORDER BY t.TAG_NAME
                """;
        try (Connection conn = poolDataSource.getConnection()) {
            List<String> tags = new ArrayList<>();
            PreparedStatement sql = conn.prepareStatement(sqlStatement);
            sql.setString(1, noteID);
            ResultSet sqlRS = sql.executeQuery();
            while (sqlRS.next()) {
                tags.add(sqlRS.getString(1));
            }
            PreparedStatement ps = conn.prepareStatement(statement);
            ps.setString(1, noteID);
            return formNoteFromResultSet(ps.executeQuery(), tags);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "There was an exception getting a note!", e);
        }
        return null;
    }
}