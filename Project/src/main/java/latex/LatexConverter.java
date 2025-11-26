package latex;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LatexConverter {

    // Use RegEx to extract latex expressions from a string
    // returns an arraylist of strings, where a string may contain a normal string or a latex expression.
    public static ArrayList<String> extractLatexFromString(String string) {
        ArrayList<String> result = new ArrayList<>();
        Pattern p = Pattern.compile(
                "(?<!\\\\)\\$\\$(.+?)\\$\\$"
                        + "|"
                        + "(?<!\\\\)(?<!\\d)\\$(?!\\d)(.+?)(?<!\\d)\\$(?!\\d)"
                        + "|"
                        + "\\\\\\((.+?)\\\\\\)"
                        + "|"
                        + "\\\\\\[(.+?)\\\\]",
                Pattern.DOTALL
        );
        Matcher m = p.matcher(string);
        int lastIndex = 0;
        while (m.find()) {
            if (m.start() > lastIndex) {
                result.add(string.substring(lastIndex, m.start()));
            }
            result.add(m.group());
            lastIndex = m.end();
        }
        if (lastIndex < string.length()) {
            result.add(string.substring(lastIndex));
        }
        for (String e : result) {
            System.out.println(e);
        }
        return result;
    }

    // Converts given latex expression into a File object
    // Discord bot will attach file and upload it
    public static File convertLatexToImage(String string) {
        File file = new File("latex/tempFile.png");
        TeXFormula teXFormula = new TeXFormula(string);
        TeXIcon teXIcon = teXFormula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 20);
        BufferedImage image = new BufferedImage(teXIcon.getIconWidth(), teXIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.BLACK);
        teXIcon.paintIcon(null, g2d, 0, 0);
        g2d.dispose();
        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }
}
