package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.UserParty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPartyRepository extends JpaRepository<UserParty, UserParty.UserPartyId> {

}
