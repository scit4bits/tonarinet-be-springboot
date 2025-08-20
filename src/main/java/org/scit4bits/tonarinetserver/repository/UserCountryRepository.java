package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.UserCountry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCountryRepository extends JpaRepository<UserCountry, UserCountry.UserCountryId> {

}
