package com.conti.e2e;

import com.conti.domain.setlist.dto.SetlistTemplateCreateRequest;
import com.conti.domain.setlist.dto.SetlistTemplateItemRequest;
import com.conti.domain.setlist.entity.SetlistItemType;
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

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("콘티 템플릿 E2E 테스트")
class SetlistTemplateE2ETest extends BaseE2ETest {

    private User admin;
    private User viewer;
    private String adminToken;
    private String viewerToken;
    private Long teamId;

    @BeforeEach
    void setUp() {
        admin = createUser("template-admin@test.com", "템플릿 관리자");
        viewer = createUser("template-viewer@test.com", "템플릿 뷰어");
        adminToken = getToken(admin.getId());
        viewerToken = getToken(viewer.getId());
        Team team = createTeamWithAdmin(admin.getId());
        teamId = team.getId();
        addTeamMember(viewer.getId(), teamId, TeamRole.VIEWER);
    }

    @Nested
    @DisplayName("템플릿 생성")
    class CreateTemplate {

        @Test
        @DisplayName("ADMIN이 템플릿을 생성한다")
        void adminCreatesTemplate() throws Exception {
            SetlistTemplateCreateRequest request = new SetlistTemplateCreateRequest(
                    "주일 1부 기본 콘티", "기본 예배 순서", WorshipType.SUNDAY_1ST,
                    List.of(
                            new SetlistTemplateItemRequest(SetlistItemType.PRAYER, null, "대표 기도", "대표 기도 시간", 5, null, null),
                            new SetlistTemplateItemRequest(SetlistItemType.SONG, null, "경배 찬양", null, null, null, null),
                            new SetlistTemplateItemRequest(SetlistItemType.SERMON, null, "말씀", "설교 시간", 30, null, null)
                    )
            );

            performPost("/api/v1/teams/" + teamId + "/setlist-templates", adminToken, request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("주일 1부 기본 콘티"))
                    .andExpect(jsonPath("$.data.description").value("기본 예배 순서"))
                    .andExpect(jsonPath("$.data.worshipType").value("SUNDAY_1ST"))
                    .andExpect(jsonPath("$.data.itemCount").value(3))
                    .andExpect(jsonPath("$.data.items", hasSize(3)))
                    .andExpect(jsonPath("$.data.id").isNumber());
        }

        @Test
        @DisplayName("이름 없이 생성 시 400 에러를 반환한다")
        void createWithoutNameReturns400() throws Exception {
            SetlistTemplateCreateRequest request = new SetlistTemplateCreateRequest(
                    null, "설명", WorshipType.SUNDAY_1ST, null
            );

            performPost("/api/v1/teams/" + teamId + "/setlist-templates", adminToken, request)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("VIEWER는 템플릿을 생성할 수 없다 (403)")
        void viewerCannotCreateTemplate() throws Exception {
            SetlistTemplateCreateRequest request = new SetlistTemplateCreateRequest(
                    "뷰어 템플릿", null, null, null
            );

            performPost("/api/v1/teams/" + teamId + "/setlist-templates", viewerToken, request)
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("템플릿 목록 조회")
    class GetTemplates {

        @Test
        @DisplayName("ADMIN이 템플릿 목록을 조회한다")
        void adminGetsTemplates() throws Exception {
            // 템플릿 2개 생성
            for (int i = 1; i <= 2; i++) {
                SetlistTemplateCreateRequest request = new SetlistTemplateCreateRequest(
                        "템플릿 " + i, null, null, null
                );
                performPost("/api/v1/teams/" + teamId + "/setlist-templates", adminToken, request);
            }

            performGet("/api/v1/teams/" + teamId + "/setlist-templates", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)));
        }

        @Test
        @DisplayName("VIEWER도 템플릿 목록을 조회할 수 있다")
        void viewerCanGetTemplates() throws Exception {
            performGet("/api/v1/teams/" + teamId + "/setlist-templates", viewerToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("템플릿 수정")
    class UpdateTemplate {

        @Test
        @DisplayName("ADMIN이 템플릿을 수정한다")
        void adminUpdatesTemplate() throws Exception {
            SetlistTemplateCreateRequest createRequest = new SetlistTemplateCreateRequest(
                    "원래 템플릿", "원래 설명", null, null
            );
            MvcResult result = performPost("/api/v1/teams/" + teamId + "/setlist-templates", adminToken, createRequest)
                    .andExpect(status().isOk())
                    .andReturn();
            Long templateId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data.id")).longValue();

            SetlistTemplateCreateRequest updateRequest = new SetlistTemplateCreateRequest(
                    "수정된 템플릿", "수정된 설명", WorshipType.SUNDAY_2ND,
                    List.of(new SetlistTemplateItemRequest(SetlistItemType.SONG, null, "찬양", null, null, null, null))
            );

            performPut("/api/v1/teams/" + teamId + "/setlist-templates/" + templateId, adminToken, updateRequest)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("수정된 템플릿"))
                    .andExpect(jsonPath("$.data.description").value("수정된 설명"));
        }
    }

    @Nested
    @DisplayName("템플릿 삭제")
    class DeleteTemplate {

        @Test
        @DisplayName("ADMIN이 템플릿을 삭제한다")
        void adminDeletesTemplate() throws Exception {
            SetlistTemplateCreateRequest request = new SetlistTemplateCreateRequest(
                    "삭제할 템플릿", null, null, null
            );
            MvcResult result = performPost("/api/v1/teams/" + teamId + "/setlist-templates", adminToken, request)
                    .andExpect(status().isOk())
                    .andReturn();
            Long templateId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data.id")).longValue();

            performDelete("/api/v1/teams/" + teamId + "/setlist-templates/" + templateId, adminToken)
                    .andExpect(status().isOk());

            // 삭제 후 목록에서 사라짐
            performGet("/api/v1/teams/" + teamId + "/setlist-templates", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        @DisplayName("VIEWER는 템플릿을 삭제할 수 없다 (403)")
        void viewerCannotDeleteTemplate() throws Exception {
            SetlistTemplateCreateRequest request = new SetlistTemplateCreateRequest(
                    "삭제 테스트 템플릿", null, null, null
            );
            MvcResult result = performPost("/api/v1/teams/" + teamId + "/setlist-templates", adminToken, request)
                    .andExpect(status().isOk())
                    .andReturn();
            Long templateId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data.id")).longValue();

            performDelete("/api/v1/teams/" + teamId + "/setlist-templates/" + templateId, viewerToken)
                    .andExpect(status().isForbidden());
        }
    }
}
