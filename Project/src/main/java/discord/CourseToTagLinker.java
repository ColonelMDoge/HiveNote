package discord;

import database.DatabaseServiceHandler;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

// Preloads stored courses and tags for faster retrieval than constant DB querying
public class CourseToTagLinker implements Serializable {
    private final HashMap<String, Set<String>> courseToTag;

    public CourseToTagLinker() {
        DatabaseServiceHandler dsh = new DatabaseServiceHandler();
        courseToTag = dsh.preLoadData();
    }

    public boolean courseCodeDNE(String code) {
        return !courseToTag.containsKey(code.toUpperCase());
    }

    public boolean tagsDNE(String code) {
        return courseToTag.get(code.toUpperCase()).isEmpty();
    }

    public StringSelectMenu.Builder getTagsAsSSM(String course) {
        StringSelectMenu.Builder ssm = StringSelectMenu.create(course.toUpperCase() + "_tags")
                .setRequiredRange(1, StringSelectMenu.OPTIONS_MAX_AMOUNT);
        courseToTag.get(course.toUpperCase()).forEach(tag -> ssm.addOption(tag.toUpperCase(), tag.toUpperCase()));
        return ssm;
    }

    public StringSelectMenu.Builder getCoursesAsSSM() {
        StringSelectMenu.Builder ssm = StringSelectMenu.create("courses_ssm")
                .setRequiredRange(1,1);
        getCoursesAsSet().forEach(c -> ssm.addOption(c.toUpperCase(), c.toUpperCase()));
        return ssm;
    }

    public Set<String> getTagsAsSet(String course) {
        return courseToTag.get(course.toUpperCase());
    }

    public Set<String> getCoursesAsSet() {
        return courseToTag.keySet();
    }

    public void addCourseCode(String code) {
        String courseCode = code.toUpperCase();
        courseToTag.put(courseCode, new HashSet<>());
    }

    public void removeCourseCode(String code) {
        String courseCode = code.toUpperCase();
        courseToTag.remove(courseCode);
    }

    public void addTag(String code, String tag) {
        String courseCode = code.toUpperCase(), tagName = tag.toUpperCase();
        courseToTag.get(courseCode).add(tagName);
    }

    public void removeTag(String code, String tag) {
        String courseCode = code.toUpperCase(), tagName = tag.toUpperCase();
        courseToTag.get(courseCode).remove(tagName);
    }
}
