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

@Entity
@Table(name = "taskgroup")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class TaskGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "contents", nullable = false, columnDefinition = "TEXT")
    private String contents;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "max_score")
    private Integer maxScore;

    @Column(name = "org_id", nullable = false)
    private Integer orgId;

    @OneToMany(mappedBy = "taskGroup", cascade = CascadeType.ALL)
    private List<Task> tasks;

    @ManyToOne
    @JoinColumn(name = "org_id", insertable = false, updatable = false)
    private Organization organization;
}
