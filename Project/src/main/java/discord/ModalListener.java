package discord;

import database.Attachment;
import database.AttachmentConvertor;
import database.DatabaseServiceHandler;
import database.Note;
import logging.LoggerUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
            List<Message.Attachment> attachment = Objects.requireNonNull(event.getValue("uploaded_note")).getAsAttachmentList();
            try {
                List<Attachment> attachments = new ArrayList<>();
                for (Message.Attachment a : attachment) {
                    attachments.add(new Attachment(a.getFileName(), AttachmentConvertor.convertToBytes(a)));
                }

                Note note = new Note(
                        -1, // Dummy variable
                        event.getUser().getId(),
                        Objects.requireNonNull(event.getValue("title")).getAsString(),
                        null,
                        null,
                        Objects.requireNonNull(event.getValue("summary")).getAsString(),
                        course,
                        attachments,
                        Objects.requireNonNull(event.getValue(course + "_tags")).getAsStringList());
                logger.info("Note has been successfully created.");
                dsh.uploadToDB(note);
                event.getHook().sendMessage((note.toString())).queue();
            } catch (NullPointerException e) {
                logger.log(Level.SEVERE, "Error with the file", e);
            }
        }
    }
}
