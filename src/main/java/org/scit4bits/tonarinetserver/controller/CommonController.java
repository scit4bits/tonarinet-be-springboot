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

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/common")
public class CommonController {

    private final GCPService gcpService;

    @PostMapping("/translate")
    public ResponseEntity<TranslationResponseDTO> postTranslation(@RequestBody TranslationRequestDTO request) {
        TranslationResponseDTO result = gcpService.translateText(request.getText(), request.getTargetLanguage());
        return ResponseEntity.ok(result);
    }

}
