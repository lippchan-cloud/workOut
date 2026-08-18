package com.workout.modules.auth.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import com.workout.modules.auth.domain.UserRole;

/**
 * 注册用户持久化实体（基础设施/持久化层）。
 * 对应表 work_out_user；仅存密码哈希，禁止承载明文密码或 JWT。
 */
@Entity
@Table(name = "work_out_user")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private UserRole role = UserRole.USER;

    /**
     * 主键。
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置主键。
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 用户名。
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名。
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 密码哈希。
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * 设置密码哈希。
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * 创建时间。
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置创建时间。
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 角色：USER 或 ADMIN。
     */
    public UserRole getRole() {
        return role;
    }

    /**
     * 设置角色。
     */
    public void setRole(UserRole role) {
        this.role = role;
    }
}
