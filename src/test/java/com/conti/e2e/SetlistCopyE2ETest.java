package com.conti.e2e;

import com.conti.domain.setlist.dto.SetlistCopyRequest;
import com.conti.domain.setlist.dto.SetlistCreateRequest;
import com.conti.domain.setlist.dto.SetlistItemRequest;
import com.conti.domain.setlist.entity.SetlistItemType;
import com.conti.domain.setlist.entity.WorshipType;
import com.conti.domain.song.dto.SongCreateRequest;
import com.conti.domain.team.entity.Team;
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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("콘티 복사 E2E 테스트")
class SetlistCopyE2ETest extends BaseE2ETest {

    private User admin;
    private User viewer;
    private String adminToken;
    private String viewerToken;
    private Long teamId;

    @BeforeEach
    void setUp() {
        admin = createUser("copy-admin@test.com", "복사 관리자");
        viewer = createUser("copy-viewer@test.com", "복사 뷰어");
        adminToken = getToken(admin.getId());
        viewerToken = getToken(viewer.getId());
        Team team = createTeamWithAdmin(admin.getId());
        teamId = team.getId();
        addTeamMember(viewer.getId(), teamId, TeamRole.VIEWER);
    }

    @Nested
    @DisplayName("콘티 복사")
    class CopySetlist {

        @Test
        @DisplayName("ADMIN이 콘티를 복사한다")
        void adminCopiesSetlist() throws Exception {
            // 곡 생성
            SongCreateRequest songRequest = new SongCreateRequest(
                    "복사 테스트 곡", "아티스트", "G", 120, null, null, null, null, null
            );
            MvcResult songResult = performPost("/api/v1/teams/" + teamId + "/songs", adminToken, songRequest)
                    .andExpect(status().isOk())
                    .andReturn();
            Long songId = ((Number) JsonPath.read(songResult.getResponse().getContentAsString(), "$.data.id")).longValue();

            // 콘티 생성
            SetlistCreateRequest setlistRequest = new SetlistCreateRequest(
                    "원본 콘티", LocalDate.of(2026, 2, 15), WorshipType.SUNDAY_1ST, admin.getId(), "원본 메모"
            );
            MvcResult setlistResult = performPost("/api/v1/teams/" + teamId + "/setlists", adminToken, setlistRequest)
                    .andExpect(status().isOk())
                    .andReturn();
            Long setlistId = ((Number) JsonPath.read(setlistResult.getResponse().getContentAsString(), "$.data.id")).longValue();

            // 콘티에 곡 추가
            SetlistItemRequest itemRequest = new SetlistItemRequest(SetlistItemType.SONG, songId, null, "G", null, null, null, null);
            performPost("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/items", adminToken, itemRequest)
                    .andExpect(status().isOk());

            // 콘티 복사
            SetlistCopyRequest copyRequest = new SetlistCopyRequest("복사된 콘티", LocalDate.of(2026, 2, 22), WorshipType.SUNDAY_1ST);

            performPost("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/copy", adminToken, copyRequest)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.worshipDate").value("2026-02-22"))
                    .andExpect(jsonPath("$.data.id").isNumber());
        }

        @Test
        @DisplayName("VIEWER는 콘티를 복사할 수 없다 (403)")
        void viewerCannotCopySetlist() throws Exception {
            // 콘티 생성
            SetlistCreateRequest setlistRequest = new SetlistCreateRequest(
                    "복사 불가 콘티", LocalDate.of(2026, 2, 15), WorshipType.SUNDAY_1ST, admin.getId(), null
            );
            MvcResult setlistResult = performPost("/api/v1/teams/" + teamId + "/setlists", adminToken, setlistRequest)
                    .andExpect(status().isOk())
                    .andReturn();
            Long setlistId = ((Number) JsonPath.read(setlistResult.getResponse().getContentAsString(), "$.data.id")).longValue();

            SetlistCopyRequest copyRequest = new SetlistCopyRequest("불법 복사", LocalDate.of(2026, 3, 1), null);

            performPost("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/copy", viewerToken, copyRequest)
                    .andExpect(status().isForbidden());
        }
    }
}
