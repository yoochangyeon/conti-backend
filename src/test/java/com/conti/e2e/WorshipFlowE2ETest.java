package com.conti.e2e;

import com.conti.domain.setlist.dto.SetlistCreateRequest;
import com.conti.domain.setlist.dto.SetlistItemRequest;
import com.conti.domain.setlist.dto.ReorderRequest;
import com.conti.domain.song.dto.SongCreateRequest;
import com.conti.domain.team.dto.TeamCreateRequest;
import com.conti.domain.team.entity.Team;
import com.conti.domain.user.entity.User;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("예배 준비 전체 플로우 E2E 테스트")
class WorshipFlowE2ETest extends BaseE2ETest {

    private User user;
    private String token;

    @BeforeEach
    void setUp() {
        user = createUser("worship-leader@test.com", "예배인도자");
        token = getToken(user.getId());
    }

    @Test
    @DisplayName("팀 생성 -> 곡 등록 -> 콘티 작성 -> 곡 추가 -> 순서 변경 전체 플로우")
    void completeWorshipPreparationFlow() throws Exception {
        // 1. 팀 생성
        TeamCreateRequest teamRequest = new TeamCreateRequest("은혜교회 찬양팀", "주일예배 찬양팀");
        MvcResult teamResult = performPost("/api/v1/teams", token, teamRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("은혜교회 찬양팀"))
                .andExpect(jsonPath("$.data.inviteCode").isNotEmpty())
                .andReturn();

        Long teamId = ((Number) JsonPath.read(teamResult.getResponse().getContentAsString(), "$.data.id")).longValue();

        // 2. 곡 3개 등록 (각각 다른 태그)
        SongCreateRequest song1Request = new SongCreateRequest(
                "이 땅의 모든 찬양", "마커스", "G", 120,
                "경배 곡 메모", "https://youtube.com/1", null, List.of("경배")
        );
        MvcResult song1Result = performPost("/api/v1/teams/" + teamId + "/songs", token, song1Request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("이 땅의 모든 찬양"))
                .andExpect(jsonPath("$.data.tags[0]").value("경배"))
                .andReturn();
        Long song1Id = ((Number) JsonPath.read(song1Result.getResponse().getContentAsString(), "$.data.id")).longValue();

        SongCreateRequest song2Request = new SongCreateRequest(
                "감사해", "찬미워십", "D", 100,
                "감사 곡 메모", null, null, List.of("감사")
        );
        MvcResult song2Result = performPost("/api/v1/teams/" + teamId + "/songs", token, song2Request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("감사해"))
                .andReturn();
        Long song2Id = ((Number) JsonPath.read(song2Result.getResponse().getContentAsString(), "$.data.id")).longValue();

        SongCreateRequest song3Request = new SongCreateRequest(
                "주님의 영", "어노인팅", "C", 80,
                "찬양 곡 메모", null, null, List.of("찬양")
        );
        MvcResult song3Result = performPost("/api/v1/teams/" + teamId + "/songs", token, song3Request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("주님의 영"))
                .andReturn();
        Long song3Id = ((Number) JsonPath.read(song3Result.getResponse().getContentAsString(), "$.data.id")).longValue();

        // 3. 곡 목록 조회 -> 3개 확인
        performGet("/api/v1/teams/" + teamId + "/songs?page=0&size=10", token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.totalElements").value(3));

        // 4. 곡 상세 조회 -> 모든 필드 확인
        performGet("/api/v1/teams/" + teamId + "/songs/" + song1Id, token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("이 땅의 모든 찬양"))
                .andExpect(jsonPath("$.data.artist").value("마커스"))
                .andExpect(jsonPath("$.data.originalKey").value("G"))
                .andExpect(jsonPath("$.data.bpm").value(120))
                .andExpect(jsonPath("$.data.memo").value("경배 곡 메모"))
                .andExpect(jsonPath("$.data.youtubeUrl").value("https://youtube.com/1"))
                .andExpect(jsonPath("$.data.tags[0]").value("경배"))
                .andExpect(jsonPath("$.data.usageCount").value(0));

        // 5. 콘티(세트리스트) 생성
        SetlistCreateRequest setlistRequest = new SetlistCreateRequest(
                "주일 1부 예배 콘티", LocalDate.of(2026, 2, 15), "주일1부", user.getId(), "이번 주 콘티"
        );
        MvcResult setlistResult = performPost("/api/v1/teams/" + teamId + "/setlists", token, setlistRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("주일 1부 예배 콘티"))
                .andExpect(jsonPath("$.data.worshipDate").value("2026-02-15"))
                .andReturn();
        Long setlistId = ((Number) JsonPath.read(setlistResult.getResponse().getContentAsString(), "$.data.id")).longValue();

        // 6. 콘티에 곡 2개 추가
        SetlistItemRequest item1Request = new SetlistItemRequest(song1Id, "G", "키 유지");
        MvcResult item1Result = performPost("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/items", token, item1Request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.songTitle").value("이 땅의 모든 찬양"))
                .andExpect(jsonPath("$.data.orderIndex").value(0))
                .andReturn();
        Long item1Id = ((Number) JsonPath.read(item1Result.getResponse().getContentAsString(), "$.data.id")).longValue();

        SetlistItemRequest item2Request = new SetlistItemRequest(song2Id, "C", "반음 내림");
        MvcResult item2Result = performPost("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/items", token, item2Request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.songTitle").value("감사해"))
                .andExpect(jsonPath("$.data.orderIndex").value(1))
                .andReturn();
        Long item2Id = ((Number) JsonPath.read(item2Result.getResponse().getContentAsString(), "$.data.id")).longValue();

        // 7. 순서 변경 (item2를 먼저, item1을 뒤로)
        ReorderRequest reorderRequest = new ReorderRequest(List.of(item2Id, item1Id));
        performPatch("/api/v1/teams/" + teamId + "/setlists/" + setlistId + "/items/reorder", token, reorderRequest)
                .andExpect(status().isOk());

        // 8. 콘티 상세 조회 -> 순서 변경 확인
        performGet("/api/v1/teams/" + teamId + "/setlists/" + setlistId, token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("주일 1부 예배 콘티"))
                .andExpect(jsonPath("$.data.items", hasSize(2)))
                .andExpect(jsonPath("$.data.songCount").value(2));

        // 9. 곡 사용 이력 조회 -> 콘티에 추가된 곡의 usage 확인
        performGet("/api/v1/teams/" + teamId + "/songs/" + song1Id + "/usages", token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].setlistId").value(setlistId));

        // 10. 팀 태그 목록 조회 -> 3개 태그 확인
        performGet("/api/v1/teams/" + teamId + "/tags", token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3)));
    }

    @Test
    @DisplayName("곡 검색 필터 테스트 - 키워드 검색")
    void songSearchByKeyword() throws Exception {
        Team team = createTeamWithAdmin(user.getId());
        Long teamId = team.getId();

        // 곡 2개 등록
        SongCreateRequest req1 = new SongCreateRequest(
                "Amazing Grace", "전통찬송", "C", 80, null, null, null, List.of("찬양")
        );
        SongCreateRequest req2 = new SongCreateRequest(
                "은혜 아니면", "마커스", "D", 100, null, null, null, List.of("경배")
        );
        performPost("/api/v1/teams/" + teamId + "/songs", token, req1);
        performPost("/api/v1/teams/" + teamId + "/songs", token, req2);

        // 키워드 "Grace" 검색 -> 1건
        performGet("/api/v1/teams/" + teamId + "/songs?keyword=Grace&page=0&size=10", token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title").value("Amazing Grace"));

        // 아티스트 "마커스" 검색 -> 1건
        performGet("/api/v1/teams/" + teamId + "/songs?keyword=마커스&page=0&size=10", token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].artist").value("마커스"));
    }
}
