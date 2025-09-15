package org.scit4bits.tonarinetserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.CountryResponseDTO;
import org.scit4bits.tonarinetserver.repository.CountryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 국가 정보 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CountryService {

    private final CountryRepository countryRepository;

    /**
     * 모든 국가 정보를 조회합니다.
     * @return CountryResponseDTO 리스트
     */
    public List<CountryResponseDTO> getAllCountries() {
        log.debug("모든 국가 정보를 조회합니다.");
        return countryRepository.findAll().stream()
                .map(CountryResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
