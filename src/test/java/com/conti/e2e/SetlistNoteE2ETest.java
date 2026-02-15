package com.conti.e2e;

import com.conti.domain.setlist.dto.SetlistCreateRequest;
import com.conti.domain.setlist.dto.SetlistNoteCreateRequest;
import com.conti.domain.setlist.dto.SetlistNoteUpdateRequest;
import com.conti.domain.setlist.entity.WorshipType;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("세트리스트 노트 E2E 테스트")
class SetlistNoteE2ETest extends BaseE2ETest {

    private User admin;
    private User viewer;
    private String adminToken;
    private String viewerToken;
    private Long teamId;
    private Long setlistId;

    @BeforeEach
    void setUp() throws Exception {
        admin = createUser("note-admin@test.com", "노트 관리자");
        viewer = createUser("note-viewer@test.com", "노트 뷰어");
        adminToken = getToken(admin.getId());
        viewerToken = getToken(viewer.getId());
        Team team = createTeamWithAdmin(admin.getId());
        teamId = team.getId();
        addTeamMember(viewer.getId(), teamId, TeamRole.VIEWER);

        // 콘티 생성
        SetlistCreateRequest setlistRequest = new SetlistCreateRequest(
                "노트 테스트 콘티", LocalDate.of(2026, 3, 1), WorshipType.SUNDAY_1ST, admin.getId(), null
        );
        MvcResult setlistResult = performPost("/api/v1/teams/" + teamId + "/setlists", adminToken, setlistRequest)
                .andExpect(status().isOk())
                .andReturn();
        setlistId = ((Number) JsonPath.read(setlistResult.getResponse().getContentAsString(), "$.data.id")).longValue();
    }

    @Nested
    @DisplayName("노트 생성")
    class CreateNote {

        @Test
        @DisplayName("ADMIN이 전체 공유 노트를 생성한다")
        void adminCreatesSharedNote() throws Exception {
            SetlistNoteCreateRequest request = new SetlistNoteCreateRequest("전체 공유 노트 내용", null);

            performPost("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/notes", adminToken, request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").value("전체 공유 노트 내용"))
                    .andExpect(jsonPath("$.data.authorName").value("노트 관리자"))
                    .andExpect(jsonPath("$.data.id").isNumber());
        }

        @Test
        @DisplayName("ADMIN이 포지션별 노트를 생성한다")
        void adminCreatesPositionNote() throws Exception {
            SetlistNoteCreateRequest request = new SetlistNoteCreateRequest("어쿠스틱 기타 솔로 노트", "ACOUSTIC_GUITAR");

            performPost("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/notes", adminToken, request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").value("어쿠스틱 기타 솔로 노트"))
                    .andExpect(jsonPath("$.data.position").value("ACOUSTIC_GUITAR"));
        }

        @Test
        @DisplayName("내용 없이 생성 시 400 에러를 반환한다")
        void createWithoutContentReturns400() throws Exception {
            SetlistNoteCreateRequest request = new SetlistNoteCreateRequest(null, null);

            performPost("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/notes", adminToken, request)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("VIEWER는 노트를 생성할 수 없다 (403) - EDITOR 이상 필요")
        void viewerCannotCreateNote() throws Exception {
            SetlistNoteCreateRequest request = new SetlistNoteCreateRequest("뷰어 노트", null);

            performPost("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/notes", viewerToken, request)
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("노트 목록 조회")
    class GetNotes {

        @Test
        @DisplayName("전체 노트 목록을 조회한다")
        void getAllNotes() throws Exception {
            // 노트 2개 생성
            performPost("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/notes", adminToken,
                    new SetlistNoteCreateRequest("노트 1", null));
            performPost("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/notes", adminToken,
                    new SetlistNoteCreateRequest("노트 2", "VOCAL"));

            performGet("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/notes", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)));
        }

        @Test
        @DisplayName("포지션 필터로 노트를 조회한다")
        void getNotesWithPositionFilter() throws Exception {
            performPost("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/notes", adminToken,
                    new SetlistNoteCreateRequest("기타 노트", "ACOUSTIC_GUITAR"));
            performPost("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/notes", adminToken,
                    new SetlistNoteCreateRequest("보컬 노트", "VOCAL"));

            performGet("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/notes?position=ACOUSTIC_GUITAR", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].position").value("ACOUSTIC_GUITAR"));
        }

        @Test
        @DisplayName("VIEWER도 노트 목록을 조회할 수 있다")
        void viewerCanGetNotes() throws Exception {
            performGet("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/notes", viewerToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("노트 수정")
    class UpdateNote {

        @Test
        @DisplayName("ADMIN이 노트를 수정한다")
        void adminUpdatesNote() throws Exception {
            MvcResult result = performPost("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/notes", adminToken,
                    new SetlistNoteCreateRequest("원래 노트", null))
                    .andExpect(status().isOk())
                    .andReturn();
            Long noteId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data.id")).longValue();

            SetlistNoteUpdateRequest updateRequest = new SetlistNoteUpdateRequest("수정된 노트", "VOCAL");

            performPatch("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/notes/" + noteId, adminToken, updateRequest)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").value("수정된 노트"))
                    .andExpect(jsonPath("$.data.position").value("VOCAL"));
        }
    }

    @Nested
    @DisplayName("노트 삭제")
    class DeleteNote {

        @Test
        @DisplayName("ADMIN이 노트를 삭제한다")
        void adminDeletesNote() throws Exception {
            MvcResult result = performPost("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/notes", adminToken,
                    new SetlistNoteCreateRequest("삭제할 노트", null))
                    .andExpect(status().isOk())
                    .andReturn();
            Long noteId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data.id")).longValue();

            performDelete("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/notes/" + noteId, adminToken)
                    .andExpect(status().isOk());

            // 삭제 후 목록 확인
            performGet("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/notes", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        @DisplayName("VIEWER는 노트를 삭제할 수 없다 (403)")
        void viewerCannotDeleteNote() throws Exception {
            MvcResult result = performPost("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/notes", adminToken,
                    new SetlistNoteCreateRequest("삭제 불가 노트", null))
                    .andExpect(status().isOk())
                    .andReturn();
            Long noteId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data.id")).longValue();

            performDelete("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/notes/" + noteId, viewerToken)
                    .andExpect(status().isForbidden());
        }
    }
}
