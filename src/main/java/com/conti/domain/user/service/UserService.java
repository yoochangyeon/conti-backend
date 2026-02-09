package com.conti.domain.user.service;

import com.conti.domain.team.entity.TeamMember;
import com.conti.domain.team.repository.TeamMemberRepository;
import com.conti.domain.user.dto.UserResponse;
import com.conti.domain.user.dto.UserTeamResponse;
import com.conti.domain.user.dto.UserUpdateRequest;
import com.conti.domain.user.entity.User;
import com.conti.domain.user.repository.UserRepository;
import com.conti.global.error.BusinessException;
import com.conti.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.updateName(request.name());
        user.updateProfileImage(request.profileImage());

        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public List<UserTeamResponse> getUserTeams(Long userId) {
        List<TeamMember> teamMembers = teamMemberRepository.findByUserId(userId);
        return teamMembers.stream()
                .map(UserTeamResponse::from)
                .toList();
    }
}
