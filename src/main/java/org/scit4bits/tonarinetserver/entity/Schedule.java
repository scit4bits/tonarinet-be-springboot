package org.scit4bits.tonarinetserver.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 일정 엔티티
 */
@Entity
@Table(name = "schedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Schedule {
    /** 일정 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** 일정 제목 */
    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    /** 일정 설명 */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** 생성자 ID */
    @Column(name = "created_by", nullable = false)
    private Integer createdById;

    /** 생성일 */
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** 언제부터 */
    @Column(name = "from_when", nullable = false)
    private LocalDateTime fromWhen;

    /** 언제까지 */
    @Column(name = "to_when", nullable = false)
    private LocalDateTime toWhen;

    /** 할당된 조직 ID */
    @Column(name = "org_id")
    private Integer orgId;

    @Column(name= "type", columnDefinition = "TEXT")
    private String type;

    /** 하루종일 여부 */
    @Builder.Default
    @Column(name = "all_day", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean allDay = false;

    /** 생성자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private User createdBy;

    /** 할당된 조직 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", insertable = false, updatable = false)
    private Organization organization;
}
