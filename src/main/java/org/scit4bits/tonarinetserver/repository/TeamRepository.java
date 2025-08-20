package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Integer> {

}
