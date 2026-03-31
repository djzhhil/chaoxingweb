package com.chaoxingweb.auth.entity;

import com.chaoxingweb.auth.enums.UserRole;
import com.chaoxingweb.auth.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Entity
@Table(name = "user")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    /**
     * 超星用户名
     */
    @Column(name = "chaoxing_username", length = 50)
    private String chaoxingUsername;

    /**
     * 超星 Cookie
     */
    @Column(name = "chaoxing_cookie", columnDefinition = "TEXT")
    private String chaoxingCookie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
