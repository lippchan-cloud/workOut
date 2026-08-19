package com.workout;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Flyway 迁移契约测试。
 * 断言业务表使用统一前缀 work_out_*，避免 MySQL 保留字 user 及共享库表名冲突。
 */
@SpringBootTest
@ActiveProfiles("test")
class FlywayMigrationTest {

    @Autowired
    private DataSource dataSource;

    /**
     * 迁移完成后必须存在带前缀的三张业务表。
     */
    @Test
    void shouldCreatePrefixedUserDailyRecordAndProfileTables() throws Exception {
        Set<String> tables = new HashSet<>();
        try (Connection connection = dataSource.getConnection();
                ResultSet rs = connection.getMetaData().getTables(null, null, "%", new String[] {"TABLE"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME").toLowerCase());
            }
        }

        assertThat(tables)
                .contains(
                        "work_out_user",
                        "work_out_daily_record",
                        "work_out_profile",
                        "work_out_profile_history",
                        "work_out_share_report",
                        "work_out_api_key",
                        "work_out_email_code");
    }
}
