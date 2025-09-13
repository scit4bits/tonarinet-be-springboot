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

/**
 * Google Cloud Platform 서비스 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GCPService {

    @Value("${google.translation.api.key}")
    private String googleTransApiKey;

    /**
     * 텍스트를 지정된 언어로 번역합니다.
     * @param text 번역할 텍스트
     * @param targetLanguage 대상 언어 코드 (e.g., "en", "ko")
     * @return 번역 결과를 담은 DTO
     */
    public TranslationResponseDTO translateText(String text, String targetLanguage) {
        // 대상 언어가 지정되지 않은 경우 영어로 기본 설정
        String target = targetLanguage == null ? "en" : targetLanguage;

        // WebClient를 사용하여 Google Translation API 호출
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

        // json-simple을 사용한 JSON 파싱
        String translatedText = text; // 기본 fallback 값
        String detectedSourceLanguage = "unknown"; // 기본 fallback 값

        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(response);

            // JSON 구조 탐색: data -> translations -> [0] -> translatedText
            JSONObject data = (JSONObject) jsonObject.get("data");
            if (data != null) {
                JSONArray translations = (JSONArray) data.get("translations");
                if (translations != null && !translations.isEmpty()) {
                    JSONObject firstTranslation = (JSONObject) translations.get(0);

                    // 번역된 텍스트 추출
                    Object translatedTextObj = firstTranslation.get("translatedText");
                    if (translatedTextObj != null) {
                        translatedText = translatedTextObj.toString();
                    }

                    // 감지된 소스 언어 추출 (선택적 필드)
                    Object detectedLanguageObj = firstTranslation.get("detectedSourceLanguage");
                    if (detectedLanguageObj != null) {
                        detectedSourceLanguage = detectedLanguageObj.toString();
                    }
                }
            }

            log.debug("번역 성공: '{}' -> '{}' (감지된 언어: {})", text, translatedText,
                    detectedSourceLanguage);

        } catch (Exception e) {
            log.error("Google Translation API 응답 파싱 실패: {}", e.getMessage());
            log.debug("원본 응답: {}", response);
            // 실패 시 원본 텍스트를 반환
            translatedText = text;
        }

        return TranslationResponseDTO.builder()
                .translatedText(translatedText)
                .sourceLanguage(detectedSourceLanguage)
                .targetLanguage(target)
                .build();
    }
}
