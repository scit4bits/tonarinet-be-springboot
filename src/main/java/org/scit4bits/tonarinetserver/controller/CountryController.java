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

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/country")
public class CountryController {

    private final CountryService countryService;

    @GetMapping
    public ResponseEntity<List<CountryResponseDTO>> getAllCountries() {
        log.info("GET /api/country - Retrieving all countries");
        List<CountryResponseDTO> countries = countryService.getAllCountries();
        return ResponseEntity.ok(countries);
    }
}
