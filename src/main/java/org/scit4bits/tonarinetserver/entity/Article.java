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
 * 게시글 엔티티
 */
@Entity
@Table(name = "article")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Article {

    /** 게시글 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** 카테고리 */
    @Column(name = "category", length = 20)
    private String category;

    /** 제목 */
    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    /** 내용 */
    @Column(name = "contents", nullable = false, columnDefinition = "TEXT")
    private String contents;

    /** 작성자 ID */
    @Column(name = "created_by", nullable = false)
    private Integer createdById;

    /** 생성일 */
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 최종 수정일 */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** 조회수 */
    @Builder.Default
    @Column(name = "views", nullable = false)
    private Integer views = 0;

    /** 게시판 ID */
    @Column(name = "board_id", nullable = false)
    private Integer boardId;

    /** 작성자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private User createdBy;

    /** 게시판 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", insertable = false, updatable = false)
    private Board board;

    /** 댓글 목록 */
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
    private List<Reply> replies;

    /** 좋아요 누른 사용자 목록 */
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
    private List<UserLikeArticle> likedByUsers;

    /** 첨부 파일 목록 */
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
    private List<FileAttachment> attachments;

    /** 태그 목록 */
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
    private List<Tag> tags;

    /** 게시글을 좋아요한 사용자 목록 */
    @ManyToMany(mappedBy = "likedArticlesList")
    private List<User> usersWhoLiked;
}

