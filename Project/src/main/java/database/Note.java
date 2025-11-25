package database;

import com.google.api.client.util.DateTime;

import java.io.File;
import java.util.ArrayList;

public class Note {
    private String note_id;
    private String userid;
    private String database_id;
    private String title;
    private File file;
    private DateTime uploadDate;
    private String content;
    private ArrayList<Tag> tags;
}
