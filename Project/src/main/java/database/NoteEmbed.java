package database;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class NoteEmbed extends EmbedBuilder {
    private static String formatTime(OffsetDateTime time) {
        if (time == null) return "No applicable time zone.";
        final String LOCAL_TIME_ZONE = "America/New_York";
        return time.atZoneSameInstant(ZoneId.of(LOCAL_TIME_ZONE))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z"));
    }

    // Separate class to store the general embed of a note
    // This is so that this will not take up space in SlashCommandListener
    public NoteEmbed(Note note, JDA jda) {
        this.setTitle(note.NOTE_TITLE());
        this.setColor(new Color(235, 171, 0));
        this.setDescription(note.NOTE_SUMMARY());
        this.setThumbnail(Objects.requireNonNull(jda.getUserById(note.USER_ID())).getAvatarUrl());
        this.addField("Course: " + note.COURSE_CODE(), note.COURSE_NAME(), true);
        this.addField("Author:", Objects.requireNonNull(jda.getUserById(note.USER_ID())).getName(), true);
        this.addField("Tags:", (note.TAGS() == null || note.TAGS().isEmpty())
                        ? "No applicable tags."
                        : String.join(", ", note.TAGS()), false
        );
        this.addField("Created At", formatTime(note.CREATED_AT()), true);
        this.addField("Updated At", formatTime(note.UPDATED_AT()), true);
        this.setFooter("Retrieved from the HiveNote DB with an ID of: " + note.NOTE_ID());
    }
}
