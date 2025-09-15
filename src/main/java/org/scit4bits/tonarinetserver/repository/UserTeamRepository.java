package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.UserTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 사용자-팀(UserTeam) 엔티티에 대한 데이터 액세스 작업을 처리하는 리포지토리
 */
@Repository
public interface UserTeamRepository extends JpaRepository<UserTeam, UserTeam.UserTeamId> {

    /**
     * 특정 사용자가 속한 모든 팀-사용자 관계를 조회합니다.
     * @param userId 사용자 ID
     * @return UserTeam 리스트
     */
    List<UserTeam> findByIdUserId(Integer userId);
}
