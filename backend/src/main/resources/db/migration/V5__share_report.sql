CREATE TABLE work_out_share_report (
    id BIGINT NOT NULL AUTO_INCREMENT,
    token VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    range_from DATE NOT NULL,
    range_to DATE NOT NULL,
    snapshot_json MEDIUMTEXT NOT NULL,
    created_at TIMESTAMP(3) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_work_out_share_report_token UNIQUE (token),
    CONSTRAINT fk_work_out_share_report_user FOREIGN KEY (user_id) REFERENCES work_out_user (id)
);

CREATE INDEX idx_work_out_share_report_user ON work_out_share_report (user_id);
