package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 제출물 엔티티
 */
@Entity
@Table(name = "submission")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Submission {

    /** 제출물 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** 생성일 */
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 작성자 ID */
    @Column(name = "created_by", nullable = false)
    private Integer createdById;

    /** 내용 */
    @Column(name = "contents", columnDefinition = "TEXT")
    private String contents;

    /** 과제 ID */
    @Column(name = "task_id", nullable = false)
    private Integer taskId;

    /** 작성자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private User createdBy;

    /** 과제 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    private Task task;

    /** 첨부 파일 목록 */
    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL)
    private List<FileAttachment> fileAttachments;
}
