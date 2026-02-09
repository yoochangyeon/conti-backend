package com.conti.domain.setlist.service;

import com.conti.domain.setlist.dto.ReorderRequest;
import com.conti.domain.setlist.dto.SetlistCreateRequest;
import com.conti.domain.setlist.dto.SetlistDetailResponse;
import com.conti.domain.setlist.dto.SetlistItemRequest;
import com.conti.domain.setlist.dto.SetlistItemResponse;
import com.conti.domain.setlist.dto.SetlistResponse;
import com.conti.domain.setlist.dto.SetlistSearchCondition;
import com.conti.domain.setlist.dto.SetlistUpdateRequest;
import com.conti.domain.setlist.entity.Setlist;
import com.conti.domain.setlist.entity.SetlistItem;
import com.conti.domain.setlist.repository.SetlistItemRepository;
import com.conti.domain.setlist.repository.SetlistRepository;
import com.conti.domain.song.entity.Song;
import com.conti.domain.song.repository.SongRepository;
import com.conti.domain.song.repository.SongUsageRepository;
import com.conti.domain.team.entity.Team;
import com.conti.domain.team.repository.TeamRepository;
import com.conti.global.error.BusinessException;
import com.conti.global.error.ErrorCode;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SetlistServiceTest {

    @InjectMocks
    private SetlistService setlistService;

    @Mock
    private SetlistRepository setlistRepository;

    @Mock
    private SetlistItemRepository setlistItemRepository;

    @Mock
    private SongRepository songRepository;

    @Mock
    private SongUsageRepository songUsageRepository;

    @Mock
    private TeamRepository teamRepository;

    private Team createTeam() {
        return Team.builder()
                .name("찬양팀")
                .description("테스트 팀")
                .inviteCode("ABC123")
                .build();
    }

    private Setlist createSetlist(Team team) {
        return Setlist.builder()
                .team(team)
                .creatorId(1L)
                .title("주일예배 콘티")
                .worshipDate(LocalDate.of(2026, 2, 9))
                .worshipType("주일1부")
                .leaderId(1L)
                .memo("메모")
                .build();
    }

    private Song createSong(Team team) {
        return Song.builder()
                .team(team)
                .title("이 땅의 모든 찬양")
                .artist("마커스")
                .originalKey("G")
                .bpm(120)
                .build();
    }

    @Nested
    @DisplayName("getSetlists")
    class GetSetlists {

        @Test
        @DisplayName("팀의 콘티 목록을 페이징하여 조회한다")
        void getSetlists_success() {
            // given
            Long teamId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            Team team = createTeam();
            Setlist setlist = createSetlist(team);
            Page<Setlist> setlistPage = new PageImpl<>(List.of(setlist), pageable, 1);
            SetlistSearchCondition condition = new SetlistSearchCondition(null, null, null);

            given(setlistRepository.searchSetlists(eq(teamId), any(SetlistSearchCondition.class), any(Pageable.class)))
                    .willReturn(setlistPage);

            // when
            Page<SetlistResponse> result = setlistService.getSetlists(teamId, condition, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).title()).isEqualTo("주일예배 콘티");
            assertThat(result.getContent().get(0).worshipType()).isEqualTo("주일1부");
        }
    }

    @Nested
    @DisplayName("createSetlist")
    class CreateSetlist {

        @Test
        @DisplayName("새 콘티를 생성한다")
        void createSetlist_success() {
            // given
            Long teamId = 1L;
            Long userId = 1L;
            Team team = createTeam();
            SetlistCreateRequest request = new SetlistCreateRequest(
                    "주일예배", LocalDate.of(2026, 2, 9), "주일1부", 1L, "메모"
            );

            given(teamRepository.findById(teamId)).willReturn(Optional.of(team));
            given(setlistRepository.save(any(Setlist.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            SetlistResponse result = setlistService.createSetlist(teamId, userId, request);

            // then
            assertThat(result.title()).isEqualTo("주일예배");
            assertThat(result.worshipDate()).isEqualTo(LocalDate.of(2026, 2, 9));
            verify(setlistRepository).save(any(Setlist.class));
        }

        @Test
        @DisplayName("팀이 없으면 예외를 던진다")
        void createSetlist_teamNotFound() {
            // given
            Long teamId = 999L;
            Long userId = 1L;
            SetlistCreateRequest request = new SetlistCreateRequest(
                    "콘티", LocalDate.now(), null, null, null
            );
            given(teamRepository.findById(teamId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> setlistService.createSetlist(teamId, userId, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.TEAM_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("getSetlist")
    class GetSetlist {

        @Test
        @DisplayName("콘티 상세 정보를 조회한다")
        void getSetlist_success() {
            // given
            Long setlistId = 1L;
            Team team = createTeam();
            Setlist setlist = createSetlist(team);

            given(setlistRepository.findById(setlistId)).willReturn(Optional.of(setlist));

            // when
            SetlistDetailResponse result = setlistService.getSetlist(setlistId);

            // then
            assertThat(result.title()).isEqualTo("주일예배 콘티");
            assertThat(result.memo()).isEqualTo("메모");
            assertThat(result.creatorId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("콘티가 없으면 예외를 던진다")
        void getSetlist_notFound() {
            // given
            Long setlistId = 999L;
            given(setlistRepository.findById(setlistId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> setlistService.getSetlist(setlistId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.SETLIST_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("updateSetlist")
    class UpdateSetlist {

        @Test
        @DisplayName("콘티 정보를 수정한다")
        void updateSetlist_success() {
            // given
            Long setlistId = 1L;
            Team team = createTeam();
            Setlist setlist = createSetlist(team);
            SetlistUpdateRequest request = new SetlistUpdateRequest(
                    "수정된 콘티", null, null, null, null
            );

            given(setlistRepository.findById(setlistId)).willReturn(Optional.of(setlist));

            // when
            SetlistResponse result = setlistService.updateSetlist(setlistId, request);

            // then
            assertThat(result.title()).isEqualTo("수정된 콘티");
            assertThat(result.worshipDate()).isEqualTo(LocalDate.of(2026, 2, 9));
        }
    }

    @Nested
    @DisplayName("deleteSetlist")
    class DeleteSetlist {

        @Test
        @DisplayName("콘티를 삭제한다")
        void deleteSetlist_success() {
            // given
            Long setlistId = 1L;
            Team team = createTeam();
            Setlist setlist = createSetlist(team);

            given(setlistRepository.findById(setlistId)).willReturn(Optional.of(setlist));

            // when
            setlistService.deleteSetlist(setlistId);

            // then
            verify(setlistRepository).delete(setlist);
        }

        @Test
        @DisplayName("콘티가 없으면 예외를 던진다")
        void deleteSetlist_notFound() {
            // given
            Long setlistId = 999L;
            given(setlistRepository.findById(setlistId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> setlistService.deleteSetlist(setlistId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.SETLIST_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("addItem")
    class AddItem {

        @Test
        @DisplayName("콘티에 곡을 추가한다")
        void addItem_success() {
            // given
            Long setlistId = 1L;
            Team team = createTeam();
            Setlist setlist = createSetlist(team);
            Song song = createSong(team);

            SetlistItemRequest request = new SetlistItemRequest(1L, "C", "메모");

            given(setlistRepository.findById(setlistId)).willReturn(Optional.of(setlist));
            given(songRepository.findById(1L)).willReturn(Optional.of(song));

            // when
            SetlistItemResponse result = setlistService.addItem(setlistId, request);

            // then
            assertThat(result.songTitle()).isEqualTo("이 땅의 모든 찬양");
            assertThat(result.songKey()).isEqualTo("C");
            assertThat(result.orderIndex()).isEqualTo(0);
            verify(songUsageRepository).save(any());
        }

        @Test
        @DisplayName("곡이 없으면 예외를 던진다")
        void addItem_songNotFound() {
            // given
            Long setlistId = 1L;
            Team team = createTeam();
            Setlist setlist = createSetlist(team);
            SetlistItemRequest request = new SetlistItemRequest(999L, "C", null);

            given(setlistRepository.findById(setlistId)).willReturn(Optional.of(setlist));
            given(songRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> setlistService.addItem(setlistId, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.SONG_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("updateItem")
    class UpdateItem {

        @Test
        @DisplayName("콘티 아이템을 수정한다")
        void updateItem_success() {
            // given
            Long setlistId = 1L;
            Long itemId = 1L;
            Team team = createTeam();
            Song song = createSong(team);
            Setlist setlist = createSetlist(team);

            SetlistItem item = SetlistItem.builder()
                    .setlist(setlist)
                    .song(song)
                    .orderIndex(0)
                    .songKey("G")
                    .memo("원래 메모")
                    .build();

            SetlistItemRequest request = new SetlistItemRequest(null, "D", "수정 메모");

            given(setlistItemRepository.findById(itemId)).willReturn(Optional.of(item));

            // when
            SetlistItemResponse result = setlistService.updateItem(setlistId, itemId, request);

            // then
            assertThat(result.songKey()).isEqualTo("D");
            assertThat(result.memo()).isEqualTo("수정 메모");
        }
    }

    @Nested
    @DisplayName("removeItem")
    class RemoveItem {

        @Test
        @DisplayName("콘티에서 곡을 제거한다")
        void removeItem_success() {
            // given
            Long setlistId = 1L;
            Long itemId = 1L;
            SetlistItem item = SetlistItem.builder()
                    .orderIndex(0)
                    .build();

            given(setlistItemRepository.findById(itemId)).willReturn(Optional.of(item));

            // when
            setlistService.removeItem(setlistId, itemId);

            // then
            verify(setlistItemRepository).delete(item);
        }
    }

    @Nested
    @DisplayName("reorderItems")
    class ReorderItems {

        @Test
        @DisplayName("콘티 아이템 순서를 변경한다")
        void reorderItems_success() {
            // given
            Long setlistId = 1L;
            SetlistItem item1 = SetlistItem.builder().orderIndex(0).build();
            SetlistItem item2 = SetlistItem.builder().orderIndex(1).build();
            SetlistItem item3 = SetlistItem.builder().orderIndex(2).build();

            // Reorder: item3, item1, item2
            ReorderRequest request = new ReorderRequest(List.of(3L, 1L, 2L));

            given(setlistItemRepository.findById(3L)).willReturn(Optional.of(item3));
            given(setlistItemRepository.findById(1L)).willReturn(Optional.of(item1));
            given(setlistItemRepository.findById(2L)).willReturn(Optional.of(item2));

            // when
            setlistService.reorderItems(setlistId, request);

            // then
            assertThat(item3.getOrderIndex()).isEqualTo(0);
            assertThat(item1.getOrderIndex()).isEqualTo(1);
            assertThat(item2.getOrderIndex()).isEqualTo(2);
        }
    }
}
