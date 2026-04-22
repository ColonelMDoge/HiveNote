package discord;

import database.Attachment;
import database.DatabaseServiceHandler;
import database.Note;
import database.NoteEmbed;
import gemini.AISummaryService;
import latex.LatexConverter;
import logging.LoggerUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.FileUpload;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class ButtonListener extends ListenerAdapter {
    private final DatabaseServiceHandler dsh = new DatabaseServiceHandler();
    private final AISummaryService ai = new AISummaryService();
    private final LatexConverter latexConverter = new LatexConverter();
    private final Logger logger = LoggerUtil.getLogger(this.getClass());
    private JDA jda;

    public void setJDA(JDA jda) {
        this.jda = jda;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        logger.info(String.format("A button interaction was received from: %s.", event.getUser().getName()));
        if (event.getComponentId().startsWith("note_")) {
            String[] parts = event.getComponentId().split("_");

            String action = parts[1];
            long noteID = Long.parseLong(parts[2]);
            int page = Integer.parseInt(parts[3]);

            Note note = dsh.retrieveByNoteID(noteID);
            List<Attachment> attachments = note.ATTACHMENTS();

            if (action.equals("next")) page++;
            if (action.equals("prev")) page--;

            page = Math.max(0, Math.min(page, attachments.size() - 1));

            Attachment current = attachments.get(page);

            NoteEmbed embed = new NoteEmbed(note, jda);
            embed.addField("Attachment (" + (page + 1) + "/" + attachments.size() + ")",
                    current.fileName(),
                    false);

            Button prev = Button.primary("note_prev_" + noteID + "_" + page, Emoji.fromFormatted("⬅"))
                    .withDisabled(page == 0);
            Button next = Button.primary("note_next_" + noteID + "_" + page, Emoji.fromFormatted("➡"))
                    .withDisabled(page == attachments.size() - 1);
            Button gemini = Button.primary("summarize_" + noteID + "_" + page, "Summarize Using Gemini");
            Button close = Button.primary("delete", "Close message");

            event.editMessageEmbeds(embed.build())
                    .setFiles(FileUpload.fromData(current.data(), current.fileName()))
                    .setComponents(ActionRow.of(prev, next, gemini, close))
                    .queue();
        }
        if (event.getComponentId().equals("delete")) {
            event.deferEdit().queue();
            event.getHook().deleteOriginal().queue();
        }
        if (event.getComponentId().startsWith("summarize_")) {
            event.deferReply(true).queue();
            InteractionHook hook = event.getHook();

            CompletableFuture.runAsync(() -> {
                String[] parts = event.getComponentId().split("_");
                long noteID = Long.parseLong(parts[1]);
                int page = Integer.parseInt(parts[2]);
                List<Attachment> attachments = dsh.retrieveBlobs(noteID);
                if (attachments.isEmpty()) {
                    hook.sendMessage("There is no data associated with id of: " + noteID).setEphemeral(true).queue();
                    return;
                }
                String message = ai.generateSummary(attachments.get(page).data());
                Button close = Button.primary("delete", "Close message");
                byte[] data = latexConverter.convertStringToLatex(message);
                if (data == null || message == null) {
                    hook.sendMessage("Could not generate a summary. Please try again later.").setEphemeral(true).queue();
                } else {
                    hook.sendFiles(FileUpload.fromData(data, "File.png"))
                            .setComponents(ActionRow.of(close))
                            .queue();
                }
            });
        }
    }
}
