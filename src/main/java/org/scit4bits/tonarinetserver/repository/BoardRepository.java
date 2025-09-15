package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 게시판(Board) 엔티티에 대한 데이터 액세스 작업을 처리하는 리포지토리
 */
@Repository
public interface BoardRepository extends JpaRepository<Board, Integer> {

    /**
     * 특정 조직 ID에 속한 모든 게시판을 조회합니다.
     * @param orgId 조직 ID
     * @return 게시판 리스트
     */
    List<Board> findByOrgId(int orgId);

    /**
     * 특정 국가 코드에 속한 모든 게시판을 조회합니다.
     * @param countryCode 국가 코드
     * @return 게시판 리스트
     */
    List<Board> findByCountryCode(String countryCode);

}
