package org.scit4bits.tonarinetserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.entity.UserCountry;
import org.scit4bits.tonarinetserver.repository.UserCountryRepository;
import org.springframework.stereotype.Service;

/**
 * 사용자의 국가 접근 권한 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCountryService {
    private final UserCountryRepository userCountryRepository;

    /**
     * 사용자가 특정 국가에 접근할 수 있는지 확인합니다.
     * @param userId 사용자 ID
     * @param countryCode 국가 코드
     * @return 접근 가능 여부
     */
    public Boolean checkUserCountryAccess(Integer userId, String countryCode) {
        UserCountry.UserCountryId userCountryId = UserCountry.UserCountryId.builder().userId(userId).countryCode(countryCode).build();
        return userCountryRepository.existsById(userCountryId);
    }
}
