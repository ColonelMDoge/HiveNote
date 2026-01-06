package database;

import logging.LoggerUtil;
import net.dv8tion.jda.api.entities.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AttachmentConvertor {
    private static final Logger logger = LoggerUtil.getLogger(AttachmentConvertor.class);
    public static byte[] convertToBytes(Message.Attachment attachment) {
        try {
            return attachment.getProxy().download().thenApply(response -> {
                try (response) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = response.read(buffer, 0, buffer.length)) != -1) {
                        baos.write(buffer, 0, read);
                    }
                    logger.info("Attachment successfully converted to a byte array");
                    return baos.toByteArray();
                } catch (IOException e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
                return null;
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }
}
