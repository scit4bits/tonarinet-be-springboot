package org.scit4bits.tonarinetserver.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", insertable = false, updatable = false)
    private User uploadedByUser;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", insertable = false, updatable = false)
    private Article article;
    
    public enum FileType {
        IMAGE, ATTACHMENT
    }
}
