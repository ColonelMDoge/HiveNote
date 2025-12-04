package discord;

import gemini.AISummaryService;
import latex.LatexConverter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.attachmentupload.AttachmentUpload;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;


public class SlashCommandListener extends ListenerAdapter {
    private final EmbedBuilder embedBuilder = new BotDescription();
    protected final String CHANNEL_NAME = "bot-channel";

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) return;
        if (!event.getMessageChannel().getName().equals(CHANNEL_NAME)) {
            event.getMessageChannel().sendMessage("You can only use this command in " + CHANNEL_NAME + "!").queue();
            return;
        }
        if (event.getName().equals("upload")) {
            if (StringSelectMenuManager.getCourseCodeMenu().getOptions().isEmpty()) {
                event.reply("Course code tags are empty! Please create some course codes!").queue();
                return;
            }
            if (StringSelectMenuManager.getTagMenu().getOptions().isEmpty()) {
                event.reply("Tags are empty! Please create some tags!").queue();
                return;
            }
            Modal uploadModal = Modal.create("upload_modal", "Note Upload Details")
                    .addComponents(
                            Label.of("Note File", AttachmentUpload.of("uploaded_note")),
                            Label.of("Title", TextInput.create("title", TextInputStyle.SHORT).build()),
                            Label.of("Summary", TextInput.create("summary", TextInputStyle.SHORT).build()),
                            Label.of("Course Code", StringSelectMenuManager.getCourseCodeMenu().build()),
                            Label.of("Tags", StringSelectMenuManager.getTagMenu().build())
                    ).build();
            event.replyModal(uploadModal).queue();
        }

        if (event.getName().equals("help")) {
            event.replyEmbeds(embedBuilder.build()).queue();
        }

        if (event.getName().equals("ask")) {
            event.reply("Processing your request...").setEphemeral(true).queue(hook ->
                    hook.deleteOriginal().queue()
            );
            String returnedMessage = AISummaryService.generateResponse(Objects.requireNonNull(event.getOption("asked_prompt")).getAsString());
            sendMessages(event.getMessageChannel(), LatexConverter.extractLatexFromString(returnedMessage));
        }

        if (event.getName().equals("create_tag")) {
            String tag = Objects.requireNonNull(event.getOption("created_tag")).getAsString();
            if (StringSelectMenuManager.addTag(tag)) {
                event.reply("Tag: \"" + tag + "\" has been created.").queue();
            } else {
                event.reply("Tag: \"" + tag + "\" already exists.").queue();
            }

        }

        if (event.getName().equals("delete_tag")) {
            String tag = Objects.requireNonNull(event.getOption("deleted_tag")).getAsString();
            if (StringSelectMenuManager.removeTag(tag)) {
                event.reply("Tag: \"" + tag + "\" has been deleted.").queue();
            } else {
                event.reply("Tag: \"" + tag + "\" does not exist.").queue();
            }
        }

        if (event.getName().equals("create_course_code")) {
            String course = Objects.requireNonNull(event.getOption("created_course")).getAsString();
            if (StringSelectMenuManager.addCourseCode(course)) {
                event.reply("Course: \"" + course + "\" has been created.").queue();
            } else {
                event.reply("Tag: \"" + course + "\" already exists.").queue();
            }
        }

        if (event.getName().equals("delete_course_code")) {
            String course = Objects.requireNonNull(event.getOption("deleted_course")).getAsString();
            if (StringSelectMenuManager.removeCourseCode(course)) {
                event.reply("Course: \"" + course + "\" has been deleted.").queue();
            } else {
                event.reply("Course: \"" + course + "\" does not exist.").queue();
            }
        }
    }

    // Determines whether the Object is a File or a String
    // Bypasses Discord's Message.MAX_CONTENT_LENGTH by sending a message in parts
    private void sendMessages(MessageChannel channel, ArrayList<Object> list) {
        for (Object object : list) {
            if (object instanceof File) {
                try (FileUpload file = FileUpload.fromData((File) object)) {
                    channel.sendFiles(file).queue();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (object instanceof String) {
                if (((String) object).length() > Message.MAX_CONTENT_LENGTH) {
                    do {
                        String cutMessage = object.toString().substring(0, Message.MAX_CONTENT_LENGTH);
                        channel.sendMessage(cutMessage).queue();
                        object = object.toString().substring(cutMessage.length());
                    } while (((String) object).length() > Message.MAX_CONTENT_LENGTH);
                }
                channel.sendMessage(object.toString()).queue();
            } else {
                System.out.println("Attempted to send a non String or File object!");
            }
        }
    }
}
