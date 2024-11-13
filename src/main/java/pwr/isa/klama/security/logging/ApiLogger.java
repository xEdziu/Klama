package pwr.isa.klama.security.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Date;
import java.text.SimpleDateFormat;

public class ApiLogger {
    private static final Logger logger = Logger.getLogger(ApiLogger.class.getName());
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    static {
        try {
            // Ensure the logs directory exists
            Files.createDirectories(Paths.get("logs"));
            String date = dateFormat.format(new Date());
            FileHandler fileHandler = new FileHandler("logs/api-log-" + date + ".log", true);
            fileHandler.setFormatter(new CustomFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to initialize logger handler.", e);
        }
    }

    public static void logInfo(String apiEndpoint, String description) {
        logger.log(Level.INFO, apiEndpoint + ":" + description);
    }

    public static void logWarning(String apiEndpoint, String description) {
        logger.log(Level.WARNING, apiEndpoint + ":" + description);
    }

    public static void logSevere(String apiEndpoint, String description) {
        logger.log(Level.SEVERE, apiEndpoint + ":" + description);
    }
}