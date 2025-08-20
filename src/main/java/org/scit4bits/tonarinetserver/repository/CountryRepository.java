package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository extends JpaRepository<Country, String> {

}
