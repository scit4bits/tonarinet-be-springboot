package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.UserTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTeamRepository extends JpaRepository<UserTeam, UserTeam.UserTeamId> {

    List<UserTeam> findByIdUserId(Integer userId);
}
