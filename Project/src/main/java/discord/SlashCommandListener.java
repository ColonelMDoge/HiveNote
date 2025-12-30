package discord;

import database.DatabaseServiceHandler;
import database.Note;
import database.NoteEmbed;
import gemini.AISummaryService;
import latex.LatexConverter;
import logging.LoggerUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SlashCommandListener extends ListenerAdapter {
    private final Logger logger = LoggerUtil.getLogger(SlashCommandListener.class);
    private final EmbedBuilder embedBuilder = new BotDescription();
    private final LatexConverter latexConverter = new LatexConverter();
    private final CourseToTagLinker courseToTagLinker;
    private final DatabaseServiceHandler dsh = new DatabaseServiceHandler();
    private JDA jda;
    protected final String CHANNEL_NAME = "bot-channel";

    public SlashCommandListener(CourseToTagLinker courseToTagLinker) {
        this.courseToTagLinker = courseToTagLinker;
    }

    public void setJDA(JDA jda) {
        this.jda = jda;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) return;
        if (!event.getMessageChannel().getName().equals(CHANNEL_NAME)) {
            event.getMessageChannel().sendMessage("You can only use this command in " + CHANNEL_NAME + ".   ").queue();
            return;
        }
        logger.info(String.format("A(n) \"%s\" slash command interaction was received from: %s.", event.getName(), event.getUser().getName()));
        if (event.getName().equals("upload")) {
            String course = Objects.requireNonNull(event.getOption("asked_course")).getAsString().toUpperCase();
            if (courseToTagLinker.isEmpty()) {
                event.reply("There are no existing courses yet.").queue();
                return;
            }
            if (courseToTagLinker.courseCodeDNE(course)) {
                event.reply("Your provided course code does not exist.").queue();
                return;
            }
            if (courseToTagLinker.tagsDNE(course)) {
                event.reply("There are no associated tags with this course yet.").queue();
                return;
            }

            Modal uploadModal = Modal.create("upload_modal", "Note Upload Details")
                    .addComponents(
                            Label.of("Note File", AttachmentUpload.of("uploaded_note")),
                            Label.of("Title", TextInput.create("title", TextInputStyle.SHORT).build()),
                            Label.of("Summary", TextInput.create("summary", TextInputStyle.SHORT).build()),
                            Label.of("Tags", courseToTagLinker.getTagsAsSSM(course).build())
                    ).build();
            event.replyModal(uploadModal).queue();
        }

        if (event.getName().equals("create_tag")) {
            String course = Objects.requireNonNull(event.getOption("provided_course")).getAsString();
            String tag = Objects.requireNonNull(event.getOption("created_tag")).getAsString();
            if (courseToTagLinker.courseCodeDNE(course)) {
                event.reply("Course: \"" + course + "\" does not exist.").queue();
                return;
            }
            if (courseToTagLinker.addTag(course, tag)) {
                event.reply("Tag: \"" + tag + "\" has been created in course: \"" + course + "\".").queue();
                dsh.insertTag(tag, course);
            } else {
                event.reply("Tag: \"" + tag + "\" does not exist in this course, or already exists.").queue();
            }
        }

        if (event.getName().equals("delete_tag")) {
            String course = Objects.requireNonNull(event.getOption("provided_course")).getAsString();
            String tag = Objects.requireNonNull(event.getOption("deleted_tag")).getAsString();
            if (courseToTagLinker.courseCodeDNE(course)) {
                event.reply("Course: \"" + course + "\" does not exist.").queue();
                return;
            }
            if (courseToTagLinker.removeTag(course, tag)) {
                event.reply("Tag: \"" + tag + "\" has been deleted.").queue();
                dsh.dropTag(course, tag);
            } else {
                event.reply("Tag: \"" + tag + "\" does not exist in this course, or already exists.").queue();
            }
        }

        if (event.getName().equals("create_course_code")) {
            String course = Objects.requireNonNull(event.getOption("created_course")).getAsString();
            if (courseToTagLinker.addCourseCode(course)) {
                event.reply("Course: \"" + course + "\" has been created.").queue();
            } else {
                event.reply("Tag: \"" + course + "\" already exists.").queue();
            }
        }

        if (event.getName().equals("delete_course_code")) {
            String course = Objects.requireNonNull(event.getOption("deleted_course")).getAsString();
            if (courseToTagLinker.removeCourseCode(course)) {
                event.reply("Course: \"" + course + "\" has been deleted.").queue();
            } else {
                event.reply("Course: \"" + course + "\" does not exist.").queue();
            }
        }

        if (event.getName().equals("retrieve_by_id")) {
            String id = Objects.requireNonNull(event.getOption("provided_id")).getAsString();
            Note note = dsh.retrieveByNoteID(id);
            NoteEmbed embed = new NoteEmbed(note, jda);
            event.getChannel()
                    .sendMessageEmbeds(embed.build())
                    .addFiles(FileUpload.fromData(note.DOCUMENT_FILE()))
                    .queue();
        }

        if (event.getName().equals("help")) {
            event.replyEmbeds(embedBuilder.build()).queue();
        }

        if (event.getName().equals("ask")) {
            event.reply("Processing your request...").setEphemeral(true).queue(hook ->
                    hook.deleteOriginal().queue()
            );
            String returnedMessage = AISummaryService.generateResponse(Objects.requireNonNull(event.getOption("asked_prompt")).getAsString());
            sendMessages(event.getMessageChannel(), latexConverter.extractLatexFromString(returnedMessage));
        }
    }

    // Determines whether the Object is a byte[] array or a String
    // Bypasses Discord's Message.MAX_CONTENT_LENGTH by sending a message in parts
    private void sendMessages(MessageChannel channel, ArrayList<Object> list) {
        for (Object object : list) {
            if (object instanceof byte[]) {
                try (FileUpload file = FileUpload.fromData((byte[]) object, "file.png")) {
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
                logger.log(Level.WARNING, "There was an object that was not a byte[] array or a String object!", object);
            }
        }
    }
}
