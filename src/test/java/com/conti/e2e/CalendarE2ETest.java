package com.conti.e2e;

import com.conti.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("캘린더 E2E 테스트")
class CalendarE2ETest extends BaseE2ETest {

    private User user;
    private String token;

    @BeforeEach
    void setUp() {
        user = createUser("calendar-user@test.com", "캘린더 유저");
        token = getToken(user.getId());
    }

    @Nested
    @DisplayName("캘린더 토큰 발급")
    class GetCalendarToken {

        @Test
        @DisplayName("캘린더 토큰을 발급받는다")
        void getCalendarToken() throws Exception {
            performGet("/api/v1/users/me/calendar-token", token)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isString());
        }
    }

    @Nested
    @DisplayName("iCal 피드")
    class ICalFeed {

        @Test
        @DisplayName("유효한 캘린더 토큰으로 iCal 피드를 조회한다")
        void getICalFeedWithValidToken() throws Exception {
            // 캘린더 토큰 발급
            MvcResult tokenResult = performGet("/api/v1/users/me/calendar-token", token)
                    .andExpect(status().isOk())
                    .andReturn();

            String calendarToken = com.jayway.jsonpath.JsonPath.read(
                    tokenResult.getResponse().getContentAsString(), "$.data");

            // iCal 피드 조회 (인증 필요 없음 - 토큰 파라미터 방식)
            mockMvc.perform(get("/api/v1/calendar.ics")
                            .param("token", calendarToken))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith("text/calendar"))
                    .andExpect(content().string(containsString("BEGIN:VCALENDAR")));
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 iCal 피드 조회 시 에러를 반환한다")
        void getICalFeedWithInvalidToken() throws Exception {
            mockMvc.perform(get("/api/v1/calendar.ics")
                            .param("token", "invalid-calendar-token"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("일반 JWT로 iCal 피드 조회 시 에러를 반환한다")
        void getICalFeedWithRegularJwt() throws Exception {
            // 일반 access token으로는 캘린더 피드를 조회할 수 없어야 함
            mockMvc.perform(get("/api/v1/calendar.ics")
                            .param("token", token))
                    .andExpect(status().isUnauthorized());
        }
    }
}
