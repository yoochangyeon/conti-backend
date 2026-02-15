package com.conti.domain.schedule.service;

import com.conti.domain.schedule.dto.ScheduleBulkCreateRequest;
import com.conti.domain.schedule.dto.ScheduleBulkResult;
import com.conti.domain.schedule.dto.ScheduleCreateRequest;
import com.conti.domain.schedule.dto.ScheduleRespondRequest;
import com.conti.domain.schedule.dto.ServiceScheduleResponse;
import com.conti.domain.schedule.entity.BlockoutDate;
import com.conti.domain.schedule.entity.ScheduleStatus;
import com.conti.domain.schedule.entity.ServiceSchedule;
import com.conti.domain.schedule.repository.BlockoutDateRepository;
import com.conti.domain.schedule.repository.ServiceScheduleRepository;
import com.conti.domain.team.repository.MemberPositionRepository;
import com.conti.domain.setlist.entity.Setlist;
import com.conti.domain.setlist.entity.WorshipType;
import com.conti.domain.setlist.repository.SetlistRepository;
import com.conti.domain.team.entity.Position;
import com.conti.domain.team.entity.Team;
import com.conti.domain.team.entity.TeamMember;
import com.conti.domain.team.entity.TeamRole;
import com.conti.domain.team.repository.TeamMemberRepository;
import com.conti.domain.notification.service.NotificationService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @InjectMocks
    private ScheduleService scheduleService;

    @Mock
    private ServiceScheduleRepository serviceScheduleRepository;

    @Mock
    private SetlistRepository setlistRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private BlockoutDateRepository blockoutDateRepository;

    @Mock
    private MemberPositionRepository memberPositionRepository;

    @Mock
    private NotificationService notificationService;

    private User user;
    private User user2;
    private Team team;
    private TeamMember teamMember;
    private TeamMember teamMember2;
    private Setlist setlist;

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

        user2 = User.builder()
                .email("test2@test.com")
                .name("테스트유저2")
                .profileImage("profile2.jpg")
                .provider(Provider.KAKAO)
                .providerId("67890")
                .build();
        ReflectionTestUtils.setField(user2, "id", 2L);

        team = Team.builder()
                .name("찬양팀")
                .description("테스트 팀")
                .inviteCode("ABC123")
                .build();
        ReflectionTestUtils.setField(team, "id", 1L);

        teamMember = TeamMember.builder()
                .user(user)
                .team(team)
                .role(TeamRole.ADMIN)
                .build();
        ReflectionTestUtils.setField(teamMember, "id", 1L);
        ReflectionTestUtils.setField(teamMember, "createdAt", LocalDateTime.now());

        teamMember2 = TeamMember.builder()
                .user(user2)
                .team(team)
                .role(TeamRole.VIEWER)
                .build();
        ReflectionTestUtils.setField(teamMember2, "id", 2L);
        ReflectionTestUtils.setField(teamMember2, "createdAt", LocalDateTime.now());

        setlist = Setlist.builder()
                .team(team)
                .creatorId(1L)
                .title("주일예배 콘티")
                .worshipDate(LocalDate.of(2026, 3, 1))
                .worshipType(WorshipType.SUNDAY_1ST)
                .build();
        ReflectionTestUtils.setField(setlist, "id", 1L);
    }

    @Nested
    @DisplayName("scheduleMembers")
    class ScheduleMembers {

        @Test
        @DisplayName("멤버를 일괄 배정한다")
        void scheduleMembers_success() {
            // given
            Long setlistId = 1L;
            ScheduleBulkCreateRequest request = new ScheduleBulkCreateRequest(List.of(
                    new ScheduleCreateRequest(1L, Position.VOCAL),
                    new ScheduleCreateRequest(2L, Position.ACOUSTIC_GUITAR)
            ));

            given(setlistRepository.findById(setlistId)).willReturn(Optional.of(setlist));
            given(teamMemberRepository.findById(1L)).willReturn(Optional.of(teamMember));
            given(teamMemberRepository.findById(2L)).willReturn(Optional.of(teamMember2));
            given(serviceScheduleRepository.existsBySetlistIdAndTeamMemberIdAndPosition(
                    eq(setlistId), any(), any())).willReturn(false);
            given(blockoutDateRepository.findByTeamMemberIdAndDateOverlapping(
                    any(), any())).willReturn(List.of());
            given(serviceScheduleRepository.save(any(ServiceSchedule.class)))
                    .willAnswer(invocation -> {
                        ServiceSchedule saved = invocation.getArgument(0);
                        ReflectionTestUtils.setField(saved, "id", 1L);
                        ReflectionTestUtils.setField(saved, "createdAt", LocalDateTime.now());
                        return saved;
                    });

            // when
            ScheduleBulkResult result = scheduleService.scheduleMembers(setlistId, request);

            // then
            assertThat(result.created()).hasSize(2);
            assertThat(result.conflicts()).isEmpty();
        }

        @Test
        @DisplayName("부재 일정이 겹치면 충돌 정보를 반환하되 스케줄은 생성한다")
        void scheduleMembers_withBlockoutConflict() {
            // given
            Long setlistId = 1L;
            ScheduleBulkCreateRequest request = new ScheduleBulkCreateRequest(List.of(
                    new ScheduleCreateRequest(1L, Position.VOCAL)
            ));

            BlockoutDate blockout = BlockoutDate.builder()
                    .teamMember(teamMember)
                    .startDate(LocalDate.of(2026, 2, 28))
                    .endDate(LocalDate.of(2026, 3, 5))
                    .reason("해외 출장")
                    .build();

            given(setlistRepository.findById(setlistId)).willReturn(Optional.of(setlist));
            given(teamMemberRepository.findById(1L)).willReturn(Optional.of(teamMember));
            given(serviceScheduleRepository.existsBySetlistIdAndTeamMemberIdAndPosition(
                    eq(setlistId), any(), any())).willReturn(false);
            given(blockoutDateRepository.findByTeamMemberIdAndDateOverlapping(
                    eq(1L), eq(LocalDate.of(2026, 3, 1)))).willReturn(List.of(blockout));
            given(serviceScheduleRepository.save(any(ServiceSchedule.class)))
                    .willAnswer(invocation -> {
                        ServiceSchedule saved = invocation.getArgument(0);
                        ReflectionTestUtils.setField(saved, "id", 1L);
                        ReflectionTestUtils.setField(saved, "createdAt", LocalDateTime.now());
                        return saved;
                    });

            // when
            ScheduleBulkResult result = scheduleService.scheduleMembers(setlistId, request);

            // then
            assertThat(result.created()).hasSize(1);
            assertThat(result.conflicts()).hasSize(1);
            assertThat(result.conflicts().get(0).conflictType()).isEqualTo("BLOCKOUT");
            assertThat(result.conflicts().get(0).memberName()).isEqualTo("테스트유저");
        }

        @Test
        @DisplayName("중복 배정 시 DUPLICATE_SCHEDULE 예외를 던진다")
        void scheduleMembers_duplicate() {
            // given
            Long setlistId = 1L;
            ScheduleBulkCreateRequest request = new ScheduleBulkCreateRequest(List.of(
                    new ScheduleCreateRequest(1L, Position.VOCAL)
            ));

            given(setlistRepository.findById(setlistId)).willReturn(Optional.of(setlist));
            given(teamMemberRepository.findById(1L)).willReturn(Optional.of(teamMember));
            given(serviceScheduleRepository.existsBySetlistIdAndTeamMemberIdAndPosition(
                    setlistId, 1L, Position.VOCAL)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> scheduleService.scheduleMembers(setlistId, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.DUPLICATE_SCHEDULE));
        }

        @Test
        @DisplayName("콘티가 없으면 예외를 던진다")
        void scheduleMembers_setlistNotFound() {
            // given
            Long setlistId = 999L;
            ScheduleBulkCreateRequest request = new ScheduleBulkCreateRequest(List.of(
                    new ScheduleCreateRequest(1L, Position.VOCAL)
            ));

            given(setlistRepository.findById(setlistId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> scheduleService.scheduleMembers(setlistId, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.SETLIST_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("respond")
    class Respond {

        @Test
        @DisplayName("배정을 수락한다")
        void respond_accept() {
            // given
            Long scheduleId = 1L;
            Long userId = 1L;

            ServiceSchedule schedule = ServiceSchedule.builder()
                    .setlist(setlist)
                    .teamMember(teamMember)
                    .position(Position.VOCAL)
                    .build();
            ReflectionTestUtils.setField(schedule, "id", scheduleId);

            ScheduleRespondRequest request = new ScheduleRespondRequest(true, null);

            given(serviceScheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));

            // when
            ServiceScheduleResponse result = scheduleService.respond(scheduleId, userId, request);

            // then
            assertThat(result.status()).isEqualTo("ACCEPTED");
        }

        @Test
        @DisplayName("배정을 거절한다")
        void respond_decline() {
            // given
            Long scheduleId = 1L;
            Long userId = 1L;

            ServiceSchedule schedule = ServiceSchedule.builder()
                    .setlist(setlist)
                    .teamMember(teamMember)
                    .position(Position.VOCAL)
                    .build();
            ReflectionTestUtils.setField(schedule, "id", scheduleId);

            ScheduleRespondRequest request = new ScheduleRespondRequest(false, "개인 사정");

            given(serviceScheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));

            // when
            ServiceScheduleResponse result = scheduleService.respond(scheduleId, userId, request);

            // then
            assertThat(result.status()).isEqualTo("DECLINED");
            assertThat(result.declinedReason()).isEqualTo("개인 사정");
        }

        @Test
        @DisplayName("거절 시 사유가 없으면 예외를 던진다")
        void respond_declineWithoutReason() {
            // given
            Long scheduleId = 1L;
            Long userId = 1L;

            ServiceSchedule schedule = ServiceSchedule.builder()
                    .setlist(setlist)
                    .teamMember(teamMember)
                    .position(Position.VOCAL)
                    .build();
            ReflectionTestUtils.setField(schedule, "id", scheduleId);

            ScheduleRespondRequest request = new ScheduleRespondRequest(false, null);

            given(serviceScheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));

            // when & then
            assertThatThrownBy(() -> scheduleService.respond(scheduleId, userId, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.DECLINE_REASON_REQUIRED));
        }

        @Test
        @DisplayName("이미 응답한 배정에 다시 응답하면 예외를 던진다")
        void respond_alreadyResponded() {
            // given
            Long scheduleId = 1L;
            Long userId = 1L;

            ServiceSchedule schedule = ServiceSchedule.builder()
                    .setlist(setlist)
                    .teamMember(teamMember)
                    .position(Position.VOCAL)
                    .build();
            schedule.accept(); // already responded
            ReflectionTestUtils.setField(schedule, "id", scheduleId);

            ScheduleRespondRequest request = new ScheduleRespondRequest(true, null);

            given(serviceScheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));

            // when & then
            assertThatThrownBy(() -> scheduleService.respond(scheduleId, userId, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.SCHEDULE_ALREADY_RESPONDED));
        }

        @Test
        @DisplayName("배정된 멤버가 아닌 사용자가 응답하면 예외를 던진다")
        void respond_notAssignedMember() {
            // given
            Long scheduleId = 1L;
            Long otherUserId = 2L;

            ServiceSchedule schedule = ServiceSchedule.builder()
                    .setlist(setlist)
                    .teamMember(teamMember) // user 1
                    .position(Position.VOCAL)
                    .build();
            ReflectionTestUtils.setField(schedule, "id", scheduleId);

            ScheduleRespondRequest request = new ScheduleRespondRequest(true, null);

            given(serviceScheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));

            // when & then
            assertThatThrownBy(() -> scheduleService.respond(scheduleId, otherUserId, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.FORBIDDEN));
        }

        @Test
        @DisplayName("스케줄이 없으면 예외를 던진다")
        void respond_notFound() {
            // given
            Long scheduleId = 999L;
            Long userId = 1L;
            ScheduleRespondRequest request = new ScheduleRespondRequest(true, null);

            given(serviceScheduleRepository.findById(scheduleId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> scheduleService.respond(scheduleId, userId, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("getSchedulesForSetlist")
    class GetSchedulesForSetlist {

        @Test
        @DisplayName("콘티의 스케줄 목록을 조회한다")
        void getSchedulesForSetlist_success() {
            // given
            Long setlistId = 1L;

            ServiceSchedule schedule = ServiceSchedule.builder()
                    .setlist(setlist)
                    .teamMember(teamMember)
                    .position(Position.VOCAL)
                    .build();
            ReflectionTestUtils.setField(schedule, "id", 1L);
            ReflectionTestUtils.setField(schedule, "createdAt", LocalDateTime.now());

            given(serviceScheduleRepository.findBySetlistIdWithMember(setlistId))
                    .willReturn(List.of(schedule));

            // when
            List<ServiceScheduleResponse> result = scheduleService.getSchedulesForSetlist(setlistId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).memberName()).isEqualTo("테스트유저");
            assertThat(result.get(0).position()).isEqualTo("VOCAL");
            assertThat(result.get(0).status()).isEqualTo("PENDING");
        }
    }

    @Nested
    @DisplayName("getMySchedules")
    class GetMySchedules {

        @Test
        @DisplayName("나의 예정된 스케줄 목록을 조회한다")
        void getMySchedules_success() {
            // given
            Long teamId = 1L;
            Long userId = 1L;

            ServiceSchedule schedule = ServiceSchedule.builder()
                    .setlist(setlist)
                    .teamMember(teamMember)
                    .position(Position.VOCAL)
                    .build();
            ReflectionTestUtils.setField(schedule, "id", 1L);
            ReflectionTestUtils.setField(schedule, "createdAt", LocalDateTime.now());

            given(teamMemberRepository.findByUserIdAndTeamId(userId, teamId))
                    .willReturn(Optional.of(teamMember));
            given(serviceScheduleRepository.findUpcomingByTeamMemberId(1L))
                    .willReturn(List.of(schedule));

            // when
            List<ServiceScheduleResponse> result = scheduleService.getMySchedules(teamId, userId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).memberName()).isEqualTo("테스트유저");
        }

        @Test
        @DisplayName("팀 멤버가 아니면 예외를 던진다")
        void getMySchedules_notMember() {
            // given
            Long teamId = 1L;
            Long userId = 999L;

            given(teamMemberRepository.findByUserIdAndTeamId(userId, teamId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> scheduleService.getMySchedules(teamId, userId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.USER_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("removeSchedule")
    class RemoveSchedule {

        @Test
        @DisplayName("스케줄을 삭제한다")
        void removeSchedule_success() {
            // given
            Long scheduleId = 1L;

            ServiceSchedule schedule = ServiceSchedule.builder()
                    .setlist(setlist)
                    .teamMember(teamMember)
                    .position(Position.VOCAL)
                    .build();
            ReflectionTestUtils.setField(schedule, "id", scheduleId);

            given(serviceScheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));

            // when
            scheduleService.removeSchedule(scheduleId);

            // then
            verify(serviceScheduleRepository).delete(schedule);
        }

        @Test
        @DisplayName("스케줄이 없으면 예외를 던진다")
        void removeSchedule_notFound() {
            // given
            Long scheduleId = 999L;

            given(serviceScheduleRepository.findById(scheduleId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> scheduleService.removeSchedule(scheduleId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND));
        }
    }
}
