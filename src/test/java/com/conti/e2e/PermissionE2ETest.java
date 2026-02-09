package com.conti.e2e;

import com.conti.domain.song.dto.SongCreateRequest;
import com.conti.domain.team.dto.TeamCreateRequest;
import com.conti.domain.team.entity.Team;
import com.conti.domain.team.entity.TeamRole;
import com.conti.domain.user.entity.User;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("권한 검증 E2E 테스트")
class PermissionE2ETest extends BaseE2ETest {

    private User admin;
    private User viewer;
    private User nonMember;
    private String adminToken;
    private String viewerToken;
    private String nonMemberToken;
    private Long teamId;

    @BeforeEach
    void setUp() throws Exception {
        admin = createUser("admin@perm.com", "관리자");
        viewer = createUser("viewer@perm.com", "뷰어");
        nonMember = createUser("outsider@perm.com", "외부인");

        adminToken = getToken(admin.getId());
        viewerToken = getToken(viewer.getId());
        nonMemberToken = getToken(nonMember.getId());

        // admin이 팀 생성 (자동으로 ADMIN 역할 부여됨)
        TeamCreateRequest createRequest = new TeamCreateRequest("권한 테스트 팀", null);
        MvcResult result = performPost("/api/v1/teams", adminToken, createRequest)
                .andExpect(status().isOk())
                .andReturn();

        teamId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data.id")).longValue();

        // viewer를 팀에 VIEWER로 추가
        addTeamMember(viewer.getId(), teamId, TeamRole.VIEWER);
    }

    @Nested
    @DisplayName("VIEWER 권한 테스트")
    class ViewerPermissions {

        @Test
        @DisplayName("VIEWER는 곡을 생성할 수 없다 (403)")
        void viewerCannotCreateSong() throws Exception {
            SongCreateRequest request = new SongCreateRequest(
                    "Test Song", "Artist", "C", 120, null, null, null, null
            );
            performPost("/api/v1/teams/" + teamId + "/songs", viewerToken, request)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("VIEWER는 팀 정보를 수정할 수 없다 (403)")
        void viewerCannotUpdateTeam() throws Exception {
            performPatch("/api/v1/teams/" + teamId, viewerToken,
                    new java.util.HashMap<>() {{
                        put("name", "해킹 시도");
                    }})
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("VIEWER는 팀을 삭제할 수 없다 (403)")
        void viewerCannotDeleteTeam() throws Exception {
            performDelete("/api/v1/teams/" + teamId, viewerToken)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("VIEWER는 곡 목록을 조회할 수 있다 (200)")
        void viewerCanGetSongs() throws Exception {
            performGet("/api/v1/teams/" + teamId + "/songs?page=0&size=10", viewerToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("VIEWER는 멤버 목록을 조회할 수 있다 (200)")
        void viewerCanGetMembers() throws Exception {
            performGet("/api/v1/teams/" + teamId + "/members", viewerToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("VIEWER는 콘티 목록을 조회할 수 있다 (200)")
        void viewerCanGetSetlists() throws Exception {
            performGet("/api/v1/teams/" + teamId + "/setlists?page=0&size=10", viewerToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("VIEWER는 콘티를 생성할 수 없다 (403)")
        void viewerCannotCreateSetlist() throws Exception {
            performPost("/api/v1/teams/" + teamId + "/setlists", viewerToken,
                    new java.util.HashMap<>() {{
                        put("title", "불법 콘티");
                        put("worshipDate", "2026-03-01");
                    }})
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("VIEWER는 태그를 조회할 수 있다 (200)")
        void viewerCanGetTags() throws Exception {
            performGet("/api/v1/teams/" + teamId + "/tags", viewerToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("VIEWER는 초대 코드를 재생성할 수 없다 (403)")
        void viewerCannotRegenerateInvite() throws Exception {
            performPostNoBody("/api/v1/teams/" + teamId + "/invite", viewerToken)
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("비멤버 권한 테스트")
    class NonMemberPermissions {

        @Test
        @DisplayName("비멤버는 팀의 곡에 접근할 수 없다 (403)")
        void nonMemberCannotAccessSongs() throws Exception {
            performGet("/api/v1/teams/" + teamId + "/songs?page=0&size=10", nonMemberToken)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("비멤버는 팀 멤버 목록에 접근할 수 없다 (403)")
        void nonMemberCannotAccessMembers() throws Exception {
            performGet("/api/v1/teams/" + teamId + "/members", nonMemberToken)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("비멤버는 팀의 콘티에 접근할 수 없다 (403)")
        void nonMemberCannotAccessSetlists() throws Exception {
            performGet("/api/v1/teams/" + teamId + "/setlists?page=0&size=10", nonMemberToken)
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("인증되지 않은 요청 테스트")
    class UnauthenticatedRequests {

        @Test
        @DisplayName("토큰 없이 곡 목록 조회 시 403을 반환한다")
        void unauthenticatedGetSongsReturns403() throws Exception {
            mockMvc.perform(get("/api/v1/teams/" + teamId + "/songs")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("토큰 없이 팀 정보 조회 시 403을 반환한다")
        void unauthenticatedGetTeamReturns403() throws Exception {
            mockMvc.perform(get("/api/v1/teams/" + teamId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("토큰 없이 사용자 정보 조회 시 403을 반환한다")
        void unauthenticatedGetUserReturns403() throws Exception {
            mockMvc.perform(get("/api/v1/users/me")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 요청 시 403을 반환한다")
        void invalidTokenReturns403() throws Exception {
            mockMvc.perform(get("/api/v1/users/me")
                            .header("Authorization", "Bearer invalid.jwt.token")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("ADMIN 권한 테스트")
    class AdminPermissions {

        @Test
        @DisplayName("ADMIN은 곡을 생성할 수 있다 (200)")
        void adminCanCreateSong() throws Exception {
            SongCreateRequest request = new SongCreateRequest(
                    "Admin Song", "Admin Artist", "E", 130, null, null, null, List.of("경배")
            );
            performPost("/api/v1/teams/" + teamId + "/songs", adminToken, request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value("Admin Song"));
        }

        @Test
        @DisplayName("ADMIN은 팀 정보를 수정할 수 있다 (200)")
        void adminCanUpdateTeam() throws Exception {
            performPatch("/api/v1/teams/" + teamId, adminToken,
                    new java.util.HashMap<>() {{
                        put("name", "수정된 팀");
                    }})
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("수정된 팀"));
        }
    }
}
