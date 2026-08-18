package com.workout.docker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Connection;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 验证 docker 运行路径使用项目 MySQL（与 application.yml 同套），而非内嵌 H2；健康检查可用。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("docker")
class DockerProfileBootstrapTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    /**
     * docker profile 不得落到 H2：JDBC 须为 mysql，且 Flyway 后存在 work_out_user，健康检查 UP。
     */
    @Test
    void dockerProfileShouldUseMysqlDatasourceAndExposeHealth() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String jdbcUrl = connection.getMetaData().getURL();
            assertThat(jdbcUrl)
                    .as("docker profile must use project MySQL, not embedded H2")
                    .startsWith("jdbc:mysql:")
                    .doesNotContain("h2");

            try (ResultSet rs =
                    connection
                            .getMetaData()
                            .getTables(connection.getCatalog(), null, "work_out_user", new String[] {"TABLE"})) {
                assertThat(rs.next()).as("work_out_user must exist after Flyway on docker/MySQL path").isTrue();
            }
        }

        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("UP"));
    }
}
