package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 파일 첨부 엔티티
 */
@Entity
@Table(name = "fileattachment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class FileAttachment {

    /** 파일 첨부 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** 파일 경로 */
    @Column(name = "filepath", nullable = false, columnDefinition = "TEXT")
    private String filepath;

    /** 원본 파일명 */
    @Column(name = "original_filename", nullable = false, columnDefinition = "TEXT")
    private String originalFilename;

    /** 비공개 여부 */
    @Column(name = "is_private", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isPrivate;

    /** 업로드한 사용자 ID */
    @Column(name = "uploaded_by", nullable = false)
    private Integer uploadedBy;

    /** 파일 타입 */
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private FileType type;

    /** 업로드 시간 */
    @CreatedDate
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    /** 게시글 ID */
    @Column(name = "article_id")
    private Integer articleId;

    /** 파일 크기 */
    @Column(name = "filesize", nullable = false)
    private Integer filesize;

    /** 제출물 ID */
    @Column(name = "submission_id")
    private Integer submissionId;

    /** 업로드한 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", insertable = false, updatable = false)
    private User uploadedByUser;

    /** 게시글 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", insertable = false, updatable = false)
    private Article article;

    /** 제출물 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", insertable = false, updatable = false)
    private Submission submission;

    /**
     * 파일 타입 열거형
     */
    public enum FileType {
        /** 이미지 파일 */
        IMAGE,
        /** 일반 첨부 파일 */
        ATTACHMENT
    }
}
