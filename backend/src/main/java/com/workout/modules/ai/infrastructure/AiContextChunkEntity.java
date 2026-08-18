package com.workout.modules.ai.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * 本地上下文摘要块（持久化层）。
 * 用 MySQL 存摘要与 hash，按 userId 隔离；不上独立向量库。
 */
@Entity
@Table(name = "work_out_ai_context_chunk")
public class AiContextChunkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "source_type", nullable = false, length = 32)
    private String sourceType;

    @Column(name = "source_ref", length = 64)
    private String sourceRef;

    @Column(name = "summary_text", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String summaryText;

    @Column(name = "embed_hash", nullable = false, length = 64)
    private String embedHash;

    @Column(name = "embedding_json", columnDefinition = "MEDIUMTEXT")
    private String embeddingJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

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
     * 所属用户（隔离边界）。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置用户。
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 来源类型，如 SHARE。
     */
    public String getSourceType() {
        return sourceType;
    }

    /**
     * 设置来源类型。
     */
    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    /**
     * 来源引用（如 share token）。
     */
    public String getSourceRef() {
        return sourceRef;
    }

    /**
     * 设置来源引用。
     */
    public void setSourceRef(String sourceRef) {
        this.sourceRef = sourceRef;
    }

    /**
     * 压缩后的摘要文本。
     */
    public String getSummaryText() {
        return summaryText;
    }

    /**
     * 设置摘要。
     */
    public void setSummaryText(String summaryText) {
        this.summaryText = summaryText;
    }

    /**
     * 摘要 SHA-256 hex，同 userId 去重。
     */
    public String getEmbedHash() {
        return embedHash;
    }

    /**
     * 设置 hash。
     */
    public void setEmbedHash(String embedHash) {
        this.embedHash = embedHash;
    }

    /**
     * MySQL 内简易向量 JSON（非独立向量库）。
     */
    public String getEmbeddingJson() {
        return embeddingJson;
    }

    /**
     * 设置 embedding JSON。
     */
    public void setEmbeddingJson(String embeddingJson) {
        this.embeddingJson = embeddingJson;
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
