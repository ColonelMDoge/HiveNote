package database;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.List;

public record Note(String USER_ID,
                   String NOTE_TITLE,
                   String COURSE_CODE,
                   OffsetDateTime CREATED_AT,
                   OffsetDateTime UPDATED_AT,
                   String NOTE_SUMMARY,
                   File FILE,
                   String FILE_NAME,
                   List<String> TAGS) {
    @NotNull
    @Override
    public String toString() {
        return String.format("""
                USER_ID: %s
                NOTE_TITLE: %s
                COURSE_CODE: %s
                CREATED_AT Code: %s
                UPDATED_AT: %s
                NOTE_SUMMARY: %s
                FILE_NAME: %s
                TAGS: %s
                """, USER_ID, NOTE_TITLE, COURSE_CODE, CREATED_AT, UPDATED_AT, NOTE_SUMMARY, FILE_NAME, String.join(", ", TAGS));
    }
}
