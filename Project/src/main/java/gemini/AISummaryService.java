package gemini;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

public class AISummaryService {
    private static final String MODEL_NAME = "gemini-2.5-flash-lite";
    private static final String SYSTEM_INSTRUCTION = "You are a Discord Bot named HiveNote.";
    public static String generateResponse(String prompt) {
        GenerateContentResponse response;
        try (Client client = new Client()) {
            GenerateContentConfig config = GenerateContentConfig
                    .builder()
                    .systemInstruction(Content.fromParts(Part.fromText(SYSTEM_INSTRUCTION)))
                    .build();
            response = client.models.generateContent(
                    MODEL_NAME,
                    prompt,
                    config);
        }
        return response.text();
    }
}

