package gemini;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import logging.LoggerUtil;
import java.io.ByteArrayInputStream;
import java.net.URLConnection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AISummaryService {
    private final Logger logger = LoggerUtil.getLogger(AISummaryService.class);
    final String MODEL_NAME = "gemini-2.5-flash-lite";
    final String SYSTEM_INSTRUCTION = "You are a Discord Bot named HiveNote.";
    final GenerateContentConfig config = GenerateContentConfig
            .builder()
            .systemInstruction(Content.fromParts(Part.fromText(SYSTEM_INSTRUCTION)))
            .build();

    // Used for the /ask command
    public String generateResponse(String prompt) {
        GenerateContentResponse response;
        try (Client client = new Client()) {
            response = client.models.generateContent(MODEL_NAME, prompt, config);
            return response.text();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to generate a response!", e);
        }
        return null;
    }

    // Used to summarize notes
    public String generateSummary(byte[] data) {
        try (Client client = new Client()) {
            String mimeType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(data));
            if (mimeType == null) mimeType = "application/pdf";
            List<Part> parts = List.of(
                    Part.fromText("Please provide a concise summary of this document."),
                    Part.fromBytes(data, mimeType)
            );
            GenerateContentResponse response = client.models.generateContent(
                    MODEL_NAME,
                    Content.builder().parts(parts).build(),
                    config
            );
            return response.text();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to generate a summary!", e);
        }
        return null;
    }
}

