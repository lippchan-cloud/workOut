CREATE TABLE work_out_profile_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    changed_at TIMESTAMP(3) NOT NULL,
    nickname VARCHAR(32) NULL,
    height_cm DECIMAL(5, 1) NULL,
    weight_kg DECIMAL(5, 1) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_work_out_profile_history_user FOREIGN KEY (user_id) REFERENCES work_out_user (id)
);

CREATE INDEX idx_work_out_profile_history_user_changed ON work_out_profile_history (user_id, changed_at);

INSERT INTO work_out_profile_history (user_id, changed_at, nickname, height_cm, weight_kg)
SELECT user_id, updated_at, nickname, height_cm, weight_kg
FROM work_out_profile;
