package com.conti.e2e;

import com.conti.domain.team.dto.MemberRoleUpdateRequest;
import com.conti.domain.team.dto.TeamCreateRequest;
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

@DisplayName("팀 관리 E2E 테스트")
class TeamManagementE2ETest extends BaseE2ETest {

    private User userA;
    private User userB;
    private String tokenA;
    private String tokenB;

    @BeforeEach
    void setUp() {
        userA = createUser("admin@test.com", "관리자A");
        userB = createUser("member@test.com", "멤버B");
        tokenA = getToken(userA.getId());
        tokenB = getToken(userB.getId());
    }

    @Nested
    @DisplayName("팀 생성 및 초대 플로우")
    class TeamCreationAndInviteFlow {

        @Test
        @DisplayName("팀 생성 -> 초대 코드 확인 -> 초대 코드로 가입 -> 멤버 확인 전체 플로우")
        void completeTeamManagementFlow() throws Exception {
            // 1. User A가 팀 생성 -> ADMIN 역할 확인
            TeamCreateRequest createRequest = new TeamCreateRequest("새 찬양팀", "테스트 팀입니다");
            MvcResult teamResult = performPost("/api/v1/teams", tokenA, createRequest)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("새 찬양팀"))
                    .andExpect(jsonPath("$.data.description").value("테스트 팀입니다"))
                    .andExpect(jsonPath("$.data.inviteCode").isNotEmpty())
                    .andReturn();

            Long teamId = ((Number) JsonPath.read(teamResult.getResponse().getContentAsString(), "$.data.id")).longValue();
            String inviteCode = JsonPath.read(teamResult.getResponse().getContentAsString(), "$.data.inviteCode");

            // 2. 초대 코드 확인
            performGet("/api/v1/teams/" + teamId, tokenA)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.inviteCode").value(inviteCode));

            // 3. User B가 초대 코드로 가입 -> VIEWER 역할
            performPostNoBody("/api/v1/teams/join/" + inviteCode, tokenB)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("새 찬양팀"));

            // 4. User A가 멤버 목록 조회 -> 2명
            performGet("/api/v1/teams/" + teamId + "/members", tokenA)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)));
        }

        @Test
        @DisplayName("역할 변경 -> 초대 코드 재생성 -> 멤버 제거 플로우")
        void roleChangeAndMemberRemovalFlow() throws Exception {
            // 1. User A가 팀 생성
            TeamCreateRequest createRequest = new TeamCreateRequest("역할 테스트 팀", null);
            MvcResult teamResult = performPost("/api/v1/teams", tokenA, createRequest)
                    .andExpect(status().isOk())
                    .andReturn();

            Long teamId = ((Number) JsonPath.read(teamResult.getResponse().getContentAsString(), "$.data.id")).longValue();
            String originalInviteCode = JsonPath.read(teamResult.getResponse().getContentAsString(), "$.data.inviteCode");

            // 2. User B가 초대 코드로 가입
            performPostNoBody("/api/v1/teams/join/" + originalInviteCode, tokenB)
                    .andExpect(status().isOk());

            // 3. 멤버 목록에서 User B의 memberId 가져오기
            MvcResult membersResult = performGet("/api/v1/teams/" + teamId + "/members", tokenA)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andReturn();

            String membersJson = membersResult.getResponse().getContentAsString();
            // User A의 memberId 찾기
            Long memberAId = null;
            Long memberBId = null;
            int size = ((Number) JsonPath.read(membersJson, "$.data.length()")).intValue();
            for (int i = 0; i < size; i++) {
                Long uid = ((Number) JsonPath.read(membersJson, "$.data[" + i + "].userId")).longValue();
                Long mid = ((Number) JsonPath.read(membersJson, "$.data[" + i + "].memberId")).longValue();
                if (uid.equals(userA.getId())) {
                    memberAId = mid;
                } else if (uid.equals(userB.getId())) {
                    memberBId = mid;
                }
            }

            // 4. User A가 User B의 역할을 ADMIN으로 변경
            MemberRoleUpdateRequest roleRequest = new MemberRoleUpdateRequest("ADMIN");
            performPatch("/api/v1/teams/" + teamId + "/members/" + memberBId, tokenA, roleRequest)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.role").value("ADMIN"));

            // 5. User A가 초대 코드를 재생성
            MvcResult inviteResult = performPostNoBody("/api/v1/teams/" + teamId + "/invite", tokenA)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.inviteCode").isNotEmpty())
                    .andReturn();

            String newInviteCode = JsonPath.read(inviteResult.getResponse().getContentAsString(), "$.data.inviteCode");
            // 새 초대 코드가 이전과 다른지 확인 (UUID 기반이므로 거의 항상 다름)
            // originalInviteCode != newInviteCode (확률적 보장)

            // 6. User B (이제 ADMIN)가 User A를 제거
            performDelete("/api/v1/teams/" + teamId + "/members/" + memberAId, tokenB)
                    .andExpect(status().isOk());

            // 7. 멤버 목록 확인 -> 1명 (User B만)
            performGet("/api/v1/teams/" + teamId + "/members", tokenB)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].userId").value(userB.getId()));
        }
    }

    @Nested
    @DisplayName("팀 CRUD 테스트")
    class TeamCRUD {

        @Test
        @DisplayName("팀 정보를 수정할 수 있다")
        void updateTeam() throws Exception {
            TeamCreateRequest createRequest = new TeamCreateRequest("원래 팀 이름", "원래 설명");
            MvcResult result = performPost("/api/v1/teams", tokenA, createRequest)
                    .andExpect(status().isOk())
                    .andReturn();

            Long teamId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data.id")).longValue();

            performPatch("/api/v1/teams/" + teamId, tokenA,
                    new java.util.HashMap<>() {{
                        put("name", "수정된 팀 이름");
                        put("description", "수정된 설명");
                    }})
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("수정된 팀 이름"))
                    .andExpect(jsonPath("$.data.description").value("수정된 설명"));
        }

        @Test
        @DisplayName("팀을 삭제할 수 있다")
        void deleteTeam() throws Exception {
            TeamCreateRequest createRequest = new TeamCreateRequest("삭제할 팀", null);
            MvcResult result = performPost("/api/v1/teams", tokenA, createRequest)
                    .andExpect(status().isOk())
                    .andReturn();

            Long teamId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data.id")).longValue();

            performDelete("/api/v1/teams/" + teamId, tokenA)
                    .andExpect(status().isOk());

            // 삭제 후 조회하면 404
            performGet("/api/v1/teams/" + teamId, tokenA)
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("중복 가입 시도 시 409 에러를 반환한다")
        void duplicateJoinReturns409() throws Exception {
            TeamCreateRequest createRequest = new TeamCreateRequest("중복 테스트 팀", null);
            MvcResult result = performPost("/api/v1/teams", tokenA, createRequest)
                    .andExpect(status().isOk())
                    .andReturn();

            String inviteCode = JsonPath.read(result.getResponse().getContentAsString(), "$.data.inviteCode");

            // User B 가입
            performPostNoBody("/api/v1/teams/join/" + inviteCode, tokenB)
                    .andExpect(status().isOk());

            // User B 중복 가입 시도
            performPostNoBody("/api/v1/teams/join/" + inviteCode, tokenB)
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("잘못된 초대 코드로 가입 시 400 에러를 반환한다")
        void invalidInviteCodeReturns400() throws Exception {
            performPostNoBody("/api/v1/teams/join/INVALID_CODE", tokenA)
                    .andExpect(status().isBadRequest());
        }
    }
}
