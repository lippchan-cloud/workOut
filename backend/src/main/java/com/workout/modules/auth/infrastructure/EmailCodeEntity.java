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
import com.workout.modules.auth.domain.EmailCodePurpose;

/**
 * 邮箱验证码持久化实体（基础设施/持久化层）。
 * 对应表 work_out_email_code；只存哈希，禁止承载明文验证码进 API。
 */
@Entity
@Table(name = "work_out_email_code")
public class EmailCodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private EmailCodePurpose purpose;

    @Column(name = "code_hash", nullable = false)
    private String codeHash;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "fail_count", nullable = false)
    private int failCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

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
     * 规范化后的邮箱。
     */
    public String getEmail() {
        return email;
    }

    /**
     * 设置邮箱。
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 验证码用途。
     */
    public EmailCodePurpose getPurpose() {
        return purpose;
    }

    /**
     * 设置用途。
     */
    public void setPurpose(EmailCodePurpose purpose) {
        this.purpose = purpose;
    }

    /**
     * 验证码哈希。
     */
    public String getCodeHash() {
        return codeHash;
    }

    /**
     * 设置验证码哈希。
     */
    public void setCodeHash(String codeHash) {
        this.codeHash = codeHash;
    }

    /**
     * 关联用户；LOGIN 为已绑定用户，BIND/UNBIND 为当前登录用户。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置关联用户。
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 过期时间。
     */
    public Instant getExpiresAt() {
        return expiresAt;
    }

    /**
     * 设置过期时间。
     */
    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * 使用时间；空表示未使用。
     */
    public Instant getUsedAt() {
        return usedAt;
    }

    /**
     * 设置使用时间。
     */
    public void setUsedAt(Instant usedAt) {
        this.usedAt = usedAt;
    }

    /**
     * 连续校验失败次数。
     */
    public int getFailCount() {
        return failCount;
    }

    /**
     * 设置失败次数。
     */
    public void setFailCount(int failCount) {
        this.failCount = failCount;
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
}
