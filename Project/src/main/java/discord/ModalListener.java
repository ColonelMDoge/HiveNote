package discord;

import database.Note;
import logging.LoggerUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModalListener extends ListenerAdapter {
    private final Logger logger = LoggerUtil.getLogger(ModalListener.class);
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (event.getModalId().equals("upload_modal")) {
            Message.Attachment attachment = Objects.requireNonNull(event.getValue("uploaded_note")).getAsAttachmentList().get(0);
            File file;
            try {
                file = File.createTempFile(attachment.getFileName().substring(0, attachment.getFileName().lastIndexOf('.')), '.' + attachment.getFileExtension());
                CompletableFuture<File> future = attachment.getProxy().downloadToFile(file);
                future.exceptionally(error -> {
                    logger.log(Level.SEVERE, "Error downloading file", error);
                    return null;
                });
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error with the file", e);
                return;
            }
            Note note = new Note(
                    event.getUser().getId(),
                    Objects.requireNonNull(event.getValue("title")).getAsString(),
                    Objects.requireNonNull(event.getValue("course_code_menu")).getAsStringList().get(0),
                    Objects.requireNonNull(event.getValue("summary")).getAsString(),
                    null,
                    null,
                    file,
                    Objects.requireNonNull(event.getValue("tag_menu")).getAsStringList());
            event.reply(note.toString()).queue();
        }
    }
}
