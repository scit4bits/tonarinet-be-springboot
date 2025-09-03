package org.scit4bits.tonarinetserver.service;

import org.scit4bits.tonarinetserver.entity.UserCountry;
import org.scit4bits.tonarinetserver.repository.UserCountryRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCountryService {
    private final UserCountryRepository userCountryRepository;
    
    public Boolean checkUserCountryAccess(Integer userId, String countryCode) {
        UserCountry.UserCountryId userCountryId = UserCountry.UserCountryId.builder().userId(userId).countryCode(countryCode).build();
        return userCountryRepository.existsById(userCountryId);
    }
}
