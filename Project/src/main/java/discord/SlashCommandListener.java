package discord;

import database.*;
import gemini.AISummaryService;
import latex.LatexConverter;
import logging.LoggerUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.attachmentupload.AttachmentUpload;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.*;
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
                event.getHook().sendFiles(FileUpload.fromData(latexConverter.convertLatexToImage(returnedMessage), "File.png")).queue();
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
            int page = 0;
            Note note = dsh.retrieveByNoteID(id);
            if (note == null) {
                event.getHook().sendMessage("There is no note associated with id of: " + id + ".").queue();
                return;
            }
            NoteEmbed embed = new NoteEmbed(note, jda);
            List<Attachment> attachments = note.ATTACHMENTS();
            Attachment current = attachments.get(page);
            embed.addField("Attachment (" + (page + 1) + "/" + attachments.size() + ")",
                    current.fileName(),
                    false);
            Button prev = Button.primary("note_prev_" + id + "_" + page, Emoji.fromFormatted("⬅"));
            Button next = Button.primary("note_next_" + id + "_" + page, Emoji.fromFormatted("➡"));
            Button gemini = Button.primary("summarize_" + id + "_" + page, "Summarize Using Gemini");
            Button close = Button.primary("delete", "Close message");
            event.getHook().sendMessageEmbeds(embed.build())
                    .addFiles(FileUpload.fromData(current.data(), current.fileName()))
                    .addComponents(ActionRow.of(prev, next, gemini, close))
                    .queue();
        }

        if(event.getName().equals("retrieve_ids_by_filter")) {
            event.deferReply().queue(hook -> {
                hook.sendMessage("Processing your request...").queue(success -> success.delete().queueAfter(5, TimeUnit.SECONDS));
                CompletableFuture.runAsync(() -> {
                    EmbedBuilder ids;
                    String course = Objects.requireNonNull(event.getOption("provided_course")).getAsString().toUpperCase();
                    OptionMapping om = event.getOption("provided_tag");
                    if (om == null || om.getAsString().isEmpty()) {
                        ids = new NoteEmbed(dsh.retrieveIDByCourseAndTag(course, null));
                    } else {
                        ids = new NoteEmbed(dsh.retrieveIDByCourseAndTag(course, om.getAsString().toUpperCase()));
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
            Modal modifyModal = Modal.create("modify_modal:" + course + ":" + noteID, "Modify Note Details")
                    .addComponents(
                            Label.of("Change Attachments", AttachmentUpload.create("modified_notes").setRequiredRange(1,10).setRequired(false).build()),
                            Label.of("Change Title", TextInput.create("title", TextInputStyle.SHORT).setRequired(false).build()),
                            Label.of("Change Course Code", courseToTagLinker.getCoursesAsSSM().setRequired(false).build()),
                            Label.of("Change Summary", TextInput.create("summary", TextInputStyle.SHORT).setRequired(false).build()),
                            Label.of("Change Tags", courseToTagLinker.getTagsAsSSM(course).setRequired(false).build())
                    )
                    .build();
            event.replyModal(modifyModal).queue();
        }

        if (event.getName().equals("delete_note")) {
            long noteID = Objects.requireNonNull(event.getOption("provided_id")).getAsLong();
            if (dsh.retrieveByNoteID(noteID) == null) {
                event.reply("There is no note with the associated ID of: " + noteID).queue();
                return;
            }
            if (dsh.deleteNote(noteID)) {
                event.reply("Note successfully deleted.").queue();
            } else {
                event.reply("Note could not be deleted!").queue();
            }
        }
    }
}
