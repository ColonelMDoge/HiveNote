package latex;
import logging.LoggerUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LatexConverter {
    private final Logger logger = LoggerUtil.getLogger(LatexConverter.class);

    // Converts given latex expression into a byte array
    // Discord bot will attach file and upload it
    public byte[] convertStringToLatex(String string) {
        String base = "src/main/java/latex";
        ProcessBuilder pb = new ProcessBuilder("node", "render.js");
        pb.directory(Path.of(base, "render.js").toAbsolutePath().getParent().toFile());

        Process process;
        try {
            String html = Files.readString(Path.of(base,"template.html")).replace("<!-- MARKDOWN_HERE -->", string);
            Files.writeString(Path.of(base, "output.html"), html);

            process = pb.start();
            String stdout = new String(process.getInputStream().readAllBytes());
            String stderr = new String(process.getErrorStream().readAllBytes());

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                Path path = Path.of(base,"equation.png");
                return Files.readAllBytes(path);
            } else {
                logger.log(Level.WARNING,"render.js failed with code: " + exitCode);
                logger.warning("[render.js stdout:] " + stdout);
                logger.warning("[render.js stderr:] " + stderr);
            }

        } catch (IOException | InterruptedException e) {
            logger.log(Level.SEVERE, "Error converting latex to image", e);
        }
        return null;
    }
}
