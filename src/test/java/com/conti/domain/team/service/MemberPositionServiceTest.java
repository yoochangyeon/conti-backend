package com.conti.domain.team.service;

import com.conti.domain.team.dto.MemberPositionRequest;
import com.conti.domain.team.dto.MemberPositionResponse;
import com.conti.domain.team.entity.MemberPosition;
import com.conti.domain.team.entity.Position;
import com.conti.domain.team.entity.Team;
import com.conti.domain.team.entity.TeamMember;
import com.conti.domain.team.entity.TeamRole;
import com.conti.domain.team.repository.MemberPositionRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberPositionServiceTest {

    @InjectMocks
    private MemberPositionService memberPositionService;

    @Mock
    private MemberPositionRepository memberPositionRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    private User user;
    private Team team;
    private TeamMember teamMember;

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
    }

    @Nested
    @DisplayName("setPositions")
    class SetPositions {

        @Test
        @DisplayName("멤버의 포지션을 설정한다")
        void setPositions_success() {
            // given
            Long teamMemberId = 1L;
            List<MemberPositionRequest> requests = List.of(
                    new MemberPositionRequest(Position.VOCAL, true),
                    new MemberPositionRequest(Position.ACOUSTIC_GUITAR, false)
            );

            given(teamMemberRepository.findById(teamMemberId)).willReturn(Optional.of(teamMember));

            // when
            List<MemberPositionResponse> result = memberPositionService.setPositions(teamMemberId, requests);

            // then
            assertThat(result).hasSize(2);
            verify(memberPositionRepository).deleteByTeamMemberId(teamMemberId);
        }

        @Test
        @DisplayName("멤버가 없으면 예외를 던진다")
        void setPositions_memberNotFound() {
            // given
            Long teamMemberId = 999L;
            List<MemberPositionRequest> requests = List.of(
                    new MemberPositionRequest(Position.VOCAL, true)
            );

            given(teamMemberRepository.findById(teamMemberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberPositionService.setPositions(teamMemberId, requests))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.USER_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("getPositions")
    class GetPositions {

        @Test
        @DisplayName("멤버의 포지션 목록을 조회한다")
        void getPositions_success() {
            // given
            Long teamMemberId = 1L;

            MemberPosition position1 = MemberPosition.builder()
                    .teamMember(teamMember)
                    .position(Position.VOCAL)
                    .primary(true)
                    .build();
            ReflectionTestUtils.setField(position1, "id", 1L);

            MemberPosition position2 = MemberPosition.builder()
                    .teamMember(teamMember)
                    .position(Position.ACOUSTIC_GUITAR)
                    .primary(false)
                    .build();
            ReflectionTestUtils.setField(position2, "id", 2L);

            given(memberPositionRepository.findByTeamMemberId(teamMemberId))
                    .willReturn(List.of(position1, position2));

            // when
            List<MemberPositionResponse> result = memberPositionService.getPositions(teamMemberId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).position()).isEqualTo("VOCAL");
            assertThat(result.get(0).displayName()).isEqualTo("보컬");
            assertThat(result.get(0).isPrimary()).isTrue();
            assertThat(result.get(1).position()).isEqualTo("ACOUSTIC_GUITAR");
            assertThat(result.get(1).isPrimary()).isFalse();
        }

        @Test
        @DisplayName("포지션이 없으면 빈 리스트를 반환한다")
        void getPositions_empty() {
            // given
            Long teamMemberId = 1L;
            given(memberPositionRepository.findByTeamMemberId(teamMemberId))
                    .willReturn(List.of());

            // when
            List<MemberPositionResponse> result = memberPositionService.getPositions(teamMemberId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getMembersByPosition")
    class GetMembersByPosition {

        @Test
        @DisplayName("특정 포지션의 멤버 ID 목록을 조회한다")
        void getMembersByPosition_success() {
            // given
            Long teamId = 1L;
            Position position = Position.VOCAL;

            given(memberPositionRepository.findTeamMemberIdsByTeamIdAndPosition(teamId, position))
                    .willReturn(List.of(1L, 2L));

            // when
            List<Long> result = memberPositionService.getMemberIdsByPosition(teamId, position);

            // then
            assertThat(result).containsExactly(1L, 2L);
        }
    }
}
