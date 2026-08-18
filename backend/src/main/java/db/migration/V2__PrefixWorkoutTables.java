package db.migration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;
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
            execute(connection, "ALTER TABLE `user` RENAME TO work_out_user");
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
            execute(connection, "ALTER TABLE daily_record RENAME TO work_out_daily_record");
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
            execute(connection, "ALTER TABLE profile RENAME TO work_out_profile");
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
     * 判断当前库是否已有指定表（JDBC DatabaseMetaData，兼容 MySQL 与 H2）。
     */
    private boolean tableExists(Connection connection, String tableName) throws Exception {
        DatabaseMetaData meta = connection.getMetaData();
        String catalog = connection.getCatalog();
        for (String candidate : nameCandidates(tableName)) {
            try (ResultSet rs = meta.getTables(catalog, null, candidate, new String[] {"TABLE"})) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断旧表是否带有本项目特征列，避免误改共享库其它同名表。
     */
    private boolean hasColumn(Connection connection, String tableName, String columnName) throws Exception {
        DatabaseMetaData meta = connection.getMetaData();
        String catalog = connection.getCatalog();
        for (String tableCandidate : nameCandidates(tableName)) {
            for (String columnCandidate : nameCandidates(columnName)) {
                try (ResultSet rs = meta.getColumns(catalog, null, tableCandidate, columnCandidate)) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 生成大小写候选名，兼容 H2 默认大写与 MySQL 小写标识符。
     */
    private String[] nameCandidates(String name) {
        String upper = name.toUpperCase(Locale.ROOT);
        String lower = name.toLowerCase(Locale.ROOT);
        if (name.equals(upper)) {
            return new String[] {name, lower};
        }
        if (name.equals(lower)) {
            return new String[] {name, upper};
        }
        return new String[] {name, lower, upper};
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
