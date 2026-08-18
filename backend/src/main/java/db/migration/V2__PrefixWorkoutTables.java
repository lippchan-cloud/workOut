package db.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Flyway V2：将业务表统一加上 work_out 前缀（基础设施迁移）。
 * 已有旧表则 RENAME；新表已存在则跳过；皆不存在则 CREATE。不改 flyway_schema_history。
 */
public class V2__PrefixWorkoutTables extends BaseJavaMigration {

    private static final Logger log = LoggerFactory.getLogger(V2__PrefixWorkoutTables.class);

    /**
     * 按旧表/新表存在性安全切换到 work_out_* 表名。
     *
     * @param context Flyway 迁移上下文（使用其连接，禁止关闭）
     */
    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        log.info("[库表前缀] migrate start schema={}", connection.getCatalog());
        // 先处理用户表，日记录与资料外键依赖它
        ensureUserTable(connection);
        ensureDailyRecordTable(connection);
        ensureProfileTable(connection);
        log.info("[库表前缀] migrate done tables=work_out_user,work_out_daily_record,work_out_profile");
    }

    /**
     * 确保 work_out_user 存在。
     */
    private void ensureUserTable(Connection connection) throws Exception {
        if (tableExists(connection, "work_out_user")) {
            log.info("[库表前缀] skip rename entityType=work_out_user alreadyExists=true");
            return;
        }
        if (tableExists(connection, "user") && hasColumn(connection, "user", "password_hash")) {
            log.info("[库表前缀] rename start from=user to=work_out_user");
            execute(connection, "RENAME TABLE `user` TO work_out_user");
            log.info("[库表前缀] rename done table=work_out_user");
            return;
        }
        log.info("[库表前缀] create start table=work_out_user");
        execute(
                connection,
                """
                CREATE TABLE work_out_user (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    username VARCHAR(32) NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (id),
                    CONSTRAINT uk_work_out_user_username UNIQUE (username)
                )
                """);
        log.info("[库表前缀] create done table=work_out_user");
    }

    /**
     * 确保 work_out_daily_record 存在。
     */
    private void ensureDailyRecordTable(Connection connection) throws Exception {
        if (tableExists(connection, "work_out_daily_record")) {
            log.info("[库表前缀] skip rename entityType=work_out_daily_record alreadyExists=true");
            return;
        }
        if (tableExists(connection, "daily_record") && hasColumn(connection, "daily_record", "user_id")) {
            log.info("[库表前缀] rename start from=daily_record to=work_out_daily_record");
            execute(connection, "RENAME TABLE daily_record TO work_out_daily_record");
            log.info("[库表前缀] rename done table=work_out_daily_record");
            return;
        }
        log.info("[库表前缀] create start table=work_out_daily_record");
        execute(
                connection,
                """
                CREATE TABLE work_out_daily_record (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    user_id BIGINT NOT NULL,
                    type VARCHAR(16) NOT NULL,
                    content VARCHAR(500) NOT NULL,
                    recorded_at TIMESTAMP NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    deleted TINYINT NOT NULL DEFAULT 0,
                    PRIMARY KEY (id),
                    CONSTRAINT fk_work_out_daily_record_user FOREIGN KEY (user_id) REFERENCES work_out_user (id)
                )
                """);
        execute(
                connection,
                "CREATE INDEX idx_work_out_daily_record_user_recorded ON work_out_daily_record (user_id, recorded_at)");
        log.info("[库表前缀] create done table=work_out_daily_record");
    }

    /**
     * 确保 work_out_profile 存在。
     */
    private void ensureProfileTable(Connection connection) throws Exception {
        if (tableExists(connection, "work_out_profile")) {
            log.info("[库表前缀] skip rename entityType=work_out_profile alreadyExists=true");
            return;
        }
        if (tableExists(connection, "profile") && hasColumn(connection, "profile", "user_id")) {
            log.info("[库表前缀] rename start from=profile to=work_out_profile");
            execute(connection, "RENAME TABLE profile TO work_out_profile");
            log.info("[库表前缀] rename done table=work_out_profile");
            return;
        }
        log.info("[库表前缀] create start table=work_out_profile");
        execute(
                connection,
                """
                CREATE TABLE work_out_profile (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    user_id BIGINT NOT NULL,
                    nickname VARCHAR(32) NULL,
                    height_cm DECIMAL(5, 1) NULL,
                    weight_kg DECIMAL(5, 1) NULL,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (id),
                    CONSTRAINT uk_work_out_profile_user UNIQUE (user_id),
                    CONSTRAINT fk_work_out_profile_user FOREIGN KEY (user_id) REFERENCES work_out_user (id)
                )
                """);
        log.info("[库表前缀] create done table=work_out_profile");
    }

    /**
     * 判断当前库是否已有指定表。
     */
    private boolean tableExists(Connection connection, String tableName) throws Exception {
        String sql =
                "SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * 判断旧表是否带有本项目特征列，避免误改共享库其它同名表。
     */
    private boolean hasColumn(Connection connection, String tableName, String columnName) throws Exception {
        String sql =
                "SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * 执行单条 DDL。
     */
    private void execute(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
