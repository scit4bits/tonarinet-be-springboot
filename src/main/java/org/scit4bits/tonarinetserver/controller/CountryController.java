package org.scit4bits.tonarinetserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.CountryResponseDTO;
import org.scit4bits.tonarinetserver.service.CountryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 국가 정보 관련 API를 처리하는 컨트롤러입니다.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/country")
public class CountryController {

    private final CountryService countryService;

    /**
     * 모든 국가 정보를 조회합니다.
     * @return CountryResponseDTO 리스트
     */
    @GetMapping
    public ResponseEntity<List<CountryResponseDTO>> getAllCountries() {
        log.info("GET /api/country - 모든 국가 정보를 조회합니다.");
        List<CountryResponseDTO> countries = countryService.getAllCountries();
        return ResponseEntity.ok(countries);
    }
}
