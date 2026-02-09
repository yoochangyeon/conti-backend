package com.conti.domain.user.service;

import com.conti.domain.team.entity.Team;
import com.conti.domain.team.entity.TeamMember;
import com.conti.domain.team.entity.TeamRole;
import com.conti.domain.team.repository.TeamMemberRepository;
import com.conti.domain.user.dto.UserResponse;
import com.conti.domain.user.dto.UserTeamResponse;
import com.conti.domain.user.dto.UserUpdateRequest;
import com.conti.domain.user.entity.Provider;
import com.conti.domain.user.entity.User;
import com.conti.domain.user.repository.UserRepository;
import com.conti.global.error.BusinessException;
import com.conti.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("getProfile")
    class GetProfile {

        @Test
        @DisplayName("사용자 ID로 프로필을 조회한다")
        void getProfile_success() {
            // given
            Long userId = 1L;
            User user = User.builder()
                    .email("test@conti.com")
                    .name("TestUser")
                    .profileImage("http://img.com/profile.jpg")
                    .provider(Provider.KAKAO)
                    .providerId("kakao-123")
                    .build();

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            UserResponse response = userService.getProfile(userId);

            // then
            assertThat(response.email()).isEqualTo("test@conti.com");
            assertThat(response.name()).isEqualTo("TestUser");
            assertThat(response.profileImage()).isEqualTo("http://img.com/profile.jpg");
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID이면 예외를 던진다")
        void getProfile_userNotFound() {
            // given
            Long userId = 999L;
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getProfile(userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("updateProfile")
    class UpdateProfile {

        @Test
        @DisplayName("사용자 프로필을 수정한다")
        void updateProfile_success() {
            // given
            Long userId = 1L;
            User user = User.builder()
                    .email("test@conti.com")
                    .name("OldName")
                    .profileImage("http://img.com/old.jpg")
                    .provider(Provider.KAKAO)
                    .providerId("kakao-123")
                    .build();

            UserUpdateRequest request = new UserUpdateRequest("NewName", "http://img.com/new.jpg");
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            UserResponse response = userService.updateProfile(userId, request);

            // then
            assertThat(response.name()).isEqualTo("NewName");
            assertThat(response.profileImage()).isEqualTo("http://img.com/new.jpg");
        }

        @Test
        @DisplayName("존재하지 않는 사용자의 프로필 수정 시 예외를 던진다")
        void updateProfile_userNotFound() {
            // given
            Long userId = 999L;
            UserUpdateRequest request = new UserUpdateRequest("NewName", null);
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.updateProfile(userId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("getUserTeams")
    class GetUserTeams {

        @Test
        @DisplayName("사용자가 속한 팀 목록을 조회한다")
        void getUserTeams_success() {
            // given
            Long userId = 1L;

            Team team = Team.builder()
                    .name("Praise Team")
                    .description("Sunday worship team")
                    .inviteCode("ABC123")
                    .build();

            TeamMember teamMember = TeamMember.builder()
                    .team(team)
                    .role(TeamRole.ADMIN)
                    .build();

            given(teamMemberRepository.findByUserId(userId)).willReturn(List.of(teamMember));

            // when
            List<UserTeamResponse> response = userService.getUserTeams(userId);

            // then
            assertThat(response).hasSize(1);
            assertThat(response.get(0).teamName()).isEqualTo("Praise Team");
            assertThat(response.get(0).role()).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("팀이 없으면 빈 리스트를 반환한다")
        void getUserTeams_emptyList() {
            // given
            Long userId = 1L;
            given(teamMemberRepository.findByUserId(userId)).willReturn(List.of());

            // when
            List<UserTeamResponse> response = userService.getUserTeams(userId);

            // then
            assertThat(response).isEmpty();
        }
    }
}
