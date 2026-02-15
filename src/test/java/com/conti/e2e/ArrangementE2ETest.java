package com.conti.e2e;

import com.conti.domain.song.dto.ArrangementCreateRequest;
import com.conti.domain.song.dto.ArrangementUpdateRequest;
import com.conti.domain.song.dto.SongCreateRequest;
import com.conti.domain.song.dto.SongSectionRequest;
import com.conti.domain.team.entity.Team;
import com.conti.domain.team.entity.TeamRole;
import com.conti.domain.user.entity.User;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("편곡 E2E 테스트")
class ArrangementE2ETest extends BaseE2ETest {

    private User admin;
    private User viewer;
    private String adminToken;
    private String viewerToken;
    private Long teamId;
    private Long songId;

    @BeforeEach
    void setUp() throws Exception {
        admin = createUser("arr-admin@test.com", "편곡 관리자");
        viewer = createUser("arr-viewer@test.com", "편곡 뷰어");
        adminToken = getToken(admin.getId());
        viewerToken = getToken(viewer.getId());
        Team team = createTeamWithAdmin(admin.getId());
        teamId = team.getId();
        addTeamMember(viewer.getId(), teamId, TeamRole.VIEWER);

        // 곡 생성
        SongCreateRequest songRequest = new SongCreateRequest(
                "편곡 테스트 곡", "아티스트", "C", 120, null, null, null, null, null
        );
        MvcResult songResult = performPost("/api/v1/teams/" + teamId + "/songs", adminToken, songRequest)
                .andExpect(status().isOk())
                .andReturn();
        songId = ((Number) JsonPath.read(songResult.getResponse().getContentAsString(), "$.data.id")).longValue();
    }

    @Nested
    @DisplayName("편곡 생성")
    class CreateArrangement {

        @Test
        @DisplayName("ADMIN이 편곡을 생성한다")
        void adminCreatesArrangement() throws Exception {
            ArrangementCreateRequest request = new ArrangementCreateRequest(
                    "어쿠스틱 버전", "G", 100, "4/4", 5, "어쿠스틱 기타 중심",
                    List.of(new SongSectionRequest("VERSE", 0, "1절", "G - D - Em - C", 2, null))
            );

            performPost("/api/v1/teams/" + teamId + "/songs/" + songId + "/arrangements", adminToken, request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("어쿠스틱 버전"))
                    .andExpect(jsonPath("$.data.songKey").value("G"))
                    .andExpect(jsonPath("$.data.bpm").value(100))
                    .andExpect(jsonPath("$.data.meter").value("4/4"))
                    .andExpect(jsonPath("$.data.durationMinutes").value(5))
                    .andExpect(jsonPath("$.data.id").isNumber());
        }

        @Test
        @DisplayName("이름 없이 생성 시 400 에러를 반환한다")
        void createWithoutNameReturns400() throws Exception {
            ArrangementCreateRequest request = new ArrangementCreateRequest(
                    null, "G", 100, null, null, null, null
            );

            performPost("/api/v1/teams/" + teamId + "/songs/" + songId + "/arrangements", adminToken, request)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("VIEWER는 편곡을 생성할 수 없다 (403)")
        void viewerCannotCreateArrangement() throws Exception {
            ArrangementCreateRequest request = new ArrangementCreateRequest(
                    "뷰어 편곡", "G", 100, null, null, null, null
            );

            performPost("/api/v1/teams/" + teamId + "/songs/" + songId + "/arrangements", viewerToken, request)
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("편곡 목록 조회")
    class GetArrangements {

        @Test
        @DisplayName("편곡 목록을 조회한다 (기본 편곡 포함)")
        void getArrangements() throws Exception {
            performGet("/api/v1/teams/" + teamId + "/songs/" + songId + "/arrangements", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("VIEWER도 편곡 목록을 조회할 수 있다")
        void viewerCanGetArrangements() throws Exception {
            performGet("/api/v1/teams/" + teamId + "/songs/" + songId + "/arrangements", viewerToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("편곡 수정")
    class UpdateArrangement {

        @Test
        @DisplayName("ADMIN이 편곡을 수정한다")
        void adminUpdatesArrangement() throws Exception {
            // 편곡 생성
            ArrangementCreateRequest createRequest = new ArrangementCreateRequest(
                    "원래 편곡", "C", 120, "4/4", null, null, null
            );
            MvcResult result = performPost("/api/v1/teams/" + teamId + "/songs/" + songId + "/arrangements", adminToken, createRequest)
                    .andExpect(status().isOk())
                    .andReturn();
            Long arrangementId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data.id")).longValue();

            // 수정
            ArrangementUpdateRequest updateRequest = new ArrangementUpdateRequest(
                    "수정된 편곡", "D", 130, "3/4", 7, "수정된 설명", null
            );

            performPatch("/api/v1/teams/" + teamId + "/songs/" + songId + "/arrangements/" + arrangementId, adminToken, updateRequest)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("수정된 편곡"))
                    .andExpect(jsonPath("$.data.songKey").value("D"))
                    .andExpect(jsonPath("$.data.bpm").value(130));
        }
    }

    @Nested
    @DisplayName("편곡 삭제")
    class DeleteArrangement {

        @Test
        @DisplayName("ADMIN이 편곡을 삭제한다")
        void adminDeletesArrangement() throws Exception {
            ArrangementCreateRequest request = new ArrangementCreateRequest(
                    "삭제할 편곡", "C", 120, null, null, null, null
            );
            MvcResult result = performPost("/api/v1/teams/" + teamId + "/songs/" + songId + "/arrangements", adminToken, request)
                    .andExpect(status().isOk())
                    .andReturn();
            Long arrangementId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data.id")).longValue();

            performDelete("/api/v1/teams/" + teamId + "/songs/" + songId + "/arrangements/" + arrangementId, adminToken)
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("VIEWER는 편곡을 삭제할 수 없다 (403)")
        void viewerCannotDeleteArrangement() throws Exception {
            ArrangementCreateRequest request = new ArrangementCreateRequest(
                    "삭제 불가 편곡", "C", 120, null, null, null, null
            );
            MvcResult result = performPost("/api/v1/teams/" + teamId + "/songs/" + songId + "/arrangements", adminToken, request)
                    .andExpect(status().isOk())
                    .andReturn();
            Long arrangementId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data.id")).longValue();

            performDelete("/api/v1/teams/" + teamId + "/songs/" + songId + "/arrangements/" + arrangementId, viewerToken)
                    .andExpect(status().isForbidden());
        }
    }
}
