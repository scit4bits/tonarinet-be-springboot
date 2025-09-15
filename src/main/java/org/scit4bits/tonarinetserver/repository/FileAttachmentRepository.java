package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.FileAttachment;
import org.scit4bits.tonarinetserver.entity.FileAttachment.FileType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 파일 첨부(FileAttachment) 엔티티에 대한 데이터 액세스 작업을 처리하는 리포지토리
 */
@Repository
public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Integer> {

    /**
     * 특정 게시글 ID에 해당하는 모든 파일 첨부를 조회합니다.
     * @param articleId 게시글 ID
     * @return 파일 첨부 리스트
     */
    List<FileAttachment> findByArticleId(Integer articleId);

    /**
     * 특정 제출물 ID에 해당하는 모든 파일 첨부를 조회합니다.
     * @param submissionId 제출물 ID
     * @return 파일 첨부 리스트
     */
    List<FileAttachment> findBySubmissionId(Integer submissionId);

    /**
     * 특정 사용자가 업로드한 모든 파일 첨부를 조회합니다.
     * @param uploadedBy 업로드한 사용자 ID
     * @return 파일 첨부 리스트
     */
    List<FileAttachment> findByUploadedBy(Integer uploadedBy);

    /**
     * 특정 파일 타입에 해당하는 모든 파일 첨부를 조회합니다.
     * @param type 파일 타입
     * @return 파일 첨부 리스트
     */
    List<FileAttachment> findByType(FileType type);

    /**
     * 특정 게시글 ID와 파일 타입에 해당하는 모든 파일 첨부를 조회합니다.
     * @param articleId 게시글 ID
     * @param type 파일 타입
     * @return 파일 첨부 리스트
     */
    List<FileAttachment> findByArticleIdAndType(Integer articleId, FileType type);

    /**
     * 특정 제출물 ID와 파일 타입에 해당하는 모든 파일 첨부를 조회합니다.
     * @param submissionId 제출물 ID
     * @param type 파일 타입
     * @return 파일 첨부 리스트
     */
    List<FileAttachment> findBySubmissionIdAndType(Integer submissionId, FileType type);

    /**
     * 특정 사용자가 업로드한 파일 중 공개/비공개 설정에 따라 조회합니다.
     * @param uploadedBy 업로드한 사용자 ID
     * @param isPrivate 비공개 여부
     * @return 파일 첨부 리스트
     */
    List<FileAttachment> findByUploadedByAndIsPrivate(Integer uploadedBy, Boolean isPrivate);

    /**
     * 특정 게시글에 첨부된 파일 중 공개/비공개 설정에 따라 조회합니다.
     * @param articleId 게시글 ID
     * @param isPrivate 비공개 여부
     * @return 파일 첨부 리스트
     */
    List<FileAttachment> findByArticleIdAndIsPrivate(Integer articleId, Boolean isPrivate);

    /**
     * 특정 제출물에 첨부된 파일 중 공개/비공개 설정에 따라 조회합니다.
     * @param submissionId 제출물 ID
     * @param isPrivate 비공개 여부
     * @return 파일 첨부 리스트
     */
    List<FileAttachment> findBySubmissionIdAndIsPrivate(Integer submissionId, Boolean isPrivate);

    /**
     * 원본 파일 이름에 특정 문자열을 포함하는 파일 첨부를 페이징하여 조회합니다. (대소문자 무시)
     * @param filename 검색할 파일 이름 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 파일 첨부
     */
    Page<FileAttachment> findByOriginalFilenameContainingIgnoreCase(String filename, Pageable pageable);

    /**
     * 특정 사용자가 업로드한 파일 첨부를 페이징하여 조회합니다.
     * @param uploadedBy 업로드한 사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 파일 첨부
     */
    Page<FileAttachment> findByUploadedBy(Integer uploadedBy, Pageable pageable);

    /**
     * 특정 게시글에 첨부된 파일을 페이징하여 조회합니다.
     * @param articleId 게시글 ID
     * @param pageable 페이징 정보
     * @return 페이징된 파일 첨부
     */
    Page<FileAttachment> findByArticleId(Integer articleId, Pageable pageable);

    /**
     * 특정 제출물에 첨부된 파일을 페이징하여 조회합니다.
     * @param submissionId 제출물 ID
     * @param pageable 페이징 정보
     * @return 페이징된 파일 첨부
     */
    Page<FileAttachment> findBySubmissionId(Integer submissionId, Pageable pageable);

    /**
     * 특정 파일 타입의 파일 첨부를 페이징하여 조회합니다.
     * @param type 파일 타입
     * @param pageable 페이징 정보
     * @return 페이징된 파일 첨부
     */
    Page<FileAttachment> findByType(FileType type, Pageable pageable);

    /**
     * 공개/비공개 상태에 따라 파일 첨부를 페이징하여 조회합니다.
     * @param isPrivate 비공개 여부
     * @param pageable 페이징 정보
     * @return 페이징된 파일 첨부
     */
    Page<FileAttachment> findByIsPrivate(Boolean isPrivate, Pageable pageable);

    /**
     * 업로더 이름에 특정 문자열을 포함하는 파일 첨부를 페이징하여 조회합니다.
     * @param uploaderName 검색할 업로더 이름
     * @param pageable 페이징 정보
     * @return 페이징된 파일 첨부
     */
    @Query("SELECT f FROM FileAttachment f WHERE f.uploadedByUser.name LIKE %:uploaderName%")
    Page<FileAttachment> findByUploaderNameContaining(@Param("uploaderName") String uploaderName, Pageable pageable);

    /**
     * 게시글 제목에 특정 문자열을 포함하는 파일 첨부를 페이징하여 조회합니다.
     * @param articleTitle 검색할 게시글 제목
     * @param pageable 페이징 정보
     * @return 페이징된 파일 첨부
     */
    @Query("SELECT f FROM FileAttachment f WHERE f.article.title LIKE %:articleTitle%")
    Page<FileAttachment> findByArticleTitleContaining(@Param("articleTitle") String articleTitle, Pageable pageable);

    /**
     * 제출물 내용에 특정 문자열을 포함하는 파일 첨부를 페이징하여 조회합니다.
     * @param submissionContents 검색할 제출물 내용
     * @param pageable 페이징 정보
     * @return 페이징된 파일 첨부
     */
    @Query("SELECT f FROM FileAttachment f WHERE f.submission.contents LIKE %:submissionContents%")
    Page<FileAttachment> findBySubmissionContentsContaining(@Param("submissionContents") String submissionContents, Pageable pageable);

    /**
     * 모든 필드에서 검색어와 일치하는 파일 첨부를 페이징하여 조회합니다.
     * @param search 검색어
     * @param pageable 페이징 정보
     * @return 페이징된 파일 첨부
     */
    @Query("SELECT f FROM FileAttachment f WHERE " +
            "LOWER(f.originalFilename) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(f.filepath) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(f.uploadedByUser.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(f.article.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(f.submission.contents) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<FileAttachment> findByAllFieldsContaining(@Param("search") String search, Pageable pageable);

    /**
     * 특정 게시글의 첨부 파일 수를 계산합니다.
     * @param articleId 게시글 ID
     * @return 첨부 파일 수
     */
    long countByArticleId(Integer articleId);

    /**
     * 특정 제출물의 첨부 파일 수를 계산합니다.
     * @param submissionId 제출물 ID
     * @return 첨부 파일 수
     */
    long countBySubmissionId(Integer submissionId);

    /**
     * 특정 사용자가 업로드한 파일 수를 계산합니다.
     * @param uploadedBy 업로드한 사용자 ID
     * @return 업로드한 파일 수
     */
    long countByUploadedBy(Integer uploadedBy);

    /**
     * 특정 사용자가 최근에 업로드한 10개의 파일을 조회합니다.
     * @param uploadedBy 업로드한 사용자 ID
     * @return 최근 업로드 파일 리스트
     */
    List<FileAttachment> findTop10ByUploadedByOrderByUploadedAtDesc(Integer uploadedBy);
}
