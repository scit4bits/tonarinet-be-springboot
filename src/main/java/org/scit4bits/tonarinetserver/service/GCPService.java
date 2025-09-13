package org.scit4bits.tonarinetserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.scit4bits.tonarinetserver.dto.TranslationResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
@RequiredArgsConstructor
public class GCPService {

    @Value("${google.translation.api.key}")
    private String googleTransApiKey;

    public TranslationResponseDTO translateText(String text, String targetLanguage) {
        // Use googleTransApiKey to call Google Translation API
        String target = targetLanguage == null ? "en" : targetLanguage;

        // call Google Translation API and return the translated text with WebClient

        WebClient webClient = WebClient.builder()
                .baseUrl("https://translation.googleapis.com/language/translate/v2")
                .build();

        String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("q", text)
                        .queryParam("target", target)
                        .queryParam("format", "text")
                        .queryParam("key", googleTransApiKey)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // JSON Parsing with json-simple
        String translatedText = text; // default fallback
        String detectedSourceLanguage = "unknown"; // default fallback

        try {
            // Using json-simple to parse the JSON response
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(response);

            // Navigate through the JSON structure: data -> translations -> [0] ->
            // translatedText
            JSONObject data = (JSONObject) jsonObject.get("data");
            if (data != null) {
                JSONArray translations = (JSONArray) data.get("translations");
                if (translations != null && !translations.isEmpty()) {
                    JSONObject firstTranslation = (JSONObject) translations.get(0);

                    // Extract translated text
                    Object translatedTextObj = firstTranslation.get("translatedText");
                    if (translatedTextObj != null) {
                        translatedText = translatedTextObj.toString();
                    }

                    // Extract detected source language (optional field)
                    Object detectedLanguageObj = firstTranslation.get("detectedSourceLanguage");
                    if (detectedLanguageObj != null) {
                        detectedSourceLanguage = detectedLanguageObj.toString();
                    }
                }
            }

            log.debug("Translation successful: '{}' -> '{}' (detected: {})", text, translatedText,
                    detectedSourceLanguage);

        } catch (Exception e) {
            log.error("Failed to parse Google Translation API response: {}", e.getMessage());
            log.debug("Raw response: {}", response);
            // Return original text as fallback
            translatedText = text;
        }

        return TranslationResponseDTO.builder()
                .translatedText(translatedText)
                .sourceLanguage(detectedSourceLanguage)
                .targetLanguage(target)
                .build();
    }
}
