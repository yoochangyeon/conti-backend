package com.conti.e2e;

import com.conti.domain.team.dto.TeamNoticeCreateRequest;
import com.conti.domain.team.dto.TeamNoticeUpdateRequest;
import com.conti.domain.team.entity.Team;
import com.conti.domain.team.entity.TeamRole;
import com.conti.domain.user.entity.User;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("팀 공지사항 E2E 테스트")
class TeamNoticeE2ETest extends BaseE2ETest {

    private User admin;
    private User viewer;
    private String adminToken;
    private String viewerToken;
    private Long teamId;

    @BeforeEach
    void setUp() {
        admin = createUser("notice-admin@test.com", "공지 관리자");
        viewer = createUser("notice-viewer@test.com", "공지 뷰어");
        adminToken = getToken(admin.getId());
        viewerToken = getToken(viewer.getId());
        Team team = createTeamWithAdmin(admin.getId());
        teamId = team.getId();
        addTeamMember(viewer.getId(), teamId, TeamRole.VIEWER);
    }

    @Nested
    @DisplayName("공지사항 생성")
    class CreateNotice {

        @Test
        @DisplayName("ADMIN이 공지사항을 생성한다")
        void adminCreatesNotice() throws Exception {
            TeamNoticeCreateRequest request = new TeamNoticeCreateRequest("이번 주 리허설 안내", "토요일 오후 3시에 리허설이 있습니다.");

            performPost("/api/v1/teams/" + teamId + "/notices", adminToken, request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("이번 주 리허설 안내"))
                    .andExpect(jsonPath("$.data.content").value("토요일 오후 3시에 리허설이 있습니다."))
                    .andExpect(jsonPath("$.data.authorName").value("공지 관리자"))
                    .andExpect(jsonPath("$.data.isPinned").value(false))
                    .andExpect(jsonPath("$.data.id").isNumber());
        }

        @Test
        @DisplayName("제목 없이 생성 시 400 에러를 반환한다")
        void createWithoutTitleReturns400() throws Exception {
            TeamNoticeCreateRequest request = new TeamNoticeCreateRequest(null, "내용만 있는 공지");

            performPost("/api/v1/teams/" + teamId + "/notices", adminToken, request)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("VIEWER는 공지사항을 생성할 수 없다 (403)")
        void viewerCannotCreateNotice() throws Exception {
            TeamNoticeCreateRequest request = new TeamNoticeCreateRequest("뷰어 공지", "뷰어가 작성한 공지");

            performPost("/api/v1/teams/" + teamId + "/notices", viewerToken, request)
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("공지사항 목록 조회")
    class GetNotices {

        @Test
        @DisplayName("공지사항 목록을 조회한다")
        void getNotices() throws Exception {
            // 공지 2개 생성
            performPost("/api/v1/teams/" + teamId + "/notices", adminToken,
                    new TeamNoticeCreateRequest("공지 1", "내용 1"));
            performPost("/api/v1/teams/" + teamId + "/notices", adminToken,
                    new TeamNoticeCreateRequest("공지 2", "내용 2"));

            performGet("/api/v1/teams/" + teamId + "/notices", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)));
        }

        @Test
        @DisplayName("VIEWER도 공지사항 목록을 조회할 수 있다")
        void viewerCanGetNotices() throws Exception {
            performGet("/api/v1/teams/" + teamId + "/notices", viewerToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("공지사항 수정")
    class UpdateNotice {

        @Test
        @DisplayName("ADMIN이 공지사항을 수정한다")
        void adminUpdatesNotice() throws Exception {
            MvcResult result = performPost("/api/v1/teams/" + teamId + "/notices", adminToken,
                    new TeamNoticeCreateRequest("원래 공지", "원래 내용"))
                    .andExpect(status().isOk())
                    .andReturn();
            Long noticeId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data.id")).longValue();

            TeamNoticeUpdateRequest updateRequest = new TeamNoticeUpdateRequest("수정된 공지", "수정된 내용");

            performPatch("/api/v1/teams/" + teamId + "/notices/" + noticeId, adminToken, updateRequest)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value("수정된 공지"))
                    .andExpect(jsonPath("$.data.content").value("수정된 내용"));
        }

        @Test
        @DisplayName("VIEWER는 공지사항을 수정할 수 없다 (403)")
        void viewerCannotUpdateNotice() throws Exception {
            MvcResult result = performPost("/api/v1/teams/" + teamId + "/notices", adminToken,
                    new TeamNoticeCreateRequest("수정 불가 공지", "내용"))
                    .andExpect(status().isOk())
                    .andReturn();
            Long noticeId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data.id")).longValue();

            performPatch("/api/v1/teams/" + teamId + "/notices/" + noticeId, viewerToken,
                    new TeamNoticeUpdateRequest("해킹 시도", "내용"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("공지사항 고정/해제")
    class PinNotice {

        @Test
        @DisplayName("공지사항을 고정한다")
        void pinNotice() throws Exception {
            MvcResult result = performPost("/api/v1/teams/" + teamId + "/notices", adminToken,
                    new TeamNoticeCreateRequest("고정할 공지", "내용"))
                    .andExpect(status().isOk())
                    .andReturn();
            Long noticeId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data.id")).longValue();

            // 고정
            performPatchNoBody("/api/v1/teams/" + teamId + "/notices/" + noticeId + "/pin", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.isPinned").value(true));

            // 해제 (토글)
            performPatchNoBody("/api/v1/teams/" + teamId + "/notices/" + noticeId + "/pin", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.isPinned").value(false));
        }
    }

    @Nested
    @DisplayName("공지사항 삭제")
    class DeleteNotice {

        @Test
        @DisplayName("ADMIN이 공지사항을 삭제한다")
        void adminDeletesNotice() throws Exception {
            MvcResult result = performPost("/api/v1/teams/" + teamId + "/notices", adminToken,
                    new TeamNoticeCreateRequest("삭제할 공지", "내용"))
                    .andExpect(status().isOk())
                    .andReturn();
            Long noticeId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data.id")).longValue();

            performDelete("/api/v1/teams/" + teamId + "/notices/" + noticeId, adminToken)
                    .andExpect(status().isOk());

            // 삭제 후 목록 확인
            performGet("/api/v1/teams/" + teamId + "/notices", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        @DisplayName("VIEWER는 공지사항을 삭제할 수 없다 (403)")
        void viewerCannotDeleteNotice() throws Exception {
            MvcResult result = performPost("/api/v1/teams/" + teamId + "/notices", adminToken,
                    new TeamNoticeCreateRequest("삭제 불가 공지", "내용"))
                    .andExpect(status().isOk())
                    .andReturn();
            Long noticeId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data.id")).longValue();

            performDelete("/api/v1/teams/" + teamId + "/notices/" + noticeId, viewerToken)
                    .andExpect(status().isForbidden());
        }
    }
}
