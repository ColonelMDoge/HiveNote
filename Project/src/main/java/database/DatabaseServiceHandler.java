package database;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
                DELETE FROM HIVENOTE_TAG WHERE TAG_NAME = ? AND COURSE_CODE = ?;
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
                INSERT INTO NOTE_TAGS (NOTE_ID, TAG_ID) VALUES (?,?);
                """;
        try (PreparedStatement ps = conn.prepareStatement(statement)) {
            ps.setLong(1, note_id);
            ps.setLong(2, tag_id);
            ps.executeUpdate();
        }
    }

    private long retrieveTagId(Connection conn, String course, String tag) throws SQLException {
        String statement = """
                SELECT TAG_ID FROM HIVENOTE_TAG WHERE TAG_NAME = ? AND COURSE_CODE = ?;
                """;
        try (PreparedStatement ps = conn.prepareStatement(statement)) {
            ps.setString(1, tag.toUpperCase());
            ps.setString(2, course.toUpperCase());
            ResultSet rs = ps.executeQuery();
            return rs.getLong(1);
        }
    }
    public void uploadToDB(Note note) {
        String statement = """
               INSERT INTO HIVENOTE_NOTE\s
               (USER_ID, NOTE_TITLE, COURSE_CODE, NOTE_CONTENT, UPDATED_AT, DOCUMENT_FILE)\s
               VALUES (?,?,?,?,?,?);
               """;
        try (Connection conn = poolDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, note.USER_ID());
            ps.setString(2, note.TITLE());
            ps.setString(3, note.COURSE_CODE());
            ps.setString(4, note.NOTE_CONTENT());
            ps.setObject(5, OffsetDateTime.now(ZoneOffset.UTC));
            ps.setObject(6, note.DOCUMENT_FILE());

            ResultSet rs = ps.getGeneratedKeys();
            long note_id = rs.getLong(1);
            for (String tag : note.TAGS()) {
                long tag_id = retrieveTagId(conn, note.COURSE_CODE(), tag);
                noteTagJunctionLinker(conn, note_id, tag_id);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "There was an exception inserting a note!", e);
        }
    }

    public void retrieveByCourseAndTag(String course, String tag) {
        testConnection();
    }

    public Note retrieveByNoteID(String noteID) {
        Note retrievedNote;
        String statement = """
                SELECT * FROM HIVENOTE_NOTE WHERE NOTE_ID = ?;
                """;
        String sqlStatement = """
                SELECT t.TAG_NAME FROM HIVENOTE_TAG t\s
                JOIN HIVENOTE_NOTE_TAG nt ON nt.TAG_ID = t.TAG_ID\s
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
            ResultSet rs = ps.executeQuery();
            retrievedNote = new Note(
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4),
                    rs.getString(5),
                    rs.getDate(6).toInstant().atOffset(ZoneOffset.UTC),
                    rs.getDate(7).toInstant().atOffset(ZoneOffset.UTC),
                    (File) rs.getObject(8),
                    tags
            );
            return retrievedNote;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "There was an exception getting a note!", e);
        }
        return null;
    }
}