package database;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.List;

public record Note(String USER_ID,
                   String TITLE,
                   String COURSE_CODE,
                   String NOTE_CONTENT,
                   OffsetDateTime CREATED_AT,
                   OffsetDateTime UPDATED_AT,
                   File DOCUMENT_FILE,
                   List<String> TAGS) {
    @NotNull
    @Override
    public String toString() {
        return String.format("""
                Note: %s
                Title: %s
                User: %s
                Course Code: %s
                Summary: %s
                Tags: %s
                Created: %s
                Updated: %s
                """, DOCUMENT_FILE.getName(), TITLE, USER_ID, COURSE_CODE, NOTE_CONTENT, String.join(",", TAGS), CREATED_AT, UPDATED_AT);
    }
}
