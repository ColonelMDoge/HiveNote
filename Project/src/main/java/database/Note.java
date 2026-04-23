package database;

import java.time.OffsetDateTime;
import java.util.List;

// General form of a Note to serve as a blueprint when inserting and retrieving notes
public record Note(long NOTE_ID,
                   String USER_ID,
                   String NOTE_TITLE,
                   OffsetDateTime CREATED_AT,
                   OffsetDateTime UPDATED_AT,
                   String NOTE_SUMMARY,
                   String COURSE_CODE,
                   String COURSE_NAME,
                   List<Attachment> ATTACHMENTS,
                   List<String> TAGS) {
}
