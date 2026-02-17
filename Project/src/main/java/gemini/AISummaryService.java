package gemini;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import logging.LoggerUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
    public String generateSummary(String prompt, List<byte[]> data) {
        GenerateContentResponse response;
        try (Client client = new Client()) {
            Content.Builder content = Content.builder();
            content.parts(Part.fromText(prompt));
            for (byte[] b : data) {
                content.parts(Part.fromBytes(b, URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(b))));
            }
            response = client.models.generateContent(MODEL_NAME, content.build(), config);
            System.out.println(prompt);
            return response.text();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to generate a summary!", e);
        }
        return null;
    }
}

