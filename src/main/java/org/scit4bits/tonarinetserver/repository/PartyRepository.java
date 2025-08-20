package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyRepository extends JpaRepository<Party, Integer> {

}
