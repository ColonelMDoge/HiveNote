package discord;

import logging.LoggerUtil;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CourseToTagLinker implements Serializable {
    private final Logger logger = LoggerUtil.getLogger(CourseToTagLinker.class);
    private HashMap<String, Set<String>> courseToTag;

    public boolean isEmpty() {
        return courseToTag.isEmpty();
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
        for (String tag : courseToTag.get(course.toUpperCase())) {
            ssm.addOption(tag.toUpperCase(), tag.toUpperCase());
        }
        return ssm;
    }

    public boolean addCourseCode(String code) {
        String courseCode = code.toUpperCase();
        if (courseToTag.containsKey(courseCode)) return false;
        courseToTag.put(courseCode, new HashSet<>());
        return true;
    }

    public boolean removeCourseCode(String code) {
        String courseCode = code.toUpperCase();
        if (!courseToTag.containsKey(courseCode)) return false;
        courseToTag.remove(courseCode);
        return true;
    }

    public boolean addTag(String code, String tag) {
        String courseCode = code.toUpperCase(), tagName = tag.toUpperCase();
        if (!courseToTag.containsKey(courseCode)) return false;
        return courseToTag.get(courseCode).add(tagName);
    }

    public boolean removeTag(String code, String tag) {
        String courseCode = code.toUpperCase(), tagName = tag.toUpperCase();
        if (!courseToTag.containsKey(courseCode)) return false;
        return courseToTag.get(courseCode).remove(tagName);
    }

    public void saveOnShutDown() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("The bot is shutting down.");
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("src/main/java/data/courseTagMap.ser"))) {
                oos.writeObject(courseToTag);
                logger.info("Course and tag contents successfully saved to serialized file.");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "There was an error attempting to save the serialized file!", e);
            }
            for (Handler handler : logger.getHandlers()) {
                handler.close();
            }
        }));
    }

    @SuppressWarnings("unchecked")
    public void loadOnStartup() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("src/main/java/data/courseTagMap.ser"))) {
            courseToTag = (HashMap<String, Set<String>>) ois.readObject();
            if (courseToTag == null) courseToTag = new HashMap<>();
            logger.info("Course and tag contents successfully loaded to map object.");
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "There was an error attempting to load the serialized file!", e);
        }
    }
}
