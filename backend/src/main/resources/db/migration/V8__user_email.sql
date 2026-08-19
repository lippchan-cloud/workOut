ALTER TABLE work_out_user
    ADD COLUMN email VARCHAR(128) NULL;

ALTER TABLE work_out_user
    ADD CONSTRAINT uk_work_out_user_email UNIQUE (email);

CREATE TABLE work_out_email_code (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(128) NOT NULL,
    purpose VARCHAR(16) NOT NULL,
    code_hash VARCHAR(255) NOT NULL,
    user_id BIGINT NULL,
    expires_at TIMESTAMP(3) NOT NULL,
    used_at TIMESTAMP(3) NULL,
    fail_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(3) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_work_out_email_code_user FOREIGN KEY (user_id) REFERENCES work_out_user (id)
);

CREATE INDEX idx_work_out_email_code_lookup ON work_out_email_code (email, purpose, created_at);
