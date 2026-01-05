package discord;

import database.DatabaseServiceHandler;
import database.Note;
import logging.LoggerUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModalListener extends ListenerAdapter {
    private final DatabaseServiceHandler dsh = new DatabaseServiceHandler();
    private final Logger logger = LoggerUtil.getLogger(ModalListener.class);
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String course = event.getModalId().split(":")[1].toUpperCase();
        if (event.getModalId().startsWith("upload_modal:")) {
            event.deferReply().queue();
            logger.info("Upload modal event has been triggered.");
            Message.Attachment attachment = Objects.requireNonNull(event.getValue("uploaded_note")).getAsAttachmentList().get(0);
            CompletableFuture<byte[]> data = attachment.getProxy().download().thenApply(response -> {
                try (response) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = response.read(buffer, 0, buffer.length)) != -1) {
                        baos.write(buffer, 0, read);
                    }
                    return baos.toByteArray();
                } catch (IOException e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
                return null;
            }).exceptionally(exception -> {
                logger.log(Level.SEVERE, exception.getMessage(), exception);
                return null;
            });
            try {
                Note note = new Note(
                        -1, // Dummy variable
                        event.getUser().getId(),
                        Objects.requireNonNull(event.getValue("title")).getAsString(),
                        course,
                        null,
                        null,
                        Objects.requireNonNull(event.getValue("summary")).getAsString(),
                        data.get(),
                        attachment.getFileName(),
                        Objects.requireNonNull(event.getValue(course + "_tags")).getAsStringList());
                logger.info("Note has been successfully created.");
                dsh.uploadToDB(note);
                event.getHook().sendMessage((note.toString())).queue();
            } catch (NullPointerException | InterruptedException | ExecutionException e) {
                logger.log(Level.SEVERE, "Error with the file", e);
            }
        }
    }
}
