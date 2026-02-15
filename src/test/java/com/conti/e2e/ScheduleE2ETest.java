package com.conti.e2e;

import com.conti.domain.schedule.dto.ScheduleBulkCreateRequest;
import com.conti.domain.schedule.dto.ScheduleCreateRequest;
import com.conti.domain.schedule.dto.ScheduleRespondRequest;
import com.conti.domain.schedule.dto.ScheduleSignupRequest;
import com.conti.domain.setlist.dto.SetlistCreateRequest;
import com.conti.domain.setlist.entity.WorshipType;
import com.conti.domain.team.dto.MemberPositionRequest;
import com.conti.domain.team.entity.Position;
import com.conti.domain.team.entity.Team;
import com.conti.domain.team.entity.TeamMember;
import com.conti.domain.team.entity.TeamRole;
import com.conti.domain.user.entity.User;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("스케줄 E2E 테스트")
class ScheduleE2ETest extends BaseE2ETest {

    private User admin;
    private User member;
    private String adminToken;
    private String memberToken;
    private Long teamId;
    private Long setlistId;
    private Long adminMemberId;
    private Long memberMemberId;

    @BeforeEach
    void setUp() throws Exception {
        admin = createUser("sched-admin@test.com", "스케줄 관리자");
        member = createUser("sched-member@test.com", "스케줄 멤버");
        adminToken = getToken(admin.getId());
        memberToken = getToken(member.getId());
        Team team = createTeamWithAdmin(admin.getId());
        teamId = team.getId();
        TeamMember viewerMember = addTeamMember(member.getId(), teamId, TeamRole.VIEWER);
        memberMemberId = viewerMember.getId();

        // adminMemberId 가져오기
        MvcResult membersResult = performGet("/api/v1/teams/" + teamId + "/members", adminToken)
                .andExpect(status().isOk())
                .andReturn();
        String membersJson = membersResult.getResponse().getContentAsString();
        int size = ((Number) JsonPath.read(membersJson, "$.data.length()")).intValue();
        for (int i = 0; i < size; i++) {
            Long uid = ((Number) JsonPath.read(membersJson, "$.data[" + i + "].userId")).longValue();
            Long mid = ((Number) JsonPath.read(membersJson, "$.data[" + i + "].memberId")).longValue();
            if (uid.equals(admin.getId())) {
                adminMemberId = mid;
            }
        }

        // 콘티 생성
        SetlistCreateRequest setlistRequest = new SetlistCreateRequest(
                "스케줄 테스트 콘티", LocalDate.of(2026, 3, 1), WorshipType.SUNDAY_1ST, admin.getId(), null
        );
        MvcResult setlistResult = performPost("/api/v1/teams/" + teamId + "/setlists", adminToken, setlistRequest)
                .andExpect(status().isOk())
                .andReturn();
        setlistId = ((Number) JsonPath.read(setlistResult.getResponse().getContentAsString(), "$.data.id")).longValue();
    }

    @Nested
    @DisplayName("멤버 배정")
    class ScheduleMembers {

        @Test
        @DisplayName("ADMIN이 멤버를 일괄 배정한다")
        void adminSchedulesMembers() throws Exception {
            ScheduleBulkCreateRequest request = new ScheduleBulkCreateRequest(
                    List.of(
                            new ScheduleCreateRequest(adminMemberId, Position.WORSHIP_LEADER),
                            new ScheduleCreateRequest(memberMemberId, Position.VOCAL)
                    )
            );

            performPost("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/schedules", adminToken, request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("VIEWER는 멤버를 배정할 수 없다 (403)")
        void viewerCannotScheduleMembers() throws Exception {
            ScheduleBulkCreateRequest request = new ScheduleBulkCreateRequest(
                    List.of(new ScheduleCreateRequest(memberMemberId, Position.VOCAL))
            );

            performPost("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/schedules", memberToken, request)
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("스케줄 조회")
    class GetSchedules {

        @Test
        @DisplayName("콘티의 스케줄을 조회한다")
        void getSchedulesForSetlist() throws Exception {
            performGet("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/schedules", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("나의 스케줄을 조회한다")
        void getMySchedules() throws Exception {
            performGet("/api/v1/teams/" + teamId + "/my-schedules", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("스케줄 응답")
    class ScheduleResponse {

        @Test
        @DisplayName("배정된 멤버가 수락한다")
        void memberAcceptsSchedule() throws Exception {
            // 배정
            ScheduleBulkCreateRequest scheduleRequest = new ScheduleBulkCreateRequest(
                    List.of(new ScheduleCreateRequest(memberMemberId, Position.VOCAL))
            );
            MvcResult scheduleResult = performPost("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/schedules", adminToken, scheduleRequest)
                    .andExpect(status().isOk())
                    .andReturn();

            // 스케줄 ID 가져오기
            MvcResult listResult = performGet("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/schedules", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();
            String listJson = listResult.getResponse().getContentAsString();
            Long scheduleId = ((Number) JsonPath.read(listJson, "$.data[0].id")).longValue();

            // 수락
            ScheduleRespondRequest respondRequest = new ScheduleRespondRequest(true, null);
            performPatch("/api/v1/teams/" + teamId + "/schedules/" + scheduleId + "/respond", memberToken, respondRequest)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
        }

        @Test
        @DisplayName("배정된 멤버가 거절한다")
        void memberDeclinesSchedule() throws Exception {
            // 배정
            ScheduleBulkCreateRequest scheduleRequest = new ScheduleBulkCreateRequest(
                    List.of(new ScheduleCreateRequest(memberMemberId, Position.VOCAL))
            );
            performPost("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/schedules", adminToken, scheduleRequest);

            // 스케줄 ID 가져오기
            MvcResult listResult = performGet("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/schedules", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();
            String listJson = listResult.getResponse().getContentAsString();
            Long scheduleId = ((Number) JsonPath.read(listJson, "$.data[0].id")).longValue();

            // 거절
            ScheduleRespondRequest respondRequest = new ScheduleRespondRequest(false, "개인 사정으로 참석이 어렵습니다");
            performPatch("/api/v1/teams/" + teamId + "/schedules/" + scheduleId + "/respond", memberToken, respondRequest)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("DECLINED"))
                    .andExpect(jsonPath("$.data.declinedReason").value("개인 사정으로 참석이 어렵습니다"));
        }
    }

    @Nested
    @DisplayName("셀프 사인업")
    class Signup {

        @Test
        @DisplayName("멤버가 직접 포지션에 사인업한다")
        void memberSignsUp() throws Exception {
            // 멤버에게 VOCAL 포지션 자격 부여
            performPut("/api/v1/teams/" + teamId + "/members/" + memberMemberId + "/positions", adminToken,
                    List.of(new MemberPositionRequest(Position.VOCAL, true)))
                    .andExpect(status().isOk());

            ScheduleSignupRequest request = new ScheduleSignupRequest(Position.VOCAL);

            performPost("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/schedules/signup", memberToken, request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.position").value("VOCAL"))
                    .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
        }
    }

    @Nested
    @DisplayName("스케줄 매트릭스")
    class ScheduleMatrix {

        @Test
        @DisplayName("스케줄 매트릭스를 조회한다")
        void getScheduleMatrix() throws Exception {
            performGet("/api/v1/teams/" + teamId + "/schedules/matrix?from=2026-02-01&to=2026-04-30", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.positions").isArray())
                    .andExpect(jsonPath("$.data.dates").isArray())
                    .andExpect(jsonPath("$.data.cells").isArray());
        }

        @Test
        @DisplayName("VIEWER도 매트릭스를 조회할 수 있다")
        void viewerCanGetMatrix() throws Exception {
            performGet("/api/v1/teams/" + teamId + "/schedules/matrix?from=2026-01-01&to=2026-03-31", memberToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("스케줄 삭제")
    class DeleteSchedule {

        @Test
        @DisplayName("ADMIN이 스케줄을 삭제한다")
        void adminDeletesSchedule() throws Exception {
            // 배정
            ScheduleBulkCreateRequest scheduleRequest = new ScheduleBulkCreateRequest(
                    List.of(new ScheduleCreateRequest(memberMemberId, Position.VOCAL))
            );
            performPost("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/schedules", adminToken, scheduleRequest);

            // 스케줄 ID 가져오기
            MvcResult listResult = performGet("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/schedules", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();
            String listJson = listResult.getResponse().getContentAsString();
            Long scheduleId = ((Number) JsonPath.read(listJson, "$.data[0].id")).longValue();

            // 삭제
            performDelete("/api/v1/teams/" + teamId + "/schedules/" + scheduleId, adminToken)
                    .andExpect(status().isOk());

            // 삭제 후 조회
            performGet("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/schedules", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }
}
