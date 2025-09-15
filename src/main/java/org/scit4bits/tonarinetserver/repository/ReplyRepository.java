package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.Reply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 댓글(Reply) 엔티티에 대한 데이터 액세스 작업을 처리하는 리포지토리
 */
@Repository
public interface ReplyRepository extends JpaRepository<Reply, Integer> {

    /**
     * ID로 댓글을 페이징하여 조회합니다.
     * @param id 댓글 ID
     * @param pageable 페이징 정보
     * @return 페이징된 댓글
     */
    Page<Reply> findById(Integer id, Pageable pageable);

    /**
     * 내용에 특정 문자열을 포함하는 댓글을 페이징하여 조회합니다. (대소문자 무시)
     * @param contents 검색할 내용 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 댓글
     */
    Page<Reply> findByContentsContainingIgnoreCase(String contents, Pageable pageable);

    /**
     * 작성자 ID로 댓글을 페이징하여 조회합니다.
     * @param createdById 작성자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 댓글
     */
    Page<Reply> findByCreatedById(Integer createdById, Pageable pageable);

    /**
     * 게시글 ID로 댓글을 페이징하여 조회합니다.
     * @param articleId 게시글 ID
     * @param pageable 페이징 정보
     * @return 페이징된 댓글
     */
    Page<Reply> findByArticleId(Integer articleId, Pageable pageable);

    /**
     * 특정 게시글의 모든 댓글을 생성 시간 오름차순으로 조회합니다.
     * @param articleId 게시글 ID
     * @return 댓글 리스트
     */
    List<Reply> findByArticleIdOrderByCreatedAtAsc(Integer articleId);

    /**
     * 모든 필드(내용)에서 검색어와 일치하는 댓글을 페이징하여 조회합니다.
     * @param search 검색어
     * @param pageable 페이징 정보
     * @return 페이징된 댓글
     */
    @Query("SELECT r FROM Reply r WHERE " +
            "LOWER(r.contents) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Reply> findByAllFieldsContaining(@Param("search") String search, Pageable pageable);
}
