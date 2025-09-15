package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 번역 응답을 위한 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationResponseDTO {
    private String translatedText;
    private String sourceLanguage; // 예: "en", "ko", "ja"
    private String targetLanguage; // 예: "en", "ko", "ja"
}
