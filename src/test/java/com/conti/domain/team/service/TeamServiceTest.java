package com.conti.domain.team.service;

import com.conti.domain.team.dto.InviteResponse;
import com.conti.domain.team.dto.MemberRoleUpdateRequest;
import com.conti.domain.team.dto.TeamCreateRequest;
import com.conti.domain.team.dto.TeamMemberResponse;
import com.conti.domain.team.dto.TeamResponse;
import com.conti.domain.team.dto.TeamUpdateRequest;
import com.conti.domain.team.entity.Team;
import com.conti.domain.team.entity.TeamMember;
import com.conti.domain.team.entity.TeamRole;
import com.conti.domain.team.repository.TeamMemberRepository;
import com.conti.domain.team.repository.TeamRepository;
import com.conti.domain.user.entity.Provider;
import com.conti.domain.user.entity.User;
import com.conti.domain.user.repository.UserRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TeamService teamService;

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
                .description("우리 교회 찬양팀")
                .inviteCode("abc12345")
                .build();
        ReflectionTestUtils.setField(team, "id", 1L);
        ReflectionTestUtils.setField(team, "createdAt", LocalDateTime.now());

        teamMember = TeamMember.builder()
                .user(user)
                .team(team)
                .role(TeamRole.ADMIN)
                .build();
        ReflectionTestUtils.setField(teamMember, "id", 1L);
        ReflectionTestUtils.setField(teamMember, "createdAt", LocalDateTime.now());
    }

    @Nested
    @DisplayName("createTeam")
    class CreateTeam {

        @Test
        @DisplayName("팀 생성 성공")
        void success() {
            // given
            TeamCreateRequest request = new TeamCreateRequest("찬양팀", "우리 교회 찬양팀");
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(teamRepository.save(any(Team.class))).willReturn(team);
            given(teamMemberRepository.save(any(TeamMember.class))).willReturn(teamMember);

            // when
            TeamResponse response = teamService.createTeam(1L, request);

            // then
            assertThat(response.name()).isEqualTo("찬양팀");
            assertThat(response.description()).isEqualTo("우리 교회 찬양팀");
            verify(teamRepository).save(any(Team.class));
            verify(teamMemberRepository).save(any(TeamMember.class));
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 팀 생성 시 예외")
        void userNotFound() {
            // given
            TeamCreateRequest request = new TeamCreateRequest("찬양팀", "설명");
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> teamService.createTeam(999L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("getTeam")
    class GetTeam {

        @Test
        @DisplayName("팀 조회 성공")
        void success() {
            // given
            given(teamRepository.findById(1L)).willReturn(Optional.of(team));

            // when
            TeamResponse response = teamService.getTeam(1L);

            // then
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.name()).isEqualTo("찬양팀");
        }

        @Test
        @DisplayName("존재하지 않는 팀 조회 시 예외")
        void teamNotFound() {
            // given
            given(teamRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> teamService.getTeam(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.TEAM_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("updateTeam")
    class UpdateTeam {

        @Test
        @DisplayName("팀 정보 수정 성공")
        void success() {
            // given
            TeamUpdateRequest request = new TeamUpdateRequest("새 이름", "새 설명");
            given(teamRepository.findById(1L)).willReturn(Optional.of(team));

            // when
            TeamResponse response = teamService.updateTeam(1L, request);

            // then
            assertThat(response.name()).isEqualTo("새 이름");
            assertThat(response.description()).isEqualTo("새 설명");
        }

        @Test
        @DisplayName("이름만 수정")
        void updateNameOnly() {
            // given
            TeamUpdateRequest request = new TeamUpdateRequest("새 이름", null);
            given(teamRepository.findById(1L)).willReturn(Optional.of(team));

            // when
            TeamResponse response = teamService.updateTeam(1L, request);

            // then
            assertThat(response.name()).isEqualTo("새 이름");
            assertThat(response.description()).isEqualTo("우리 교회 찬양팀");
        }

        @Test
        @DisplayName("존재하지 않는 팀 수정 시 예외")
        void teamNotFound() {
            // given
            TeamUpdateRequest request = new TeamUpdateRequest("새 이름", "새 설명");
            given(teamRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> teamService.updateTeam(999L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.TEAM_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("deleteTeam")
    class DeleteTeam {

        @Test
        @DisplayName("팀 삭제 성공")
        void success() {
            // given
            given(teamRepository.findById(1L)).willReturn(Optional.of(team));
            given(teamMemberRepository.findByTeamId(1L)).willReturn(List.of(teamMember));

            // when
            teamService.deleteTeam(1L);

            // then
            verify(teamMemberRepository).deleteAll(List.of(teamMember));
            verify(teamRepository).delete(team);
        }

        @Test
        @DisplayName("존재하지 않는 팀 삭제 시 예외")
        void teamNotFound() {
            // given
            given(teamRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> teamService.deleteTeam(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.TEAM_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("getMembers")
    class GetMembers {

        @Test
        @DisplayName("팀원 목록 조회 성공")
        void success() {
            // given
            given(teamRepository.findById(1L)).willReturn(Optional.of(team));
            given(teamMemberRepository.findByTeamId(1L)).willReturn(List.of(teamMember));

            // when
            List<TeamMemberResponse> responses = teamService.getMembers(1L);

            // then
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).userName()).isEqualTo("테스트유저");
            assertThat(responses.get(0).role()).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("존재하지 않는 팀의 멤버 조회 시 예외")
        void teamNotFound() {
            // given
            given(teamRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> teamService.getMembers(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.TEAM_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("addMember")
    class AddMember {

        private User newUser;

        @BeforeEach
        void setUp() {
            newUser = User.builder()
                    .email("new@test.com")
                    .name("새멤버")
                    .profileImage("new_profile.jpg")
                    .provider(Provider.GOOGLE)
                    .providerId("67890")
                    .build();
            ReflectionTestUtils.setField(newUser, "id", 2L);
        }

        @Test
        @DisplayName("팀원 추가 성공")
        void success() {
            // given
            given(teamRepository.findById(1L)).willReturn(Optional.of(team));
            given(userRepository.findById(2L)).willReturn(Optional.of(newUser));
            given(teamMemberRepository.existsByUserIdAndTeamId(2L, 1L)).willReturn(false);

            TeamMember newMember = TeamMember.builder()
                    .user(newUser)
                    .team(team)
                    .role(TeamRole.VIEWER)
                    .build();
            ReflectionTestUtils.setField(newMember, "id", 2L);
            ReflectionTestUtils.setField(newMember, "createdAt", LocalDateTime.now());
            given(teamMemberRepository.save(any(TeamMember.class))).willReturn(newMember);

            // when
            TeamMemberResponse response = teamService.addMember(1L, 2L);

            // then
            assertThat(response.userName()).isEqualTo("새멤버");
            assertThat(response.role()).isEqualTo("VIEWER");
        }

        @Test
        @DisplayName("이미 팀에 속한 멤버 추가 시 예외")
        void duplicateMember() {
            // given
            given(teamRepository.findById(1L)).willReturn(Optional.of(team));
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(teamMemberRepository.existsByUserIdAndTeamId(1L, 1L)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> teamService.addMember(1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.DUPLICATE_TEAM_MEMBER);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 추가 시 예외")
        void userNotFound() {
            // given
            given(teamRepository.findById(1L)).willReturn(Optional.of(team));
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> teamService.addMember(1L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("updateMemberRole")
    class UpdateMemberRole {

        @Test
        @DisplayName("역할 변경 성공")
        void success() {
            // given
            MemberRoleUpdateRequest request = new MemberRoleUpdateRequest("VIEWER");
            given(teamRepository.findById(1L)).willReturn(Optional.of(team));
            given(teamMemberRepository.findById(1L)).willReturn(Optional.of(teamMember));

            // when
            TeamMemberResponse response = teamService.updateMemberRole(1L, 1L, request);

            // then
            assertThat(response.role()).isEqualTo("VIEWER");
        }

        @Test
        @DisplayName("존재하지 않는 멤버 역할 변경 시 예외")
        void memberNotFound() {
            // given
            MemberRoleUpdateRequest request = new MemberRoleUpdateRequest("VIEWER");
            given(teamRepository.findById(1L)).willReturn(Optional.of(team));
            given(teamMemberRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> teamService.updateMemberRole(1L, 999L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("removeMember")
    class RemoveMember {

        @Test
        @DisplayName("팀원 제거 성공")
        void success() {
            // given
            given(teamRepository.findById(1L)).willReturn(Optional.of(team));
            given(teamMemberRepository.findById(1L)).willReturn(Optional.of(teamMember));

            // when
            teamService.removeMember(1L, 1L);

            // then
            verify(teamMemberRepository).delete(teamMember);
        }
    }

    @Nested
    @DisplayName("regenerateInviteCode")
    class RegenerateInviteCode {

        @Test
        @DisplayName("초대 코드 재생성 성공")
        void success() {
            // given
            given(teamRepository.findById(1L)).willReturn(Optional.of(team));

            // when
            InviteResponse response = teamService.regenerateInviteCode(1L);

            // then
            assertThat(response.inviteCode()).isNotNull();
            assertThat(response.inviteCode()).hasSize(8);
        }
    }

    @Nested
    @DisplayName("joinByInviteCode")
    class JoinByInviteCode {

        private User newUser;

        @BeforeEach
        void setUp() {
            newUser = User.builder()
                    .email("new@test.com")
                    .name("새멤버")
                    .profileImage("new_profile.jpg")
                    .provider(Provider.GOOGLE)
                    .providerId("67890")
                    .build();
            ReflectionTestUtils.setField(newUser, "id", 2L);
        }

        @Test
        @DisplayName("초대 코드로 가입 성공")
        void success() {
            // given
            given(teamRepository.findByInviteCode("abc12345")).willReturn(Optional.of(team));
            given(userRepository.findById(2L)).willReturn(Optional.of(newUser));
            given(teamMemberRepository.existsByUserIdAndTeamId(2L, 1L)).willReturn(false);

            TeamMember newMember = TeamMember.builder()
                    .user(newUser)
                    .team(team)
                    .role(TeamRole.VIEWER)
                    .build();
            ReflectionTestUtils.setField(newMember, "id", 2L);
            given(teamMemberRepository.save(any(TeamMember.class))).willReturn(newMember);

            // when
            TeamResponse response = teamService.joinByInviteCode(2L, "abc12345");

            // then
            assertThat(response.name()).isEqualTo("찬양팀");
            verify(teamMemberRepository).save(any(TeamMember.class));
        }

        @Test
        @DisplayName("유효하지 않은 초대 코드로 가입 시 예외")
        void invalidInviteCode() {
            // given
            given(teamRepository.findByInviteCode("invalid")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> teamService.joinByInviteCode(2L, "invalid"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_INVITE_CODE);
        }

        @Test
        @DisplayName("이미 팀에 속한 사용자가 초대 코드로 가입 시 예외")
        void duplicateMember() {
            // given
            given(teamRepository.findByInviteCode("abc12345")).willReturn(Optional.of(team));
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(teamMemberRepository.existsByUserIdAndTeamId(1L, 1L)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> teamService.joinByInviteCode(1L, "abc12345"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.DUPLICATE_TEAM_MEMBER);
        }
    }
}
