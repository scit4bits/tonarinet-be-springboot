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

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "email", length = 50, nullable = false, unique = true)
    private String email;
    
    @Column(name = "password", nullable = false, columnDefinition = "TEXT")
    private String password;
    
    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    private String name;
    
    @Column(name = "birth")
    private LocalDate birth;
    
    @Column(name = "nickname", length = 10, nullable = false, unique = true)
    private String nickname;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "provider", length=10)
    private String provider;
    
    @Column(name = "oauth_id", columnDefinition = "TEXT")
    private String oauthid;

    @Column(name = "is_admin", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isAdmin;
    
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private List<Article> articles;
    
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private List<LiveReport> liveReports;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Notification> notifications;
    
    @OneToMany(mappedBy = "leaderUser", cascade = CascadeType.ALL)
    private List<ChatRoom> chatRooms;
    
    @OneToMany(mappedBy = "leaderUser", cascade = CascadeType.ALL)
    private List<Party> parties;
    
    @OneToMany(mappedBy = "leaderUser", cascade = CascadeType.ALL)
    private List<Team> teams;
    
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private List<Reply> replies;
    
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private List<Task> tasksCreated;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Task> tasksAssigned;
    
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private List<TownReview> townReviews;
    
    @ManyToMany(mappedBy = "users")
    private List<ChatRoom> joinedChatRooms;
    
    @ManyToMany(mappedBy = "users")
    private List<Party> joinedParties;
    
    @ManyToMany(mappedBy = "users")
    private List<Team> joinedTeams;
    
    @ManyToMany
    @JoinTable(
        name = "usercountry",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "country_code")
    )
    private List<Country> countries;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserRole> userRoles;


    @ManyToMany
    @JoinTable(
        name = "userrole",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "org_id")
    )
    private List<Organization> organizations;

    // UserDetails implementation methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Return empty collection for now - you can implement roles later
        return Collections.emptyList();
    }

    @Override
    public String getUsername() {
        // Use id as username
        return this.id != null ? this.id.toString() : null;
    }

    @Override
    public String getPassword() {
        // Return the actual password field
        return this.password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
