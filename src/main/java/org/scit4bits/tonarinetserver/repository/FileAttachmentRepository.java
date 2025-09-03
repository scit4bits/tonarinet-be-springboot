package org.scit4bits.tonarinetserver.repository;

import java.util.List;

import org.scit4bits.tonarinetserver.entity.FileAttachment;
import org.scit4bits.tonarinetserver.entity.FileAttachment.FileType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Integer> {
    
    // Find by article ID
    List<FileAttachment> findByArticleId(Integer articleId);
    
    // Find by uploaded user ID
    List<FileAttachment> findByUploadedBy(Integer uploadedBy);
    
    // Find by file type
    List<FileAttachment> findByType(FileType type);
    
    // Find by article ID and file type
    List<FileAttachment> findByArticleIdAndType(Integer articleId, FileType type);
    
    // Find by uploaded user and privacy
    List<FileAttachment> findByUploadedByAndIsPrivate(Integer uploadedBy, Boolean isPrivate);
    
    // Find by article ID and privacy
    List<FileAttachment> findByArticleIdAndIsPrivate(Integer articleId, Boolean isPrivate);
    
    // Find by original filename containing
    Page<FileAttachment> findByOriginalFilenameContainingIgnoreCase(String filename, Pageable pageable);
    
    // Find by uploaded user ID with pagination
    Page<FileAttachment> findByUploadedBy(Integer uploadedBy, Pageable pageable);
    
    // Find by article ID with pagination
    Page<FileAttachment> findByArticleId(Integer articleId, Pageable pageable);
    
    // Find by type with pagination
    Page<FileAttachment> findByType(FileType type, Pageable pageable);
    
    // Find by privacy status with pagination
    Page<FileAttachment> findByIsPrivate(Boolean isPrivate, Pageable pageable);
    
    // Custom query for searching by uploader name
    @Query("SELECT f FROM FileAttachment f WHERE f.uploadedByUser.name LIKE %:uploaderName%")
    Page<FileAttachment> findByUploaderNameContaining(@Param("uploaderName") String uploaderName, Pageable pageable);
    
    // Custom query for searching by article title
    @Query("SELECT f FROM FileAttachment f WHERE f.article.title LIKE %:articleTitle%")
    Page<FileAttachment> findByArticleTitleContaining(@Param("articleTitle") String articleTitle, Pageable pageable);
    
    // Global search query
    @Query("SELECT f FROM FileAttachment f WHERE " +
           "LOWER(f.originalFilename) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(f.filepath) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(f.uploadedByUser.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(f.article.title) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<FileAttachment> findByAllFieldsContaining(@Param("search") String search, Pageable pageable);
    
    // Count attachments by article
    long countByArticleId(Integer articleId);
    
    // Count attachments by user
    long countByUploadedBy(Integer uploadedBy);
    
    // Find recent uploads by user
    List<FileAttachment> findTop10ByUploadedByOrderByUploadedAtDesc(Integer uploadedBy);
}
