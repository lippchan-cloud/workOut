package com.workout.record;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workout.support.TestUsernames;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DailyRecordQueryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void sameDayRecordsShouldBeOrderedByRecordedAtThenId() throws Exception {
        AuthUser user = register(TestUsernames.unique("query_user"), "secret12");
        create(user.token(), "INTAKE", "早餐", "2026-08-18T08:00:00+08:00");
        create(user.token(), "CONSUME", "晨跑", "2026-08-18T07:30:00+08:00");

        mockMvc.perform(get("/api/v1/dailyRecords")
                        .param("date", "2026-08-18")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].type").value("CONSUME"))
                .andExpect(jsonPath("$.data.list[0].content").value("晨跑"))
                .andExpect(jsonPath("$.data.list[1].type").value("INTAKE"))
                .andExpect(jsonPath("$.data.list[1].content").value("早餐"));
    }

    @Test
    void yearMonthShouldReturnOnlyThatMonthWithFromTo() throws Exception {
        AuthUser user = register(TestUsernames.unique("ym_user"), "secret12");
        create(user.token(), "CONSUME", "八月晨跑", "2026-08-01T07:30:00+08:00");
        create(user.token(), "INTAKE", "八月晚餐", "2026-08-31T21:00:00+08:00");
        create(user.token(), "CONSUME", "九月记录", "2026-09-01T00:30:00+08:00");

        mockMvc.perform(get("/api/v1/dailyRecords")
                        .param("yearMonth", "2026-08")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.yearMonth").value("2026-08"))
                .andExpect(jsonPath("$.data.from").value("2026-08-01"))
                .andExpect(jsonPath("$.data.to").value("2026-08-31"))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].content").value("八月晨跑"))
                .andExpect(jsonPath("$.data.list[1].content").value("八月晚餐"));
    }

    @Test
    void emptyYearMonthShouldReturnEmptyListWithMonthBounds() throws Exception {
        AuthUser user = register(TestUsernames.unique("ym_empty"), "secret12");

        mockMvc.perform(get("/api/v1/dailyRecords")
                        .param("yearMonth", "2026-02")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.yearMonth").value("2026-02"))
                .andExpect(jsonPath("$.data.from").value("2026-02-01"))
                .andExpect(jsonPath("$.data.to").value("2026-02-28"))
                .andExpect(jsonPath("$.data.list.length()").value(0));
    }

    @Test
    void fromToInclusiveRangeShouldExcludeOutsideDays() throws Exception {
        AuthUser user = register(TestUsernames.unique("range_user"), "secret12");
        create(user.token(), "CONSUME", "十七日", "2026-08-17T10:00:00+08:00");
        create(user.token(), "INTAKE", "十九日", "2026-08-19T10:00:00+08:00");

        mockMvc.perform(get("/api/v1/dailyRecords")
                        .param("from", "2026-08-17")
                        .param("to", "2026-08-18")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.from").value("2026-08-17"))
                .andExpect(jsonPath("$.data.to").value("2026-08-18"))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].content").value("十七日"));
    }

    @Test
    void sameDayFromToShouldMatchDateQueryCount() throws Exception {
        AuthUser user = register(TestUsernames.unique("same_day"), "secret12");
        create(user.token(), "CONSUME", "晨跑", "2026-08-18T07:30:00+08:00");
        create(user.token(), "INTAKE", "早餐", "2026-08-18T08:00:00+08:00");

        MvcResult dateResult = mockMvc.perform(get("/api/v1/dailyRecords")
                        .param("date", "2026-08-18")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andReturn();
        MvcResult rangeResult = mockMvc.perform(get("/api/v1/dailyRecords")
                        .param("from", "2026-08-18")
                        .param("to", "2026-08-18")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode dateList =
                objectMapper.readTree(dateResult.getResponse().getContentAsString()).path("data").path("list");
        JsonNode rangeList =
                objectMapper.readTree(rangeResult.getResponse().getContentAsString()).path("data").path("list");
        org.assertj.core.api.Assertions.assertThat(rangeList.size()).isEqualTo(dateList.size()).isEqualTo(2);
    }

    @Test
    void mixingDateAndYearMonthShouldReturn400() throws Exception {
        AuthUser user = register(TestUsernames.unique("mix_mode"), "secret12");

        mockMvc.perform(get("/api/v1/dailyRecords")
                        .param("date", "2026-08-18")
                        .param("yearMonth", "2026-08")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value(org.hamcrest.Matchers.containsString("只能使用一种筛选条件")));
    }

    @Test
    void fromAfterToShouldReturn400() throws Exception {
        AuthUser user = register(TestUsernames.unique("from_after"), "secret12");

        mockMvc.perform(get("/api/v1/dailyRecords")
                        .param("from", "2026-08-20")
                        .param("to", "2026-08-18")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value(org.hamcrest.Matchers.containsString("开始日期不能晚于结束日期")));
    }

    @Test
    void rangeLongerThan366DaysShouldReturn400() throws Exception {
        AuthUser user = register(TestUsernames.unique("too_long"), "secret12");

        mockMvc.perform(get("/api/v1/dailyRecords")
                        .param("from", "2025-01-01")
                        .param("to", "2026-12-31")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value(org.hamcrest.Matchers.containsString("查询区间不能超过366天")));
    }

    @Test
    void dateOnlyQueryShouldStillReturn200() throws Exception {
        AuthUser user = register(TestUsernames.unique("date_only"), "secret12");
        create(user.token(), "CONSUME", "晨跑", "2026-08-18T07:30:00+08:00");

        mockMvc.perform(get("/api/v1/dailyRecords")
                        .param("date", "2026-08-18")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.date").value("2026-08-18"))
                .andExpect(jsonPath("$.data.list.length()").value(1));
    }

    private void create(String token, String type, String content, String recordedAt) throws Exception {
        mockMvc.perform(post("/api/v1/dailyRecords")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "request",
                                Map.of("type", type, "content", content, "recordedAt", recordedAt)))))
                .andExpect(status().isOk());
    }

    private AuthUser register(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "request", Map.of("username", username, "password", password)))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        return new AuthUser(data.path("token").asText(), data.path("userId").asLong());
    }

    private record AuthUser(String token, long userId) {}
}
