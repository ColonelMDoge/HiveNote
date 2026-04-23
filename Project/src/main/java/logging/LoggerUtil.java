package logging;

import java.io.IOException;
import java.util.logging.*;

public class LoggerUtil {


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
                    "%1$tF %1$tT [%2$s] %3$s.%4$s: %5$s %6$s%n",
                    record.getMillis(),
                    record.getLevel(),
                    record.getSourceClassName(),
                    record.getSourceMethodName(),
                    formatMessage(record),
                    (record.getThrown() != null ? record.getThrown() : "")
            );
        }
    }

    public static class MyLogManager extends LogManager {
        static MyLogManager instance;
        public MyLogManager() { instance = this; }
        @Override public void reset() {}
        private void reset0() { super.reset(); }
        public static void resetFinally() { instance.reset0(); }
    }

    private static FileHandler globalFH;
    public static void setupLogging() {
        try {
            globalFH = new FlushFileHandler("src/main/java/logging/latest.log", false);
            Formatter fmt = new MyFormatter();
            globalFH.setFormatter(fmt);
            globalFH.setLevel(Level.ALL);

            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(fmt);
            consoleHandler.setLevel(Level.ALL);

            Logger logger = LoggerUtil.getLogger(LoggerUtil.class);
            logger.setLevel(Level.ALL);

            for (Handler h : logger.getHandlers()) {
                logger.removeHandler(h);
            }

            logger.addHandler(globalFH);
            logger.addHandler(consoleHandler);
            logger.info("Logging started.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Logger getLogger(Class<?> providedClass) {
        Logger logger = Logger.getLogger(providedClass.getName());
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        logger.addHandler(globalFH);
        return logger;
    }
}
