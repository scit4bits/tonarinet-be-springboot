package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationResponseDTO {
    private String translatedText;
    private String sourceLanguage; // e.g., "en", "ko", "ja"
    private String targetLanguage; // e.g., "en", "ko", "ja"
}
