package com.anicictina.backend.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GeminiClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(15))
        .build();

    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    public GeminiClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String generateJson(String prompt, Map<String, Object> responseSchema) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AIServiceException(
                "AI servis nije podešen (nedostaje GEMINI_API_KEY). Kontaktirajte administratora.");
        }

        Map<String, Object> requestBody = Map.of(
            "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
            "generationConfig", Map.of(
                "responseMimeType", "application/json",
                "responseSchema", responseSchema
            )
        );

        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();
        } catch (IOException e) {
            throw new AIServiceException("Neuspešno formiranje zahteva ka AI servisu.", e);
        }

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            log.error("Failed to reach Gemini API", e);
            throw new AIServiceException("AI servis trenutno nije dostupan. Pokušajte ponovo kasnije.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AIServiceException("Zahtev ka AI servisu je prekinut.", e);
        }

        if (response.statusCode() != 200) {
            log.error("Gemini API returned status {}: {}", response.statusCode(), response.body());
            throw new AIServiceException("AI servis je vratio grešku. Pokušajte ponovo kasnije.");
        }

        try {
            JsonNode root = objectMapper.readTree(response.body());
            return root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
        } catch (IOException e) {
            log.error("Failed to parse Gemini API response: {}", response.body(), e);
            throw new AIServiceException("Neuspešno čitanje odgovora AI servisa.", e);
        }
    }
}
