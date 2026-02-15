package com.conti.domain.schedule.service;

import com.conti.domain.schedule.dto.BlockoutDateCreateRequest;
import com.conti.domain.schedule.dto.BlockoutDateResponse;
import com.conti.domain.schedule.entity.BlockoutDate;
import com.conti.domain.schedule.repository.BlockoutDateRepository;
import com.conti.domain.team.entity.Team;
import com.conti.domain.team.entity.TeamMember;
import com.conti.domain.team.entity.TeamRole;
import com.conti.domain.team.repository.TeamMemberRepository;
import com.conti.domain.user.entity.Provider;
import com.conti.domain.user.entity.User;
import com.conti.global.error.BusinessException;
import com.conti.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BlockoutDateServiceTest {

    @InjectMocks
    private BlockoutDateService blockoutDateService;

    @Mock
    private BlockoutDateRepository blockoutDateRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    private User user;
    private User otherUser;
    private Team team;
    private TeamMember teamMember;
    private TeamMember adminMember;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@test.com")
                .name("테스트유저")
                .profileImage("profile.jpg")
                .provider(Provider.KAKAO)
                .providerId("12345")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        otherUser = User.builder()
                .email("other@test.com")
                .name("다른유저")
                .provider(Provider.KAKAO)
                .providerId("67890")
                .build();
        ReflectionTestUtils.setField(otherUser, "id", 2L);

        team = Team.builder()
                .name("찬양팀")
                .description("테스트 팀")
                .inviteCode("ABC123")
                .build();
        ReflectionTestUtils.setField(team, "id", 1L);

        teamMember = TeamMember.builder()
                .user(user)
                .team(team)
                .role(TeamRole.VIEWER)
                .build();
        ReflectionTestUtils.setField(teamMember, "id", 1L);
        ReflectionTestUtils.setField(teamMember, "createdAt", LocalDateTime.now());

        adminMember = TeamMember.builder()
                .user(otherUser)
                .team(team)
                .role(TeamRole.ADMIN)
                .build();
        ReflectionTestUtils.setField(adminMember, "id", 2L);
        ReflectionTestUtils.setField(adminMember, "createdAt", LocalDateTime.now());
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("자신의 부재 일정을 생성한다")
        void create_self_success() {
            // given
            Long teamMemberId = 1L;
            Long userId = 1L;
            BlockoutDateCreateRequest request = new BlockoutDateCreateRequest(
                    LocalDate.of(2026, 3, 1),
                    LocalDate.of(2026, 3, 7),
                    "해외 출장"
            );

            given(teamMemberRepository.findById(teamMemberId)).willReturn(Optional.of(teamMember));
            given(blockoutDateRepository.save(any(BlockoutDate.class)))
                    .willAnswer(invocation -> {
                        BlockoutDate saved = invocation.getArgument(0);
                        ReflectionTestUtils.setField(saved, "id", 1L);
                        return saved;
                    });

            // when
            BlockoutDateResponse result = blockoutDateService.create(teamMemberId, userId, request);

            // then
            assertThat(result.startDate()).isEqualTo(LocalDate.of(2026, 3, 1));
            assertThat(result.endDate()).isEqualTo(LocalDate.of(2026, 3, 7));
            assertThat(result.reason()).isEqualTo("해외 출장");
            verify(blockoutDateRepository).save(any(BlockoutDate.class));
        }

        @Test
        @DisplayName("ADMIN이 다른 멤버의 부재 일정을 생성한다")
        void create_admin_success() {
            // given
            Long teamMemberId = 1L;
            Long adminUserId = 2L;
            BlockoutDateCreateRequest request = new BlockoutDateCreateRequest(
                    LocalDate.of(2026, 3, 1),
                    LocalDate.of(2026, 3, 7),
                    "휴가"
            );

            given(teamMemberRepository.findById(teamMemberId)).willReturn(Optional.of(teamMember));
            given(teamMemberRepository.findByUserIdAndTeamId(adminUserId, team.getId()))
                    .willReturn(Optional.of(adminMember));
            given(blockoutDateRepository.save(any(BlockoutDate.class)))
                    .willAnswer(invocation -> {
                        BlockoutDate saved = invocation.getArgument(0);
                        ReflectionTestUtils.setField(saved, "id", 1L);
                        return saved;
                    });

            // when
            BlockoutDateResponse result = blockoutDateService.create(teamMemberId, adminUserId, request);

            // then
            assertThat(result.startDate()).isEqualTo(LocalDate.of(2026, 3, 1));
            verify(blockoutDateRepository).save(any(BlockoutDate.class));
        }

        @Test
        @DisplayName("권한 없는 사용자가 다른 멤버의 부재 일정 생성 시 예외")
        void create_forbidden() {
            // given
            Long teamMemberId = 1L;
            Long otherUserId = 2L;
            BlockoutDateCreateRequest request = new BlockoutDateCreateRequest(
                    LocalDate.of(2026, 3, 1),
                    LocalDate.of(2026, 3, 7),
                    "테스트"
            );

            TeamMember viewerMember = TeamMember.builder()
                    .user(otherUser)
                    .team(team)
                    .role(TeamRole.VIEWER)
                    .build();
            ReflectionTestUtils.setField(viewerMember, "id", 3L);

            given(teamMemberRepository.findById(teamMemberId)).willReturn(Optional.of(teamMember));
            given(teamMemberRepository.findByUserIdAndTeamId(otherUserId, team.getId()))
                    .willReturn(Optional.of(viewerMember));

            // when & then
            assertThatThrownBy(() -> blockoutDateService.create(teamMemberId, otherUserId, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.FORBIDDEN));
        }

        @Test
        @DisplayName("종료일이 시작일보다 이전이면 예외를 던진다")
        void create_invalidDateRange() {
            // given
            Long teamMemberId = 1L;
            Long userId = 1L;
            BlockoutDateCreateRequest request = new BlockoutDateCreateRequest(
                    LocalDate.of(2026, 3, 7),
                    LocalDate.of(2026, 3, 1),
                    "날짜 역순"
            );

            given(teamMemberRepository.findById(teamMemberId)).willReturn(Optional.of(teamMember));

            // when & then
            assertThatThrownBy(() -> blockoutDateService.create(teamMemberId, userId, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_DATE_RANGE));
        }

        @Test
        @DisplayName("멤버가 없으면 예외를 던진다")
        void create_memberNotFound() {
            // given
            Long teamMemberId = 999L;
            Long userId = 1L;
            BlockoutDateCreateRequest request = new BlockoutDateCreateRequest(
                    LocalDate.of(2026, 3, 1),
                    LocalDate.of(2026, 3, 7),
                    "테스트"
            );

            given(teamMemberRepository.findById(teamMemberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> blockoutDateService.create(teamMemberId, userId, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.USER_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("getForMember")
    class GetForMember {

        @Test
        @DisplayName("멤버의 부재 일정을 조회한다")
        void getForMember_success() {
            // given
            Long teamMemberId = 1L;

            BlockoutDate blockout = BlockoutDate.builder()
                    .teamMember(teamMember)
                    .startDate(LocalDate.of(2026, 3, 1))
                    .endDate(LocalDate.of(2026, 3, 7))
                    .reason("해외 출장")
                    .build();
            ReflectionTestUtils.setField(blockout, "id", 1L);

            given(blockoutDateRepository.findByTeamMemberIdOrderByStartDate(teamMemberId))
                    .willReturn(List.of(blockout));

            // when
            List<BlockoutDateResponse> result = blockoutDateService.getForMember(teamMemberId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).startDate()).isEqualTo(LocalDate.of(2026, 3, 1));
            assertThat(result.get(0).reason()).isEqualTo("해외 출장");
        }
    }

    @Nested
    @DisplayName("getForTeamInRange")
    class GetForTeamInRange {

        @Test
        @DisplayName("팀의 날짜 범위 내 부재 일정을 조회한다")
        void getForTeamInRange_success() {
            // given
            Long teamId = 1L;
            LocalDate from = LocalDate.of(2026, 3, 1);
            LocalDate to = LocalDate.of(2026, 3, 31);

            BlockoutDate blockout = BlockoutDate.builder()
                    .teamMember(teamMember)
                    .startDate(LocalDate.of(2026, 3, 5))
                    .endDate(LocalDate.of(2026, 3, 10))
                    .reason("여행")
                    .build();
            ReflectionTestUtils.setField(blockout, "id", 1L);

            given(blockoutDateRepository.findByTeamIdAndDateRange(teamId, from, to))
                    .willReturn(List.of(blockout));

            // when
            List<BlockoutDateResponse> result = blockoutDateService.getForTeamInRange(teamId, from, to);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).startDate()).isEqualTo(LocalDate.of(2026, 3, 5));
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("자신의 부재 일정을 삭제한다")
        void delete_self_success() {
            // given
            Long blockoutId = 1L;
            Long userId = 1L;

            BlockoutDate blockout = BlockoutDate.builder()
                    .teamMember(teamMember)
                    .startDate(LocalDate.of(2026, 3, 1))
                    .endDate(LocalDate.of(2026, 3, 7))
                    .build();
            ReflectionTestUtils.setField(blockout, "id", blockoutId);

            given(blockoutDateRepository.findById(blockoutId)).willReturn(Optional.of(blockout));

            // when
            blockoutDateService.delete(blockoutId, userId);

            // then
            verify(blockoutDateRepository).delete(blockout);
        }

        @Test
        @DisplayName("ADMIN이 다른 멤버의 부재 일정을 삭제한다")
        void delete_admin_success() {
            // given
            Long blockoutId = 1L;
            Long adminUserId = 2L;

            BlockoutDate blockout = BlockoutDate.builder()
                    .teamMember(teamMember)
                    .startDate(LocalDate.of(2026, 3, 1))
                    .endDate(LocalDate.of(2026, 3, 7))
                    .build();
            ReflectionTestUtils.setField(blockout, "id", blockoutId);

            given(blockoutDateRepository.findById(blockoutId)).willReturn(Optional.of(blockout));
            given(teamMemberRepository.findByUserIdAndTeamId(adminUserId, team.getId()))
                    .willReturn(Optional.of(adminMember));

            // when
            blockoutDateService.delete(blockoutId, adminUserId);

            // then
            verify(blockoutDateRepository).delete(blockout);
        }

        @Test
        @DisplayName("부재 일정이 없으면 예외를 던진다")
        void delete_notFound() {
            // given
            Long blockoutId = 999L;
            Long userId = 1L;

            given(blockoutDateRepository.findById(blockoutId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> blockoutDateService.delete(blockoutId, userId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.BLOCKOUT_NOT_FOUND));
        }

        @Test
        @DisplayName("권한 없는 사용자가 삭제 시 예외를 던진다")
        void delete_forbidden() {
            // given
            Long blockoutId = 1L;
            Long otherUserId = 2L;

            BlockoutDate blockout = BlockoutDate.builder()
                    .teamMember(teamMember)
                    .startDate(LocalDate.of(2026, 3, 1))
                    .endDate(LocalDate.of(2026, 3, 7))
                    .build();
            ReflectionTestUtils.setField(blockout, "id", blockoutId);

            TeamMember viewerMember = TeamMember.builder()
                    .user(otherUser)
                    .team(team)
                    .role(TeamRole.VIEWER)
                    .build();
            ReflectionTestUtils.setField(viewerMember, "id", 3L);

            given(blockoutDateRepository.findById(blockoutId)).willReturn(Optional.of(blockout));
            given(teamMemberRepository.findByUserIdAndTeamId(otherUserId, team.getId()))
                    .willReturn(Optional.of(viewerMember));

            // when & then
            assertThatThrownBy(() -> blockoutDateService.delete(blockoutId, otherUserId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.FORBIDDEN));
        }
    }
}
