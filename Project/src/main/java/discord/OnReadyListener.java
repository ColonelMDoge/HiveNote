package discord;

import database.DatabaseServiceHandler;
import logging.LoggerUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.logging.Logger;

public class OnReadyListener extends ListenerAdapter {
    private final Logger logger = LoggerUtil.getLogger(OnReadyListener.class);
    private final CourseToTagLinker courseToTagLinker;
    private final SlashCommandListener slashCommandListener;
    private final DatabaseServiceHandler dsh = new DatabaseServiceHandler();

    public OnReadyListener(CourseToTagLinker courseToTagLinker, SlashCommandListener slashCommandListener) {
        this.courseToTagLinker = courseToTagLinker;
        this.slashCommandListener = slashCommandListener;
    }

    public void onReady(@NotNull ReadyEvent event) {
        if (dsh.testConnection()) {
            logger.info("Connected to the HiveNote database.");
        } else {
            throw new RuntimeException("Could not connect to the HiveNote database.");
        }

        logger.info("The Discord bot is ready.");
        JDA jda = event.getJDA();
        slashCommandListener.setJDA(jda);
        Guild testGuild = jda.getGuildById("1127969348049457295");
        if (testGuild != null) {
            testGuild.updateCommands().addCommands(

                    // Overall commands not tied to notes
                    Commands.slash("ask", "Prompt you want to ask.")
                            .addOption(OptionType.STRING, "asked_prompt", "Prompt you want to ask.", true),

                    Commands.slash("help", "Get a list of commands."),

                    Commands.slash("retrieve_course_codes", "Retrieve a list of all course codes."),

                    Commands.slash("retrieve_tags_by_course", "Retrieve a list of all tags relating to a specific course.")
                            .addOption(OptionType.STRING, "provided_course", "Required course code.", true),

                    // Course and tag creation commands
                    Commands.slash("create_tag", "Create a tag.")
                            .addOption(OptionType.STRING, "provided_course", "Course code relating to the tag.", true)
                            .addOption(OptionType.STRING, "created_tag", "Tag you want to create.", true),

                    Commands.slash("delete_tag", "Delete a tag.")
                            .addOption(OptionType.STRING, "provided_course", "Course code relating to the tag.", true)
                            .addOption(OptionType.STRING, "deleted_tag", "Delete a tag that exists.", true),

                    Commands.slash("create_course", "Create a course code.")
                            .addOption(OptionType.STRING, "created_course", "Course code you want to create.", true)
                            .addOption(OptionType.STRING, "provided_name", "The name of the course.", true),

                    Commands.slash("delete_course", "Delete a course code.")
                            .addOption(OptionType.STRING, "deleted_course", "Delete a course code that exists.", true),

                    // Database related commands
                    Commands.slash("upload_note", "Upload a file to the database.")
                            .addOption(OptionType.STRING, "asked_course", "Course code related to file.", true),

                    Commands.slash("retrieve_note_by_id", "Retrieve a note based on its database ID.")
                            .addOption(OptionType.INTEGER, "provided_id", "Provided ID.", true),

                    Commands.slash("retrieve_ids_by_filter", "Retrieve a list of IDs based on classifiers")
                            .addOption(OptionType.STRING, "provided_course", "Required course code.", true)
                            .addOption(OptionType.STRING, "provided_tag", "Optionally provided tag"),

                    Commands.slash("change_note_title", "Change the title of a note.")
                            .addOption(OptionType.INTEGER, "provided_id", "Provided ID.", true)
                            .addOption(OptionType.STRING, "provided_title", "New title.", true),

                    Commands.slash("change_note_summary", "Change the summary of a note.")
                            .addOption(OptionType.INTEGER, "provided_id", "Provided ID.", true)
                            .addOption(OptionType.STRING, "provided_summary", "New summary.", true),

                    Commands.slash("change_note_file", "Change the file of a note.")
                            .addOption(OptionType.INTEGER, "provided_id", "Provided ID.", true)
                            .addOption(OptionType.ATTACHMENT, "provided_attachment", "New file.", true),

                    // AI related commands
                    Commands.slash("generate_summary_by_id", "Request a summary of a note based on its database ID.")
                            .addOption(OptionType.INTEGER, "provided_id", "Provided ID", true)
                            .addOption(OptionType.STRING, "provided_prompt", "Optionally provided prompt (Default is a summary request.)")
            ).queue();
            courseToTagLinker.loadOnStartup();
            //courseToTagLinker.saveOnShutDown();
        }
    }
}
