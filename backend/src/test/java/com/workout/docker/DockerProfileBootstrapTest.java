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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 验证 docker profile 可在无外部 MySQL 时启动：内嵌 H2 + Flyway 产出 work_out_*，健康检查可用。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("docker")
class DockerProfileBootstrapTest {

    @DynamicPropertySource
    static void inMemoryH2(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                () ->
                        "jdbc:h2:mem:workout_docker;MODE=MySQL;DATABASE_TO_LOWER=TRUE;"
                                + "CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    @Test
    void dockerProfileShouldBootWithEmbeddedH2AndExposeHealth() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("UP"));

        try (Connection connection = dataSource.getConnection();
                ResultSet rs =
                        connection
                                .getMetaData()
                                .getTables(connection.getCatalog(), null, "work_out_user", new String[] {"TABLE"})) {
            assertThat(rs.next()).as("work_out_user must exist after Flyway V2 on docker profile").isTrue();
        }
    }
}
