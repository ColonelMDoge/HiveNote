package discord;

import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;

public class StringSelectMenuManager{
    private static final StringSelectMenu.Builder courseCodeMenu = StringSelectMenu.create("course_code_menu");
    private static final StringSelectMenu.Builder tagMenu = StringSelectMenu.create("tag_menu")
            .setRequiredRange(1, StringSelectMenu.OPTIONS_MAX_AMOUNT);

    public static StringSelectMenu.Builder getCourseCodeMenu() {
        return courseCodeMenu;
    }

    public static StringSelectMenu.Builder getTagMenu() {
        return tagMenu;
    }

    public static boolean addCourseCode(String code) {
        SelectOption option = SelectOption.of(code.toUpperCase(), code.toUpperCase());
        if (courseCodeMenu.getOptions().contains(option)) {
            return false;
        }
        courseCodeMenu.addOptions(option);
        return true;
    }

    public static boolean addTag(String tag) {
        SelectOption option = SelectOption.of(tag.toUpperCase(), tag.toUpperCase());
        if (tagMenu.getOptions().contains(option)) {
            return false;
        }
        tagMenu.addOptions(option);
        return true;
    }

    public static boolean removeCourseCode(String code) {
        SelectOption option = SelectOption.of(code.toUpperCase(), code.toUpperCase());
        if (courseCodeMenu.getOptions().contains(option)) {
            courseCodeMenu.getOptions().remove(option);
            return true;
        }
        return false;
    }

    public static boolean removeTag(String tag) {
        SelectOption option = SelectOption.of(tag.toUpperCase(), tag.toUpperCase());
        if (tagMenu.getOptions().contains(option)) {
            tagMenu.getOptions().remove(option);
            return true;
        }
        return false;
    }
}
