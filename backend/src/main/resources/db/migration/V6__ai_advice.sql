-- 七期：用户 API Key、AI 调用日志、上下文摘要块；分享建议状态
ALTER TABLE work_out_share_report
    ADD COLUMN advice_status VARCHAR(16) NOT NULL DEFAULT 'NONE_KEY' AFTER snapshot_json;

CREATE TABLE work_out_user_api_key (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    api_key VARCHAR(256) NOT NULL,
    key_mask VARCHAR(32) NOT NULL,
    updated_at TIMESTAMP(3) NOT NULL,
    updated_by BIGINT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_work_out_user_api_key_user UNIQUE (user_id),
    CONSTRAINT fk_work_out_user_api_key_user FOREIGN KEY (user_id) REFERENCES work_out_user (id)
);

CREATE TABLE work_out_ai_call_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    api_key_id BIGINT NULL,
    purpose VARCHAR(32) NOT NULL,
    status VARCHAR(16) NOT NULL,
    share_token VARCHAR(64) NULL,
    created_at TIMESTAMP(3) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_work_out_ai_call_log_user FOREIGN KEY (user_id) REFERENCES work_out_user (id),
    CONSTRAINT fk_work_out_ai_call_log_key FOREIGN KEY (api_key_id) REFERENCES work_out_user_api_key (id)
);

CREATE INDEX idx_work_out_ai_call_log_key_created ON work_out_ai_call_log (api_key_id, created_at);
CREATE INDEX idx_work_out_ai_call_log_user_created ON work_out_ai_call_log (user_id, created_at);

CREATE TABLE work_out_ai_context_chunk (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    source_type VARCHAR(32) NOT NULL,
    source_ref VARCHAR(64) NULL,
    summary_text MEDIUMTEXT NOT NULL,
    embed_hash VARCHAR(64) NOT NULL,
    embedding_json MEDIUMTEXT NULL,
    created_at TIMESTAMP(3) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_work_out_ai_context_user FOREIGN KEY (user_id) REFERENCES work_out_user (id)
);

CREATE INDEX idx_work_out_ai_context_user_hash ON work_out_ai_context_chunk (user_id, embed_hash);
