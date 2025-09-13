package org.scit4bits.tonarinetserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.TranslationRequestDTO;
import org.scit4bits.tonarinetserver.dto.TranslationResponseDTO;
import org.scit4bits.tonarinetserver.service.GCPService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 공통 기능 관련 API를 처리하는 컨트롤러입니다.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/common")
public class CommonController {

    private final GCPService gcpService;

    /**
     * 텍스트를 번역합니다.
     * @param request 번역할 텍스트와 대상 언어 정보
     * @return 번역된 텍스트 정보
     */
    @PostMapping("/translate")
    public ResponseEntity<TranslationResponseDTO> postTranslation(@RequestBody TranslationRequestDTO request) {
        // GCPService를 통해 텍스트를 번역합니다.
        TranslationResponseDTO result = gcpService.translateText(request.getText(), request.getTargetLanguage());
        return ResponseEntity.ok(result);
    }

}
