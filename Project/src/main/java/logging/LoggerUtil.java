package logging;

import java.io.IOException;
import java.util.logging.*;

public class LoggerUtil {

    private static FileHandler fileHandler;

    private static class FlushFileHandler extends FileHandler {
        public FlushFileHandler(String pattern, boolean append) throws IOException {
            super(pattern, append);
        }

        @Override
        public synchronized void publish(LogRecord record) {
            super.publish(record);
            flush();
        }
    }

    private static class MyFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            return String.format(
                    "%1$tF %1$tT [%2$s] %3$s: %4$s%n",
                    record.getMillis(),
                    record.getLevel(),
                    record.getLoggerName(),
                    formatMessage(record)
            );
        }
    }

    public static void setupLogging() {
        try {
            fileHandler = new FlushFileHandler("src/main/java/logging/latest.log", false);
            fileHandler.setFormatter(new MyFormatter());
            fileHandler.setLevel(Level.INFO);

            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new MyFormatter());
            consoleHandler.setLevel(Level.INFO);

            Logger root = Logger.getLogger("");
            root.setLevel(Level.INFO);

            for (Handler h : root.getHandlers()) {
                root.removeHandler(h);
            }

            root.addHandler(fileHandler);
            root.addHandler(consoleHandler);
            root.info("Logging started.");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    fileHandler.flush();
                    fileHandler.close();
                } catch (Exception ignored) {}
            }));

        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize logging!", e);
        }
    }

    public static Logger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }
}