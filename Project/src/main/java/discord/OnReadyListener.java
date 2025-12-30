package discord;

import logging.LoggerUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.io.*;
import java.util.logging.Logger;

public class OnReadyListener extends ListenerAdapter {
    private final Logger logger = LoggerUtil.getLogger(OnReadyListener.class);
    private final CourseToTagLinker courseToTagLinker;
    private final SlashCommandListener slashCommandListener;

    public OnReadyListener(CourseToTagLinker courseToTagLinker, SlashCommandListener slashCommandListener) {
        this.courseToTagLinker = courseToTagLinker;
        this.slashCommandListener = slashCommandListener;
    }

    public void onReady(ReadyEvent event) {
        logger.info("The Discord bot is ready.");
        JDA jda = event.getJDA();
        slashCommandListener.setJDA(jda);
        Guild testGuild = jda.getGuildById("1127969348049457295");
        if (testGuild != null) {
            testGuild.updateCommands().addCommands(
                    Commands.slash("upload", "Upload a file to the database.")
                            .addOption(OptionType.STRING, "asked_course", "Course code related to file.", true),

                    Commands.slash("ask", "Prompt you want to ask.")
                            .addOption(OptionType.STRING, "asked_prompt", "Prompt you want to ask.", true),

                    Commands.slash("help", "Get a list of commands."),

                    Commands.slash("create_tag", "Create a tag.")
                            .addOption(OptionType.STRING, "provided_course", "Course code relating to the tag.", true)
                            .addOption(OptionType.STRING, "created_tag", "Tag you want to create.", true),

                    Commands.slash("delete_tag", "Delete a tag.")
                            .addOption(OptionType.STRING, "provided_course", "Course code relating to the tag.", true)
                            .addOption(OptionType.STRING, "deleted_tag", "Delete a tag that exists.", true),

                    Commands.slash("create_course_code", "Create a course code.")
                            .addOption(OptionType.STRING, "created_course", "Course code you want to create.", true),

                    Commands.slash("delete_course_code", "Delete a course code.")
                            .addOption(OptionType.STRING, "deleted_course", "Delete a course code that exists.", true),

                    Commands.slash("retrieve_by_id", "Retrieve a note based on its database ID.")
                            .addOption(OptionType.STRING, "provided_id", "Provided ID.", true)
            ).queue();
            courseToTagLinker.loadOnStartup();
            courseToTagLinker.saveOnShutDown();
        }
    }
}
