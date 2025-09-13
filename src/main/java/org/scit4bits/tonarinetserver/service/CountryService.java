package org.scit4bits.tonarinetserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.CountryResponseDTO;
import org.scit4bits.tonarinetserver.repository.CountryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CountryService {

    private final CountryRepository countryRepository;

    public List<CountryResponseDTO> getAllCountries() {
        log.debug("Retrieving all countries");
        return countryRepository.findAll().stream()
                .map(CountryResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
