package discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.io.*;
import java.util.Arrays;

public class OnReadyListener extends ListenerAdapter {
    public void onReady(ReadyEvent event) {
        System.out.println("The bot is ready!");
        JDA jda = event.getJDA();
        Guild testGuild = jda.getGuildById("1127969348049457295");
        if (testGuild != null) {
            testGuild.updateCommands().addCommands(
                    Commands.slash("upload", "Upload a file to the database."),

                    Commands.slash("ask", "Prompt you want to ask.")
                            .addOption(OptionType.STRING, "asked_prompt", "Prompt you want to ask.", true),

                    Commands.slash("help", "Get a list of commands."),

                    Commands.slash("create_tag", "Create a tag.")
                            .addOption(OptionType.STRING, "created_tag", "Tag you want to create.", true),

                    Commands.slash("delete_tag", "Delete a tag.")
                            .addOption(OptionType.STRING, "deleted_tag", "Delete a tag that exists.", true),

                    Commands.slash("create_course_code", "Create a course code.")
                            .addOption(OptionType.STRING, "created_course", "Course code you want to create.", true),

                    Commands.slash("delete_course_code", "Delete a course code.")
                            .addOption(OptionType.STRING, "deleted_course", "Delete a course code that exists.", true)
            ).queue();
            load();
            saveOnShutDown();
        }
    }

    private void saveOnShutDown() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("The bot is shutting down!");
            try (FileWriter fileWriter = new FileWriter("src/main/resources/saved_tags.txt")) {
                StringSelectMenu.Builder courses = StringSelectMenuManager.getCourseCodeMenu();
                StringSelectMenu.Builder tags = StringSelectMenuManager.getTagMenu();
                if (courses.getOptions().isEmpty() && tags.getOptions().isEmpty()) return;

                StringBuilder builder = new StringBuilder();
                for (SelectOption selectOption : courses.getOptions()) {
                    builder.append(selectOption.getValue()).append(",");
                }
                builder.append("\n");
                for (SelectOption selectOption : tags.getOptions()) {
                    builder.append(selectOption.getValue()).append(",");
                }
                builder.append("\n");
                fileWriter.write(builder.toString());
                System.out.println("Course codes and tags saved!");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }));
    }
    private void load() {
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/saved_tags.txt"))) {
            String courseCodeLine = br.readLine(), tagLine = br.readLine();
            if (courseCodeLine == null || tagLine == null) return;

            String[] courseCodes = courseCodeLine.split(",");
            Arrays.sort(courseCodes);
            String[] tags = tagLine.split(",");
            Arrays.sort(tags);

            for (String courseCode : courseCodes) {
                StringSelectMenuManager.addCourseCode(courseCode);
            }
            for (String tag : tags) {
                StringSelectMenuManager.addTag(tag);
            }
            System.out.println("Course codes and tags loaded!");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
