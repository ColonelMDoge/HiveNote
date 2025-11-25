package gemini;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

public class AISummaryService {
    GenerateContentResponse response;
    public String generateSummary() {
        try (Client client = new Client()) {
            GenerateContentConfig config = GenerateContentConfig
                    .builder()
                    .systemInstruction(Content.fromParts(Part.fromText("You are a Discord Bot named HiveNote.")))
                    .build();
            response = client.models.generateContent(
                    "gemini-2.5-flash-lite",
                    "Who are you",
                    config);
        }
        return response.text();
    }
    public static String generateResponse(String prompt) {
        GenerateContentResponse response;
        try (Client client = new Client()) {
            GenerateContentConfig config = GenerateContentConfig
                    .builder()
                    .systemInstruction(Content.fromParts(Part.fromText("You are a Discord Bot named HiveNote.")))
                    .build();
            response = client.models.generateContent(
                    "gemini-2.5-flash-lite",
                    prompt,
                    config);
        }
        return response.text();
    }
}

