package discord;

import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;

import java.util.HashMap;

public class CourseToTagLinker {
    private static final HashMap<String, StringSelectMenu.Builder> courseToTag = new HashMap<>();

    public static StringSelectMenu.Builder getCoursesAsSSM() {
        StringSelectMenu.Builder ssm = StringSelectMenu.create("course_code_menu");
        for (String course : courseToTag.keySet()) {
            ssm.addOption(course.toUpperCase(), course.toUpperCase());
        }
        return ssm;
    }

    public static StringSelectMenu.Builder getTagsAsSSM(String course) {
        return courseToTag.get(course.toUpperCase());
    }

    public static boolean addCourseCode(String code) {
        String courseCode = code.toUpperCase();
        if (courseToTag.containsKey(courseCode)) return false;
        courseToTag.put(courseCode, StringSelectMenu.create(courseCode).setRequiredRange(1, StringSelectMenu.OPTIONS_MAX_AMOUNT));
        return true;
    }

    public static boolean removeCourseCode(String code) {
        String courseCode = code.toUpperCase();
        if (!courseToTag.containsKey(courseCode)) return false;
        courseToTag.remove(courseCode);
        return true;
    }

    public static boolean addTag(String code, String tag) {
        String courseCode = code.toUpperCase(), tagName = tag.toUpperCase();
        if (!courseToTag.containsKey(courseCode)) return false;
        StringSelectMenu.Builder menu = courseToTag.get(courseCode);
        if (menu.getOptions().contains(SelectOption.of(tagName, tagName))) return false;
        menu.getOptions().add(SelectOption.of(tagName, tagName));
        return true;
    }

    public static boolean removeTag(String code, String tag) {
        String courseCode = code.toUpperCase(), tagName = tag.toUpperCase();
        if (!courseToTag.containsKey(courseCode)) return false;
        StringSelectMenu.Builder menu = courseToTag.get(courseCode);
        if (!menu.getOptions().contains(SelectOption.of(tagName, tagName))) return false;
        menu.getOptions().remove(SelectOption.of(tagName, tagName));
        return true;
    }

}
