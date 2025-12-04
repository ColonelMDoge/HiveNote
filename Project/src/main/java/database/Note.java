package database;

import java.io.File;
import java.util.List;

public class Note {
    private final String title;
    private final String summary;
    private final File file;
    private final String courseCode;
    private final List<String> tags;
    public Note(String title, String summary, File file, String courseCode, List<String> tags) {
        this.title = title;
        this.summary = summary;
        this.file = file;
        this.courseCode = courseCode;
        this.tags = tags;
    }
    @Override
    public String toString() {
        return "Note [title=" + title + ", summary=" + summary + ", file=" + file.getName() + ", courseCode=" + courseCode + ", tags=" + tags.toString() + "]";
    }
}
