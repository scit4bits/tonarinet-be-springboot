package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 사용자 엔티티 (Spring Security UserDetails 구현)
 */
@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails {

    /** 사용자 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** 이메일 */
    @Column(name = "email", length = 50, nullable = false, unique = true)
    private String email;

    /** 비밀번호 */
    @Column(name = "password", nullable = false, columnDefinition = "TEXT")
    private String password;

    /** 이름 */
    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    private String name;

    /** 생년월일 */
    @Column(name = "birth")
    private LocalDate birth;

    /** 닉네임 */
    @Column(name = "nickname", length = 10, nullable = false, unique = true)
    private String nickname;

    /** 전화번호 */
    @Column(name = "phone", length = 20)
    private String phone;

    /** 성별 */
    @Column(name = "gender", length = 10)
    private String gender;

    /** 설명 */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** 제공자 (예: 'email', 'google', 'kakao') */
    @Column(name = "provider", length = 10)
    private String provider;

    /** OAuth ID */
    @Column(name = "oauth_id", columnDefinition = "TEXT")
    private String oauthid;

    /** 관리자 여부 */
    @Column(name = "is_admin", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isAdmin;

    /** 국적 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nationality", referencedColumnName = "country_code")
    private Country nationality;

    /** 비밀번호 재설정 토큰 */
    @Column(name = "reset_token", columnDefinition = "TEXT")
    private String resetToken;

    @Column(name = "profile_file_id")
    private Integer profileFileId;

    /** 생성일 */
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** 작성한 게시글 목록 */
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private List<Article> articles;

    /** 작성한 실시간 제보 목록 */
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private List<LiveReport> liveReports;

    /** 알림 목록 */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Notification> notifications;

    /** 생성한 채팅방 목록 */
    @OneToMany(mappedBy = "leaderUser", cascade = CascadeType.ALL)
    private List<ChatRoom> chatRooms;

    /** 생성한 파티 목록 */
    @OneToMany(mappedBy = "leaderUser", cascade = CascadeType.ALL)
    private List<Party> parties;

    /** 생성한 팀 목록 */
    @ManyToMany
    @JoinTable(name = "userteam", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "team_id"))
    private List<Team> teams;

    /** 작성한 댓글 목록 */
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private List<Reply> replies;

    /** 생성한 과제 목록 */
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private List<Task> tasksCreated;

    /** 할당된 과제 목록 */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Task> tasksAssigned;

    /** 작성한 동네 리뷰 목록 */
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private List<TownReview> townReviews;

    /** 제출물 목록 */
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private List<Submission> submissions;

    /** 참여한 채팅방 목록 */
    @ManyToMany(mappedBy = "users")
    private List<ChatRoom> joinedChatRooms;

    /** 참여한 파티 목록 */
    @ManyToMany(mappedBy = "users")
    private List<Party> joinedParties;

    /** 참여한 팀 목록 */
    @ManyToMany(mappedBy = "users")
    private List<Team> joinedTeams;

    /** 사용자가 속한 국가 목록 */
    @ManyToMany
    @JoinTable(name = "usercountry", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "country_code"))
    private List<Country> countries;

    /** 사용자 역할 목록 */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserRole> userRoles;

    /** 좋아요한 게시글 목록 */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserLikeArticle> likedArticles;

    /** 업로드한 파일 목록 */
    @OneToMany(mappedBy = "uploadedByUser", cascade = CascadeType.ALL)
    private List<FileAttachment> uploadedFiles;

    /** 좋아요한 게시글 목록 (ManyToMany 관계) */
    @ManyToMany
    @JoinTable(name = "userlikearticle", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "article_id"))
    private List<Article> likedArticlesList;

    /** 소속된 조직 목록 */
    @ManyToMany
    @JoinTable(name = "userrole", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "org_id"))
    private List<Organization> organizations;

    /**
     * 사용자의 권한 목록을 반환합니다.
     * @return 권한 목록 (현재는 비어 있음)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 현재는 비어 있는 컬렉션을 반환합니다. (나중에 역할을 구현할 수 있습니다.)
        return Collections.emptyList();
    }

    /**
     * 사용자의 고유 식별자(여기서는 ID)를 반환합니다.
     * @return 사용자 ID 문자열
     */
    @Override
    public String getUsername() {
        // ID를 사용자 이름으로 사용
        return this.id != null ? this.id.toString() : null;
    }

    /**
     * 사용자의 비밀번호를 반환합니다.
     * @return 비밀번호
     */
    @Override
    public String getPassword() {
        // 실제 비밀번호 필드를 반환
        return this.password;
    }

    /**
     * 계정이 만료되지 않았는지 여부를 반환합니다.
     * @return 항상 true
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 계정이 잠겨있지 않았는지 여부를 반환합니다.
     * @return 항상 true
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 자격 증명(비밀번호)이 만료되지 않았는지 여부를 반환합니다.
     * @return 항상 true
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 계정이 활성화되어 있는지 여부를 반환합니다.
     * @return 항상 true
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}

