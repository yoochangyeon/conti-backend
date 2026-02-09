package com.conti.e2e;

import com.conti.domain.song.dto.SongCreateRequest;
import com.conti.domain.song.dto.SongUpdateRequest;
import com.conti.domain.team.entity.Team;
import com.conti.domain.user.entity.User;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("곡 CRUD E2E 테스트")
class SongCrudE2ETest extends BaseE2ETest {

    private User user;
    private String token;
    private Long teamId;

    @BeforeEach
    void setUp() {
        user = createUser("song-admin@test.com", "곡 관리자");
        token = getToken(user.getId());
        Team team = createTeamWithAdmin(user.getId());
        teamId = team.getId();
    }

    @Nested
    @DisplayName("곡 생성")
    class CreateSong {

        @Test
        @DisplayName("태그를 포함하여 곡을 생성한다")
        void createSongWithTags() throws Exception {
            SongCreateRequest request = new SongCreateRequest(
                    "이 땅의 모든 찬양", "마커스", "G", 120,
                    "좋은 경배곡입니다", "https://youtube.com/song1", "https://music.com/song1",
                    List.of("경배", "감사")
            );

            performPost("/api/v1/teams/" + teamId + "/songs", token, request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("이 땅의 모든 찬양"))
                    .andExpect(jsonPath("$.data.artist").value("마커스"))
                    .andExpect(jsonPath("$.data.originalKey").value("G"))
                    .andExpect(jsonPath("$.data.bpm").value(120))
                    .andExpect(jsonPath("$.data.tags", hasSize(2)))
                    .andExpect(jsonPath("$.data.id").isNumber())
                    .andExpect(jsonPath("$.data.createdAt").isNotEmpty());
        }

        @Test
        @DisplayName("태그 없이 최소 정보로 곡을 생성한다")
        void createSongMinimal() throws Exception {
            SongCreateRequest request = new SongCreateRequest(
                    "간단한 곡", null, null, null, null, null, null, null
            );

            performPost("/api/v1/teams/" + teamId + "/songs", token, request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value("간단한 곡"))
                    .andExpect(jsonPath("$.data.tags", hasSize(0)));
        }

        @Test
        @DisplayName("제목 없이 곡 생성 시 400 에러를 반환한다")
        void createSongWithoutTitle() throws Exception {
            SongCreateRequest request = new SongCreateRequest(
                    null, "Artist", null, null, null, null, null, null
            );

            performPost("/api/v1/teams/" + teamId + "/songs", token, request)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("빈 제목으로 곡 생성 시 400 에러를 반환한다")
        void createSongWithBlankTitle() throws Exception {
            SongCreateRequest request = new SongCreateRequest(
                    "  ", "Artist", null, null, null, null, null, null
            );

            performPost("/api/v1/teams/" + teamId + "/songs", token, request)
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("곡 상세 조회")
    class GetSongDetail {

        @Test
        @DisplayName("곡 상세 정보를 조회한다 (메모, URL, 파일, 사용횟수)")
        void getSongDetail() throws Exception {
            // 곡 생성
            SongCreateRequest request = new SongCreateRequest(
                    "상세 조회 곡", "아티스트", "D", 100,
                    "메모 내용", "https://youtube.com/detail", "https://music.com/detail",
                    List.of("경배", "찬양")
            );

            MvcResult createResult = performPost("/api/v1/teams/" + teamId + "/songs", token, request)
                    .andExpect(status().isOk())
                    .andReturn();
            Long songId = ((Number) JsonPath.read(createResult.getResponse().getContentAsString(), "$.data.id")).longValue();

            // 상세 조회
            performGet("/api/v1/teams/" + teamId + "/songs/" + songId, token)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value("상세 조회 곡"))
                    .andExpect(jsonPath("$.data.artist").value("아티스트"))
                    .andExpect(jsonPath("$.data.originalKey").value("D"))
                    .andExpect(jsonPath("$.data.bpm").value(100))
                    .andExpect(jsonPath("$.data.memo").value("메모 내용"))
                    .andExpect(jsonPath("$.data.youtubeUrl").value("https://youtube.com/detail"))
                    .andExpect(jsonPath("$.data.musicUrl").value("https://music.com/detail"))
                    .andExpect(jsonPath("$.data.tags", hasSize(2)))
                    .andExpect(jsonPath("$.data.files", hasSize(0)))
                    .andExpect(jsonPath("$.data.usageCount").value(0));
        }

        @Test
        @DisplayName("존재하지 않는 곡 조회 시 404를 반환한다")
        void getSongNotFound() throws Exception {
            performGet("/api/v1/teams/" + teamId + "/songs/999999", token)
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("곡 수정")
    class UpdateSong {

        @Test
        @DisplayName("곡 정보를 부분 수정한다 (제목만 변경)")
        void partialUpdateSong() throws Exception {
            // 곡 생성
            SongCreateRequest createRequest = new SongCreateRequest(
                    "원래 제목", "원래 아티스트", "C", 120, "원래 메모", null, null, List.of("경배")
            );
            MvcResult result = performPost("/api/v1/teams/" + teamId + "/songs", token, createRequest)
                    .andExpect(status().isOk())
                    .andReturn();
            Long songId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data.id")).longValue();

            // 제목만 수정
            SongUpdateRequest updateRequest = new SongUpdateRequest(
                    "수정된 제목", null, null, null, null, null, null, null
            );
            performPatch("/api/v1/teams/" + teamId + "/songs/" + songId, token, updateRequest)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value("수정된 제목"))
                    .andExpect(jsonPath("$.data.artist").value("원래 아티스트"))
                    .andExpect(jsonPath("$.data.originalKey").value("C"))
                    .andExpect(jsonPath("$.data.bpm").value(120));
        }

        @Test
        @DisplayName("곡의 태그를 변경한다")
        void updateSongTags() throws Exception {
            // 곡 생성
            SongCreateRequest createRequest = new SongCreateRequest(
                    "태그 변경 곡", null, null, null, null, null, null, List.of("경배", "감사")
            );
            MvcResult result = performPost("/api/v1/teams/" + teamId + "/songs", token, createRequest)
                    .andExpect(status().isOk())
                    .andReturn();
            Long songId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data.id")).longValue();

            // 태그 변경
            SongUpdateRequest updateRequest = new SongUpdateRequest(
                    null, null, null, null, null, null, null, List.of("찬양", "선포", "회개")
            );
            performPatch("/api/v1/teams/" + teamId + "/songs/" + songId, token, updateRequest)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.tags", hasSize(3)));
        }
    }

    @Nested
    @DisplayName("파일 관리")
    class FileManagement {

        @Test
        @DisplayName("파일 URL을 등록하고 삭제한다")
        void uploadAndDeleteFileByUrl() throws Exception {
            // 곡 생성
            SongCreateRequest createRequest = new SongCreateRequest(
                    "파일 테스트 곡", null, null, null, null, null, null, null
            );
            MvcResult result = performPost("/api/v1/teams/" + teamId + "/songs", token, createRequest)
                    .andExpect(status().isOk())
                    .andReturn();
            Long songId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data.id")).longValue();

            // 파일 URL 등록
            MvcResult fileResult = mockMvc.perform(post(
                    "/api/v1/teams/" + teamId + "/songs/" + songId + "/files/url")
                            .header("Authorization", "Bearer " + token)
                            .param("fileName", "악보.pdf")
                            .param("fileUrl", "https://example.com/score.pdf")
                            .param("fileType", "PDF")
                            .param("fileSize", "1024"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.fileName").value("악보.pdf"))
                    .andExpect(jsonPath("$.data.fileUrl").value("https://example.com/score.pdf"))
                    .andExpect(jsonPath("$.data.fileType").value("PDF"))
                    .andExpect(jsonPath("$.data.fileSize").value(1024))
                    .andReturn();
            Long fileId = ((Number) JsonPath.read(fileResult.getResponse().getContentAsString(), "$.data.id")).longValue();

            // 상세 조회에서 파일 확인
            performGet("/api/v1/teams/" + teamId + "/songs/" + songId, token)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.files", hasSize(1)))
                    .andExpect(jsonPath("$.data.files[0].fileName").value("악보.pdf"));

            // 파일 삭제
            performDelete("/api/v1/teams/" + teamId + "/songs/" + songId + "/files/" + fileId, token)
                    .andExpect(status().isOk());

            // 영속성 컨텍스트 초기화 (트랜잭셔널 테스트에서 캐시된 엔티티 상태 갱신)
            flushAndClear();

            // 삭제 후 파일 없음 확인
            performGet("/api/v1/teams/" + teamId + "/songs/" + songId, token)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.files", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("곡 삭제")
    class DeleteSong {

        @Test
        @DisplayName("곡을 삭제하면 조회 시 404를 반환한다")
        void deleteSongAndVerify() throws Exception {
            // 곡 생성
            SongCreateRequest createRequest = new SongCreateRequest(
                    "삭제 대상 곡", null, null, null, null, null, null, null
            );
            MvcResult result = performPost("/api/v1/teams/" + teamId + "/songs", token, createRequest)
                    .andExpect(status().isOk())
                    .andReturn();
            Long songId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data.id")).longValue();

            // 삭제
            performDelete("/api/v1/teams/" + teamId + "/songs/" + songId, token)
                    .andExpect(status().isOk());

            // 삭제 후 조회 시 404
            performGet("/api/v1/teams/" + teamId + "/songs/" + songId, token)
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("곡 목록 조회")
    class GetSongs {

        @Test
        @DisplayName("곡 목록을 페이징하여 조회한다")
        void getSongsWithPaging() throws Exception {
            // 곡 5개 생성
            for (int i = 1; i <= 5; i++) {
                SongCreateRequest request = new SongCreateRequest(
                        "곡 " + i, "아티스트 " + i, null, null, null, null, null, null
                );
                performPost("/api/v1/teams/" + teamId + "/songs", token, request);
            }

            // 페이지 크기 3으로 조회
            performGet("/api/v1/teams/" + teamId + "/songs?page=0&size=3", token)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content", hasSize(3)))
                    .andExpect(jsonPath("$.data.totalElements").value(5))
                    .andExpect(jsonPath("$.data.totalPages").value(2));

            // 두 번째 페이지
            performGet("/api/v1/teams/" + teamId + "/songs?page=1&size=3", token)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content", hasSize(2)));
        }

        @Test
        @DisplayName("빈 목록 조회 시 빈 페이지를 반환한다")
        void getEmptySongsList() throws Exception {
            performGet("/api/v1/teams/" + teamId + "/songs?page=0&size=10", token)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content", hasSize(0)))
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }
    }
}
