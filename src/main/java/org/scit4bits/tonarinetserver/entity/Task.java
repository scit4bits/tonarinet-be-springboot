package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 과제 엔티티
 */
@Entity
@Table(name = "task")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Task {

    /** 과제 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** 과제 이름 */
    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    private String name;

    /** 과제 내용 */
    @Column(name = "contents", nullable = false, columnDefinition = "TEXT")
    private String contents;

    /** 생성자 ID */
    @Column(name = "created_by", nullable = false)
    private Integer createdById;

    /** 생성일 */
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** 수정일 */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** 마감일 */
    @Column(name = "due_date")
    private LocalDateTime dueDate;

    /** 할당된 사용자 ID */
    @Column(name = "user_id")
    private Integer userId;

    /** 할당된 팀 ID */
    @Column(name = "team_id")
    private Integer teamId;

    /** 점수 */
    @Column(name = "score")
    private Integer score;

    /** 최대 점수 */
    @Column(name = "max_score")
    private Integer maxScore;

    /** 피드백 */
    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    /** 과제 그룹 ID */
    @Column(name = "taskgroup_id", nullable = false)
    private Integer taskGroupId;

    /** 생성자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private User createdBy;

    /** 할당된 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /** 할당된 팀 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", insertable = false, updatable = false)
    private Team team;

    /** 과제 그룹 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taskgroup_id", insertable = false, updatable = false)
    private TaskGroup taskGroup;

    /** 제출물 목록 */
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private List<Submission> submissions;
}
