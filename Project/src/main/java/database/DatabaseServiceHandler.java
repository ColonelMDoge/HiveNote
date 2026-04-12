package database;

import java.sql.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import logging.LoggerUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import oracle.ucp.jdbc.PoolDataSource;

public class DatabaseServiceHandler {

    @FunctionalInterface
    private interface SQLFunction<T, R> {
        R apply(T t) throws SQLException;
    }

    @FunctionalInterface
    private interface SQLConsumer<T> {
        void accept(T t) throws SQLException;
    }

    private static PoolDataSource poolDataSource = null;
    private final static String DB_USER = System.getenv("DB_USER");
    private final static String DB_PASSWORD = System.getenv("DB_PASSWORD");
    private final static String CONNECT_STRING = System.getenv("CONNECT_STRING");
    private final static String CONN_FACTORY_CLASS_NAME = "oracle.jdbc.replay.OracleConnectionPoolDataSourceImpl";
    private static final Logger logger = LoggerUtil.getLogger(DatabaseServiceHandler.class);

    public PoolDataSource getPoolDataSource() {
        if (poolDataSource != null) return poolDataSource;
        poolDataSource = PoolDataSourceFactory.getPoolDataSource();
        try {
            poolDataSource.setConnectionFactoryClassName(CONN_FACTORY_CLASS_NAME);
            poolDataSource.setURL("jdbc:oracle:thin:@" + CONNECT_STRING);
            poolDataSource.setUser(DB_USER);
            poolDataSource.setPassword(DB_PASSWORD);
            poolDataSource.setConnectionPoolName("JDBC_UCP_POOL");
            poolDataSource.setLoginTimeout(5);
            logger.info("PoolDataSource created.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "There was an exception creating the PoolDataSource: ", e);
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

    private <T> T executeQuery(String sqlStatement, SQLFunction<PreparedStatement, T> function) {
        try (Connection conn = getPoolDataSource().getConnection();
             PreparedStatement statement = conn.prepareStatement(sqlStatement)) {
            return function.apply(statement);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Could not execute query: " + sqlStatement, ex);
            return null;
        }
    }

    private <T> T executeQuery(String sqlStatement, String[] keys, SQLFunction<PreparedStatement, T> function) {
        try (Connection conn = getPoolDataSource().getConnection();
             PreparedStatement statement = conn.prepareStatement(sqlStatement, keys)) {
            return function.apply(statement);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Could not execute query: " + sqlStatement, ex);
            return null;
        }
    }

    private boolean executeUpdate(String sqlStatement, SQLConsumer<PreparedStatement> consumer) {
        try (Connection conn = getPoolDataSource().getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement statement = conn.prepareStatement(sqlStatement)) {
                consumer.accept(statement);
                int affectedRows = statement.executeUpdate();
                conn.commit();
                return affectedRows > 0;
            } catch (SQLException e) {
                conn.rollback();
                logger.log(Level.WARNING, "Could not commit transaction and thus rolled back", e);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database operation failed", e);
        }
        return false;
    }

    private void executeBatch(String sqlStatement, SQLConsumer<PreparedStatement> consumer) {
        try (Connection conn = getPoolDataSource().getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement statement = conn.prepareStatement(sqlStatement)) {
                consumer.accept(statement);
                statement.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                logger.log(Level.WARNING, "Could not commit batch transaction and thus rolled back", e);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database batch operation failed", e);
        }
    }

    public HashMap<String, Set<String>> preLoadData() {
        String statement = """
                SELECT c.COURSE_CODE, t.TAG_NAME
                FROM COURSE c
                LEFT JOIN TAG t ON c.COURSE_ID = t.COURSE_ID
                """;
        return executeQuery(statement, ps -> {
            HashMap<String, Set<String>> data = new HashMap<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String courseName = rs.getString(1);
                    String tagName = rs.getString(2);
                    Set<String> tags = data.computeIfAbsent(courseName, k -> new HashSet<>());
                    if (tagName != null) {
                        tags.add(tagName);
                    }
                }
            }
            logger.info("Preloading data complete.");
            return data;
        });
    }

    public boolean insertCourse(String course, String name) {
        String statement = """
                INSERT INTO COURSE (COURSE_CODE, COURSE_NAME) VALUES (?, ?)
                """;
        boolean update = executeUpdate(statement, ps -> {
            ps.setString(1, course.toUpperCase());
            ps.setString(2, name);
        });
        if (update) {
            logger.info("Course " + course + " inserted successfully.");
            return true;
        }
        return false;
    }

    public boolean dropCourse(String course) {
        String statement = """
                DELETE FROM COURSE WHERE COURSE_CODE = ?
                """;
        boolean update = executeUpdate(statement, ps -> ps.setString(1, course.toUpperCase()));
        if (update) {
            logger.info(String.format("Dropped course: %s from the database", course.toUpperCase()));
            return true;
        }
        return false;
    }

    public boolean insertTag(String course_code, String tag) {
        String statement = """
                INSERT INTO TAG (TAG_NAME, COURSE_ID)
                SELECT ?, c.COURSE_ID FROM COURSE c
                WHERE c.COURSE_CODE = ?
                """;
        boolean update = executeUpdate(statement, ps -> {
            ps.setString(1, tag.toUpperCase());
            ps.setString(2, course_code.toUpperCase());
        });
        if (update) {
            logger.info(String.format("Inserted tag: \"%s\" with course code: \"%s\" into the database.", tag.toUpperCase(), course_code.toUpperCase()));
            return true;
        }
        return false;
    }

    public boolean dropTag(String course, String tag) {
        String statement = """
                DELETE t FROM TAG t
                JOIN COURSE course ON course.COURSE_ID = t.COURSE_ID
                WHERE t.TAG_NAME = ? AND  course.COURSE_CODE = ?
                """;
        boolean update = executeUpdate(statement, ps -> {
            ps.setString(1, tag.toUpperCase());
            ps.setString(2, course.toUpperCase());
        });
        if (update) {
            logger.info(String.format("Dropped tag: %s from the database", tag.toUpperCase()));
            return true;
        }
        return false;
    }

    public String retrieveCourseCodeById(long noteID) {
        String statement = """
                SELECT c.COURSE_CODE FROM COURSE c
                JOIN NOTE n ON n.COURSE_ID = c.COURSE_ID
                WHERE n.NOTE_ID = ?
                """;
        return executeQuery(statement, ps -> {
            ps.setLong(1, noteID);      // set the parameter
            try (ResultSet rs = ps.executeQuery()) {  // execute query
                if (rs.next()) return rs.getString(1);
                return null;
            }
        });
    }

    private long retrieveTagId(String course, String tag) {
        String statement = """
            SELECT t.TAG_ID FROM TAG t
            JOIN COURSE c ON c.COURSE_ID = t.COURSE_ID
            WHERE t.TAG_NAME = ? AND c.COURSE_CODE = ?
            """;

        Long result = executeQuery(statement, ps -> {
            ps.setString(1, tag.toUpperCase());
            ps.setString(2, course.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    logger.info(String.format("Retrieved tag \"%s\" (Course: \"%s\") with ID: %d", tag, course, id));
                    return id;
                }
            }
            return -1L;
        });

        return (result == null) ? -1L : result;
    }

    public long uploadToDB(Note note) {
        String courseStatement = """
               SELECT COURSE_ID FROM COURSE WHERE COURSE_CODE = ?
               """;
        String statement = """
               INSERT INTO NOTE
               (USER_ID, NOTE_TITLE, CREATED_AT, UPDATED_AT, NOTE_SUMMARY, COURSE_ID)
               VALUES (?,?,?,?,?,?)
               """;
        String attachmentStatement = """
                INSERT INTO ATTACHMENT (NOTE_ID, FILE_NAME, FILE_BLOB)
                VALUES (?,?,?)
                """;

        Long courseID = executeQuery(courseStatement, ps -> {
            ps.setString(1, note.COURSE_CODE());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            } catch (SQLException e) {
                return -1L;
            }
        });
        long courseIDNonNull = (courseID == null) ? -1 : courseID;

        Long noteID = executeQuery(statement, new String[]{"NOTE_ID"}, ps -> {
            ps.setString(1, note.USER_ID());
            ps.setString(2, note.NOTE_TITLE());
            ps.setTimestamp(3, Timestamp.from(Instant.now()));
            ps.setTimestamp(4, Timestamp.from(Instant.now()));
            ps.setString(5, note.NOTE_SUMMARY());
            ps.setLong(6, courseIDNonNull);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "There was an exception inserting a note!", e);            }
            return -1L;
        });
        long noteIDNonNull = (noteID == null) ? -1 : noteID;

        String linkStatement = """
                        INSERT INTO NOTE_TAG_JUNCTION (NOTE_ID, TAG_ID) VALUES (?,?)
                        """;

        for (String tag : note.TAGS()) {
            long tagID = retrieveTagId(note.COURSE_CODE(), tag);
            if (tagID != -1) {
                executeUpdate(linkStatement, ps -> {
                    ps.setLong(1, noteIDNonNull);
                    ps.setLong(2, tagID);
                });
                logger.info(String.format("Successfully linked note ID %d to tag ID %d", noteIDNonNull, tagID));
            }
        }

        executeBatch(attachmentStatement, ps -> {
            for (Attachment attachment : note.ATTACHMENTS()) {
                ps.setLong(1, noteIDNonNull);
                ps.setString(2, attachment.fileName());
                ps.setBytes(3, attachment.data());
                ps.addBatch();
            }
        });

        logger.info("Note successfully uploaded to the database.");
        return noteIDNonNull;
    }

    public List<Attachment> retrieveBlobs(long noteID) {
        String attachmentStatement = """
                SELECT FILE_NAME, FILE_BLOB FROM ATTACHMENT WHERE NOTE_ID = ? ORDER BY FILE_NAME
                """;
        return executeQuery(attachmentStatement, ps -> {
            ps.setLong(1, noteID);
            try (ResultSet rs = ps.executeQuery()) {
                List<Attachment> blobList = new ArrayList<>();
                while (rs.next()) {
                    blobList.add(new Attachment(rs.getString(1), rs.getBytes(2)));
                }
                return blobList;
            }
        });
    }

    public HashMap<Long, String> retrieveIDByCourseAndTag(String course, String tag) {
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
        return executeQuery(statement, ps -> {
            ps.setString(1, course);
            if (tag == null) {
                ps.setNull(2, Types.VARCHAR);
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(2, tag);
                ps.setString(3, tag);
            }
            HashMap<Long, String> map = new HashMap<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.putIfAbsent(rs.getLong(1), rs.getString(2));
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Could not retrieve IDs from the database.", e);
            }
            return map;
        });
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

        List<String> tags = executeQuery(sqlStatement, ps -> {
            ps.setLong(1, noteID);
            try (ResultSet rs = ps.executeQuery()) {
                List<String> tagList = new ArrayList<>();
                while (rs.next()) {
                    tagList.add(rs.getString(1));
                }
                return tagList;
            }
        });

        List<Attachment> blobs = retrieveBlobs(noteID);

        return executeQuery(statement, ps -> {
            ps.setLong(1, noteID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Note(
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
                }
            }
            return null;
        });
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

        executeUpdate(updateNoteSQL, ps -> {
            if (newTitle == null) ps.setNull(1, Types.VARCHAR);
            else ps.setString(1, newTitle.getAsString());
            if (newSummary == null) ps.setNull(2, Types.VARCHAR);
            else ps.setString(2, newSummary.getAsString());
            ps.setTimestamp(3, Timestamp.from(Instant.now()));
            if (newCourseCode.getAsStringList().isEmpty()) ps.setNull(4, Types.VARCHAR);
            else ps.setString(4, newCourseCode.getAsStringList().get(0));
            ps.setLong(5, noteID);
            logger.info("Descriptors successfully updated!");
        });

        if (!newAttachments.getAsAttachmentList().isEmpty()) {
            executeUpdate(deleteAttachmentsSQL, ps -> ps.setLong(1, noteID));
            executeBatch(insertAttachmentsSQL, ps -> {
                for (Message.Attachment a : newAttachments.getAsAttachmentList()) {
                    ps.setLong(1, noteID);
                    ps.setBytes(2, AttachmentConvertor.convertToBytes(a));
                    ps.setString(3, a.getFileName());
                    ps.addBatch();
                }
            });
            logger.info("Attachments successfully updated!");
        }
        if (!newTags.getAsStringList().isEmpty()) {
            executeUpdate(deleteTagsSQL, ps -> ps.setLong(1, noteID));
            executeBatch(insertTagSQL, ps -> {
                for (String tag : newTags.getAsStringList()) {
                    ps.setLong(1, noteID);
                    ps.setString(2, tag);
                    ps.addBatch();
                }
                logger.info("Tags successfully updated!");
            });
            logger.info("Note updated successfully.");
        }
    }

    public boolean deleteNote(long noteID) {
        String statement = "DELETE FROM NOTE WHERE NOTE_ID = ?";
        return executeUpdate(statement, ps -> ps.setLong(1, noteID));
    }
}