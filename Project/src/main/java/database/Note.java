package database;

import java.time.OffsetDateTime;
import java.util.List;

public record Note(long NOTE_ID,
                   String USER_ID,
                   String NOTE_TITLE,
                   OffsetDateTime CREATED_AT,
                   OffsetDateTime UPDATED_AT,
                   String NOTE_SUMMARY,
                   String COURSE_CODE,
                   List<Attachment> ATTACHMENTS,
                   List<String> TAGS) {
}
