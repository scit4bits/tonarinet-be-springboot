package org.scit4bits.tonarinetserver.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "article")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Article {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "category", length = 20)
    private String category;
    
    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;
    
    @Column(name = "contents", nullable = false, columnDefinition = "TEXT")
    private String contents;
    
    @Column(name = "created_by", nullable = false)
    private Integer createdById;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "board_id", nullable = false)
    private Integer boardId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private User createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", insertable = false, updatable = false)
    private Board board;
    
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
    private List<Reply> replies;
    
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
    private List<UserLikeArticle> likedByUsers;
    
    @ManyToMany(mappedBy = "likedArticlesList")
    private List<User> usersWhoLiked;
}
