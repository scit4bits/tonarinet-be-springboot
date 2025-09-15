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
 * 과제 그룹 엔티티
 */
@Entity
@Table(name = "taskgroup")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class TaskGroup {

    /** 과제 그룹 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** 제목 */
    @Column(name = "title", length = 100)
    private String title;

    /** 내용 */
    @Column(name = "contents", nullable = false, columnDefinition = "TEXT")
    private String contents;

    /** 생성일 */
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 마감일 */
    @Column(name = "due_date")
    private LocalDateTime dueDate;

    /** 최대 점수 */
    @Column(name = "max_score")
    private Integer maxScore;

    /** 조직 ID */
    @Column(name = "org_id", nullable = false)
    private Integer orgId;

    /** 과제 목록 */
    @OneToMany(mappedBy = "taskGroup", cascade = CascadeType.ALL)
    private List<Task> tasks;

    /** 조직 */
    @ManyToOne
    @JoinColumn(name = "org_id", insertable = false, updatable = false)
    private Organization organization;
}
