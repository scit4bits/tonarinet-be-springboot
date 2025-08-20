package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepository extends JpaRepository<Region, Integer> {

}
