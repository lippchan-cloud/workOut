-- 密钥库：独立管理 DeepSeek API Key；用户绑定表增加 pool_id
CREATE TABLE work_out_api_key (
    id BIGINT NOT NULL AUTO_INCREMENT,
    api_key VARCHAR(256) NOT NULL,
    key_mask VARCHAR(32) NOT NULL,
    enabled TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP(3) NOT NULL,
    created_by BIGINT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_work_out_api_key_value (api_key)
);

INSERT INTO work_out_api_key (api_key, key_mask, enabled, created_at, created_by)
SELECT api_key, MAX(key_mask), 1, MIN(updated_at), MIN(updated_by)
FROM work_out_user_api_key
GROUP BY api_key;

ALTER TABLE work_out_user_api_key
    ADD COLUMN pool_id BIGINT NULL AFTER user_id;

UPDATE work_out_user_api_key u
INNER JOIN work_out_api_key p ON p.api_key = u.api_key
SET u.pool_id = p.id;

ALTER TABLE work_out_ai_call_log
    DROP FOREIGN KEY fk_work_out_ai_call_log_key;

UPDATE work_out_ai_call_log l
INNER JOIN work_out_user_api_key u ON l.api_key_id = u.id
SET l.api_key_id = u.pool_id
WHERE l.api_key_id IS NOT NULL;

ALTER TABLE work_out_ai_call_log
    ADD CONSTRAINT fk_work_out_ai_call_log_key FOREIGN KEY (api_key_id) REFERENCES work_out_api_key (id);

ALTER TABLE work_out_user_api_key
    ADD CONSTRAINT fk_work_out_user_api_key_pool FOREIGN KEY (pool_id) REFERENCES work_out_api_key (id);
