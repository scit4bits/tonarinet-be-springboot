package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.UserCountry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 사용자-국가(UserCountry) 엔티티에 대한 데이터 액세스 작업을 처리하는 리포지토리
 */
@Repository
public interface UserCountryRepository extends JpaRepository<UserCountry, UserCountry.UserCountryId> {

}
