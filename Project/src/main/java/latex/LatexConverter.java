package latex;
import logging.LoggerUtil;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LatexConverter {
    private final Logger logger = LoggerUtil.getLogger(LatexConverter.class);

    // Use RegEx to extract latex expressions from a string
    // returns an arraylist of strings, where a string may contain a normal string or a latex expression.
    public ArrayList<Object> extractLatexFromString(String string) {
        ArrayList<Object> result = new ArrayList<>();
        Pattern p = Pattern.compile(
                // Match $$...$$ for display math mode
                "(?<!\\\\)\\$\\$(.+?)\\$\\$"
                        + "|"
                        // Match $...$ for inline math mode
                        + "(?<!\\\\)(?<!\\d)\\$(?!\\d)(.+?)(?<!\\d)\\$(?!\\d)"
                        + "|"
                        // Match \(...\) for escaped parentheses
                        + "\\\\\\((.+?)\\\\\\)"
                        + "|"
                        // Match \[...] for escaped square brackets
                        + "\\\\\\[(.+?)\\\\]"
                        + "|"
                        // Match \textbf{...} for bold text
                        + "\\\\textbf\\{(.+?)}"
                        + "|"
                        // Match \frac{...}{...} for fractions
                        + "\\\\frac\\{(.+?)}\\{(.+?)}"
                        + "|"
                        // Match integrals with optional limits: \int_{lower}^{upper} integrand
                        + "\\\\int(?:_\\{(.+?)})?(?:\\^\\{(.+?)})?\\s*(.+?)"
                        + "|"
                        // Match Greek letters (like \alpha, \beta, etc.)
                        + "\\\\([a-zA-Z]+)"
                        + "|"
                        // Match common math symbols
                        + "\\\\(sum|prod|lim|infty|alpha|beta|gamma|delta|theta|pi|rho|sigma|lambda|mu|omega|phi|tau|chi|varphi|varepsilon|vartheta|varkappa|upsilon|xi|zeta)"
                        + "|"
                        // Match operators like \sqrt, \frac, \sum, \int, etc.
                        + "\\\\(sqrt|frac|sum|int|prod|lim|log|ln|sin|cos|tan|arcsin|arccos|arctan)"
                , Pattern.DOTALL
        );
        string = string.replaceAll("(?m)^```(?:latex|math)\\s*$", "");
        string = string.replaceAll("(?m)^```\\s*$", "");

        Matcher m = p.matcher(string);
        int lastIndex = 0;
        while (m.find()) {
            if (m.start() > lastIndex) {
                result.add(string.substring(lastIndex, m.start()));
            }
            result.add(convertLatexToImage(m.group()));
            lastIndex = m.end();
        }
        if (lastIndex < string.length()) {
            result.add(string.substring(lastIndex));
        }
        return result;
    }

    // Converts given latex expression into a File object
    // Discord bot will attach file and upload it
    private byte[] convertLatexToImage(String string) {
        try {
            TeXFormula teXFormula = new TeXFormula(string);
            TeXIcon teXIcon = teXFormula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 20);
            BufferedImage image = new BufferedImage(teXIcon.getIconWidth(), teXIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = image.createGraphics();
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            teXIcon.setForeground(Color.WHITE);
            teXIcon.paintIcon(null, g2d, 0, 0);
            g2d.dispose();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error converting latex to image", e);
        }
        return null;
    }
}
