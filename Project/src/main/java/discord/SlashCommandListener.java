package discord;

import database.*;
import gemini.AISummaryService;
import latex.LatexConverter;
import logging.LoggerUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.attachmentupload.AttachmentUpload;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SlashCommandListener extends ListenerAdapter {
    private final Logger logger = LoggerUtil.getLogger(SlashCommandListener.class);
    private final AISummaryService aiSummaryService = new AISummaryService();
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

        if (event.getName().equals("help")) {
            event.replyEmbeds(embedBuilder.build()).queue();
        }

        if (event.getName().equals("ask")) {
            event.reply("Processing your request...").setEphemeral(true).queue(hook ->
                    hook.deleteOriginal().queueAfter(5, TimeUnit.SECONDS)
            );
            try {
                String returnedMessage = aiSummaryService.generateResponse(Objects.requireNonNull(event.getOption("asked_prompt")).getAsString());
                sendMessages(event.getMessageChannel(), latexConverter.extractLatexFromString(returnedMessage));
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to generate response.", e);
                event.reply("An error occurred while generating the response.").queue();
            }
        }

        if (event.getName().equals("retrieve_course_codes")) {
            EmbedBuilder embed = new  EmbedBuilder();
            embed.setTitle("All Available Courses");
            embed.setColor(new Color(235, 171, 0));
            StringBuilder sb = new StringBuilder();
            if (courseToTagLinker.getCoursesAsSet().isEmpty()) {
                event.reply("There are no course codes yet.").queue();
                return;
            }
            courseToTagLinker.getCoursesAsSet().stream().sorted().forEach(
                    course -> sb.append("``").append(course).append("``").append("\n"));
            embed.addField("Available courses:", sb.toString(), true);
            event.replyEmbeds(embed.build()).queue();
        }

        if (event.getName().equals("retrieve_tags_by_course")){
            String course = Objects.requireNonNull(event.getOption("provided_course")).getAsString().toUpperCase();
            if (courseToTagLinker.courseCodeDNE(course)) {
                event.reply("Provided course code does not exist.").queue();
                return;
            }

            if (courseToTagLinker.getTagsAsSet(course).isEmpty()) {
                event.reply("Provided course code has no tags yet.").queue();
                return;
            }

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Tags corresponding to " + course);
            embed.setColor(new Color(235, 171, 0));
            StringBuilder sb = new StringBuilder();
            courseToTagLinker.getTagsAsSet(course).stream().sorted().forEach(
                    tag -> sb.append("``").append(tag).append("``").append("\n"));
            embed.addField("Available tags relating to the provided course:", sb.toString(), true);
            event.replyEmbeds(embed.build()).queue();
        }

        if (event.getName().equals("create_tag")) {
            String course = Objects.requireNonNull(event.getOption("provided_course")).getAsString().toUpperCase();
            String tag = Objects.requireNonNull(event.getOption("created_tag")).getAsString().toUpperCase();
            if (dsh.insertTag(course, tag)) {
                courseToTagLinker.addTag(course, tag);
                event.reply("Tag: \"" + tag + "\" has been created in course: \"" + course + "\".").queue();
            } else {
                event.reply("Tag: \"" + tag + "\" does not exist in this course, or already exists.").queue();
            }
        }

        if (event.getName().equals("delete_tag")) {
            String course = Objects.requireNonNull(event.getOption("provided_course")).getAsString().toUpperCase();
            String tag = Objects.requireNonNull(event.getOption("deleted_tag")).getAsString().toUpperCase();
            if (dsh.dropTag(course, tag)) {
                courseToTagLinker.removeTag(course, tag);
                event.reply("Tag: \"" + tag + "\" has been deleted.").queue();
            } else {
                event.reply("Tag: \"" + tag + "\" does not exist in this course, or already exists.").queue();
            }
        }

        if (event.getName().equals("create_course")) {
            String course = Objects.requireNonNull(event.getOption("created_course")).getAsString().toUpperCase();
            String name = Objects.requireNonNull(event.getOption("provided_name")).getAsString();
            if (dsh.insertCourse(course, name)) {
                courseToTagLinker.addCourseCode(course);
                event.reply("Course: \"" + course + "\" has been created.").queue();
            } else {
                event.reply("Tag: \"" + course + "\" already exists.").queue();
            }
        }

        if (event.getName().equals("delete_course")) {
            String course = Objects.requireNonNull(event.getOption("deleted_course")).getAsString().toUpperCase();
            if (dsh.dropCourse(course)) {
                courseToTagLinker.removeCourseCode(course);
                event.reply("Course: \"" + course + "\" and its associated tags has been deleted.").queue();
            } else {
                event.reply("Course: \"" + course + "\" does not exist.").queue();
            }
        }

        if (event.getName().equals("upload_note")) {
            String course = Objects.requireNonNull(event.getOption("asked_course")).getAsString().toUpperCase();
            if (courseToTagLinker.getCoursesAsSet().isEmpty()) {
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

            Modal uploadModal = Modal.create("upload_modal:" + course, "Note Upload Details")
                    .addComponents(
                            Label.of("Attachments", AttachmentUpload.create("uploaded_note").setRequiredRange(1,10).build()),
                            Label.of("Title", TextInput.create("title", TextInputStyle.SHORT).build()),
                            Label.of("Summary", TextInput.create("summary", TextInputStyle.SHORT).build()),
                            Label.of("Tags", courseToTagLinker.getTagsAsSSM(course).build())
                    ).build();
            event.replyModal(uploadModal).queue();
        }

        if (event.getName().equals("retrieve_note_by_id")) {
            event.deferReply().queue();
            long id = Objects.requireNonNull(event.getOption("provided_id")).getAsLong();
            Note note = dsh.retrieveByNoteID(id);
            if (note == null) {
                event.getHook().sendMessage("There is no note associated with id of: " + id).queue();
                return;
            }
            NoteEmbed embed = new NoteEmbed(note, jda);
            List<FileUpload> uploads = new ArrayList<>();
            for (Attachment attachment : note.ATTACHMENTS()) {
                uploads.add(FileUpload.fromData(attachment.data(), attachment.fileName()));
            }
            event.getHook().sendMessageEmbeds(embed.build())
                    .addFiles(uploads)
                    .queue(success -> success.delete().queueAfter(1, TimeUnit.MINUTES));
        }

        if(event.getName().equals("retrieve_ids_by_filter")) {
            event.deferReply().queue(hook -> {
                hook.sendMessage("Processing your request...").queue(success -> success.delete().queueAfter(5, TimeUnit.SECONDS));
                CompletableFuture.runAsync(() -> {
                    EmbedBuilder ids;
                    String course = Objects.requireNonNull(event.getOption("provided_course")).getAsString().toUpperCase();
                    OptionMapping om = event.getOption("provided_tag");
                    if (om == null || om.getAsString().isEmpty()) {
                        ids = dsh.retrieveIDByCourseAndTag(course, null);
                    } else {
                        ids = dsh.retrieveIDByCourseAndTag(course, om.getAsString().toUpperCase());
                    }
                    if (ids.isEmpty()) {
                        hook.sendMessage("There are no note IDs that matched your request.").queue();
                        return;
                    }
                    hook.sendMessageEmbeds(ids.build()).queue();
                });
            });
        }

        if (event.getName().equals("modify_note")) {
            long noteID = Objects.requireNonNull(event.getOption("provided_id")).getAsLong();
            String course = dsh.retrieveCourseCodeById(noteID);
            if (course == null) {
                event.reply("No note exists with provided ID.").queue();
                return;
            }
            Modal modifyModal = Modal.create("modify_modal", "Modify Note Details")
                    .addComponents(
                            Label.of("Change Attachments", AttachmentUpload.create("uploaded_note").setRequiredRange(1,10).build()),
                            Label.of("Change Title", TextInput.create("title", TextInputStyle.SHORT).build()),
                            Label.of("Change Course Code", TextInput.create("course_code", TextInputStyle.SHORT).build()),
                            Label.of("Change Summary", TextInput.create("summary", TextInputStyle.SHORT).build()),
                            Label.of("Change Tags", courseToTagLinker.getTagsAsSSM(course).build())
                    )
                    .build();
            event.replyModal(modifyModal).queue();
        }
//        if (event.getName().equals("change_note_title")) {
//            event.deferReply().queue(hook -> {
//                hook.sendMessage("Processing your request...").queue(success -> success.delete().queueAfter(5, TimeUnit.SECONDS));
//                long noteID = Objects.requireNonNull(event.getOption("provided_id")).getAsLong();
//                String newTitle = Objects.requireNonNull(event.getOption("provided_title")).getAsString();
//                CompletableFuture.runAsync(() -> {
//                    if (dsh.changeTitle(noteID, newTitle)) {
//                        hook.sendMessage("Successfully changed the title of note: " + noteID + " to: " + newTitle).queue();
//                    } else {
//                        hook.sendMessage("Could not change the title of the note!").queue();
//                    }
//                });
//            });
//        }
//
//        if (event.getName().equals("change_note_summary")) {
//            event.deferReply().queue(hook -> {
//                hook.sendMessage("Processing your request...").queue(success -> success.delete().queueAfter(5, TimeUnit.SECONDS));
//                long noteID = Objects.requireNonNull(event.getOption("provided_id")).getAsLong();
//                String newSummary = Objects.requireNonNull(event.getOption("provided_summary")).getAsString();
//                CompletableFuture.runAsync(() -> {
//                    if (dsh.changeSummary(noteID, newSummary)) {
//                        hook.sendMessage("Successfully changed the summary of note: " + noteID + " to: " + newSummary).queue();
//                    } else {
//                        hook.sendMessage("Could not change the summary of the note!").queue();
//                    }
//                });
//            });
//        }
//
//        if (event.getName().equals("change_note_file")) {
//            event.deferReply().queue(hook -> {
//                hook.sendMessage("Processing your request...").queue(success -> success.delete().queueAfter(5, TimeUnit.SECONDS));
//                long noteID = Objects.requireNonNull(event.getOption("provided_id")).getAsLong();
//                Message.Attachment attachment = Objects.requireNonNull(event.getOption("provided_attachment")).getAsAttachment();
//                byte[] data = AttachmentConvertor.convertToBytes(attachment);
//                if(dsh.changeFile(noteID, data, attachment.getFileName())) {
//                    hook.sendMessage("Successfully changed the file of note: " + noteID + " to: " + attachment.getFileName()).queue();
//                } else {
//                    hook.sendMessage("Could not change the file of the note!").queue();
//
//                }
//            });
//        }

        if (event.getName().equals("generate_summary_by_id")) {
            event.deferReply().queue(hook -> {
                hook.sendMessage("Processing your request...").queue(success -> success.delete().queueAfter(5, TimeUnit.SECONDS));
                CompletableFuture.runAsync(() -> {
                    try {
                        long id = Objects.requireNonNull(event.getOption("provided_id")).getAsLong();
                        String prompt = Objects.requireNonNull(event.getOption("provided_prompt")).getAsString();
                        byte[] data = dsh.retrieveBlob(id);
                        if (data == null) {
                            hook.sendMessage("There is no data associated with id of: " + id).queue();
                            return;
                        }
                        String message = aiSummaryService.generateSummary(prompt, data);
                        sendMessages(hook.getInteraction().getMessageChannel(), latexConverter.extractLatexFromString(message));
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Failed to generate summary.", e);
                        hook.sendMessage("An error occurred while generating the summary.").queue();
                    }
                });
            });
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
