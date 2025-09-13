package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "fileattachment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class FileAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "filepath", nullable = false, columnDefinition = "TEXT")
    private String filepath;

    @Column(name = "original_filename", nullable = false, columnDefinition = "TEXT")
    private String originalFilename;

    @Column(name = "is_private", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isPrivate;

    @Column(name = "uploaded_by", nullable = false)
    private Integer uploadedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private FileType type;

    @CreatedDate
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "article_id")
    private Integer articleId;

    @Column(name = "filesize", nullable = false)
    private Integer filesize;

    @Column(name = "submission_id")
    private Integer submissionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", insertable = false, updatable = false)
    private User uploadedByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", insertable = false, updatable = false)
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", insertable = false, updatable = false)
    private Submission submission;

    public enum FileType {
        IMAGE, ATTACHMENT
    }
}
