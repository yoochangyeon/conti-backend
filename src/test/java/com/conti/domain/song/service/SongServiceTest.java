package com.conti.domain.song.service;

import com.conti.domain.setlist.repository.SetlistRepository;
import com.conti.domain.song.dto.SongCreateRequest;
import com.conti.domain.song.dto.SongDetailResponse;
import com.conti.domain.song.dto.SongResponse;
import com.conti.domain.song.dto.SongSearchCondition;
import com.conti.domain.song.dto.SongUpdateRequest;
import com.conti.domain.song.dto.TagResponse;
import com.conti.domain.song.entity.Song;
import com.conti.domain.song.entity.SongFile;
import com.conti.domain.song.entity.SongTag;
import com.conti.domain.song.repository.SongFileRepository;
import com.conti.domain.song.repository.SongRepository;
import com.conti.domain.song.repository.SongTagRepository;
import com.conti.domain.song.repository.SongUsageRepository;
import com.conti.domain.team.entity.Team;
import com.conti.domain.team.repository.TeamRepository;
import com.conti.global.error.BusinessException;
import com.conti.global.error.ErrorCode;
import com.conti.infra.s3.S3FileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SongServiceTest {

    @InjectMocks
    private SongService songService;

    @Mock
    private SongRepository songRepository;

    @Mock
    private SongTagRepository songTagRepository;

    @Mock
    private SongFileRepository songFileRepository;

    @Mock
    private SongUsageRepository songUsageRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private SetlistRepository setlistRepository;

    @Mock
    private S3FileService s3FileService;

    private Team createTeam() {
        return Team.builder()
                .name("찬양팀")
                .description("테스트 팀")
                .inviteCode("ABC123")
                .build();
    }

    private Song createSong(Team team) {
        Song song = Song.builder()
                .team(team)
                .title("이 땅의 모든 찬양")
                .artist("마커스")
                .originalKey("G")
                .bpm(120)
                .memo("메모")
                .youtubeUrl("https://youtube.com/test")
                .musicUrl("https://music.com/test")
                .build();

        SongTag tag = SongTag.builder()
                .song(song)
                .tag("경배")
                .build();
        song.getSongTags().add(tag);

        return song;
    }

    @Nested
    @DisplayName("getSongs")
    class GetSongs {

        @Test
        @DisplayName("팀의 찬양 목록을 페이징하여 조회한다")
        void getSongs_success() {
            // given
            Long teamId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            Team team = createTeam();
            Song song = createSong(team);
            Page<Song> songPage = new PageImpl<>(List.of(song), pageable, 1);
            SongSearchCondition condition = new SongSearchCondition(null, null, null, null, null);

            given(songRepository.searchSongs(eq(teamId), any(SongSearchCondition.class), any(Pageable.class)))
                    .willReturn(songPage);

            // when
            Page<SongResponse> result = songService.getSongs(teamId, condition, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).title()).isEqualTo("이 땅의 모든 찬양");
            assertThat(result.getContent().get(0).artist()).isEqualTo("마커스");
        }
    }

    @Nested
    @DisplayName("createSong")
    class CreateSong {

        @Test
        @DisplayName("새 찬양을 생성한다")
        void createSong_success() {
            // given
            Long teamId = 1L;
            Team team = createTeam();
            SongCreateRequest request = new SongCreateRequest(
                    "새 찬양", "아티스트", "C", 100, "메모",
                    "https://youtube.com", "https://music.com",
                    List.of("경배", "감사")
            );

            given(teamRepository.findById(teamId)).willReturn(Optional.of(team));
            given(songRepository.save(any(Song.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            SongResponse result = songService.createSong(teamId, request);

            // then
            assertThat(result.title()).isEqualTo("새 찬양");
            assertThat(result.artist()).isEqualTo("아티스트");
            assertThat(result.tags()).containsExactly("경배", "감사");
            verify(songRepository).save(any(Song.class));
        }

        @Test
        @DisplayName("팀이 없으면 예외를 던진다")
        void createSong_teamNotFound() {
            // given
            Long teamId = 999L;
            SongCreateRequest request = new SongCreateRequest(
                    "찬양", null, null, null, null, null, null, null
            );
            given(teamRepository.findById(teamId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> songService.createSong(teamId, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.TEAM_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("getSong")
    class GetSong {

        @Test
        @DisplayName("찬양 상세 정보를 조회한다")
        void getSong_success() {
            // given
            Long teamId = 1L;
            Long songId = 1L;
            Team team = createTeam();
            Song song = createSong(team);

            given(songRepository.findById(songId)).willReturn(Optional.of(song));
            given(songUsageRepository.findBySongId(songId)).willReturn(Collections.emptyList());

            // when
            SongDetailResponse result = songService.getSong(teamId, songId);

            // then
            assertThat(result.title()).isEqualTo("이 땅의 모든 찬양");
            assertThat(result.memo()).isEqualTo("메모");
            assertThat(result.usageCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("찬양이 없으면 예외를 던진다")
        void getSong_notFound() {
            // given
            Long teamId = 1L;
            Long songId = 999L;
            given(songRepository.findById(songId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> songService.getSong(teamId, songId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.SONG_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("updateSong")
    class UpdateSong {

        @Test
        @DisplayName("찬양 정보를 수정한다")
        void updateSong_success() {
            // given
            Long teamId = 1L;
            Long songId = 1L;
            Team team = createTeam();
            Song song = createSong(team);
            SongUpdateRequest request = new SongUpdateRequest(
                    "수정된 제목", null, null, null, null, null, null, null
            );

            given(songRepository.findById(songId)).willReturn(Optional.of(song));

            // when
            SongResponse result = songService.updateSong(teamId, songId, request);

            // then
            assertThat(result.title()).isEqualTo("수정된 제목");
            assertThat(result.artist()).isEqualTo("마커스");
        }

        @Test
        @DisplayName("태그를 수정하면 기존 태그가 교체된다")
        void updateSong_replaceTags() {
            // given
            Long teamId = 1L;
            Long songId = 1L;
            Team team = createTeam();
            Song song = createSong(team);
            SongUpdateRequest request = new SongUpdateRequest(
                    null, null, null, null, null, null, null,
                    List.of("선포", "사랑")
            );

            given(songRepository.findById(songId)).willReturn(Optional.of(song));

            // when
            SongResponse result = songService.updateSong(teamId, songId, request);

            // then
            assertThat(result.tags()).containsExactly("선포", "사랑");
        }
    }

    @Nested
    @DisplayName("deleteSong")
    class DeleteSong {

        @Test
        @DisplayName("찬양을 삭제한다")
        void deleteSong_success() {
            // given
            Long teamId = 1L;
            Long songId = 1L;
            Team team = createTeam();
            Song song = createSong(team);

            given(songRepository.findById(songId)).willReturn(Optional.of(song));

            // when
            songService.deleteSong(teamId, songId);

            // then
            verify(songRepository).delete(song);
        }

        @Test
        @DisplayName("찬양이 없으면 예외를 던진다")
        void deleteSong_notFound() {
            // given
            Long teamId = 1L;
            Long songId = 999L;
            given(songRepository.findById(songId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> songService.deleteSong(teamId, songId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.SONG_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("getTeamTags")
    class GetTeamTags {

        @Test
        @DisplayName("팀의 태그 목록을 조회한다")
        void getTeamTags_success() {
            // given
            Long teamId = 1L;
            List<TagResponse> tags = List.of(
                    new TagResponse("경배", 5),
                    new TagResponse("감사", 3)
            );
            given(songTagRepository.findTagsWithCountByTeamId(teamId)).willReturn(tags);

            // when
            List<TagResponse> result = songService.getTeamTags(teamId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).tag()).isEqualTo("경배");
            assertThat(result.get(0).count()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("uploadFile")
    class UploadFile {

        @Test
        @DisplayName("찬양에 파일을 업로드한다")
        void uploadFile_success() {
            // given
            Long songId = 1L;
            Team team = createTeam();
            Song song = createSong(team);

            given(songRepository.findById(songId)).willReturn(Optional.of(song));

            // when
            var result = songService.uploadFile(songId, "악보.pdf", "https://s3.com/file.pdf", "PDF", 1024L);

            // then
            assertThat(result.fileName()).isEqualTo("악보.pdf");
            assertThat(result.fileUrl()).isEqualTo("https://s3.com/file.pdf");
        }
    }

    @Nested
    @DisplayName("deleteFile")
    class DeleteFile {

        @Test
        @DisplayName("파일을 삭제한다")
        void deleteFile_success() {
            // given
            Long songId = 1L;
            Long fileId = 1L;
            Team team = createTeam();
            Song song = createSong(team);

            // SongFile needs an id for removeIf to match
            SongFile songFile = SongFile.builder()
                    .song(song)
                    .fileName("악보.pdf")
                    .fileUrl("https://s3.com/file.pdf")
                    .build();
            song.getSongFiles().add(songFile);

            // Use reflection to set id since it's auto-generated
            try {
                var idField = SongFile.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(songFile, fileId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            given(songRepository.findById(songId)).willReturn(Optional.of(song));

            // when
            songService.deleteFile(songId, fileId);

            // then
            assertThat(song.getSongFiles()).isEmpty();
        }
    }
}
