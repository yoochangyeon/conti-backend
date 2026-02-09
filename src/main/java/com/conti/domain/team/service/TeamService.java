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
import com.conti.domain.user.entity.User;
import com.conti.domain.user.repository.UserRepository;
import com.conti.global.error.BusinessException;
import com.conti.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public TeamResponse createTeam(Long userId, TeamCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Team team = Team.builder()
                .name(request.name())
                .description(request.description())
                .inviteCode(generateInviteCode())
                .build();
        teamRepository.save(team);

        TeamMember member = TeamMember.builder()
                .user(user)
                .team(team)
                .role(TeamRole.ADMIN)
                .build();
        teamMemberRepository.save(member);

        return TeamResponse.from(team);
    }

    @Transactional(readOnly = true)
    public TeamResponse getTeam(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));
        return TeamResponse.from(team);
    }

    @Transactional
    public TeamResponse updateTeam(Long teamId, TeamUpdateRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

        if (request.name() != null) {
            team.updateName(request.name());
        }
        if (request.description() != null) {
            team.updateDescription(request.description());
        }

        return TeamResponse.from(team);
    }

    @Transactional
    public void deleteTeam(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

        List<TeamMember> members = teamMemberRepository.findByTeamId(teamId);
        teamMemberRepository.deleteAll(members);
        teamRepository.delete(team);
    }

    @Transactional(readOnly = true)
    public List<TeamMemberResponse> getMembers(Long teamId) {
        teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

        return teamMemberRepository.findByTeamId(teamId).stream()
                .map(TeamMemberResponse::from)
                .toList();
    }

    @Transactional
    public TeamMemberResponse addMember(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (teamMemberRepository.existsByUserIdAndTeamId(userId, teamId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_TEAM_MEMBER);
        }

        TeamMember member = TeamMember.builder()
                .user(user)
                .team(team)
                .role(TeamRole.VIEWER)
                .build();
        teamMemberRepository.save(member);

        return TeamMemberResponse.from(member);
    }

    @Transactional
    public TeamMemberResponse updateMemberRole(Long teamId, Long memberId, MemberRoleUpdateRequest request) {
        teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        member.updateRole(TeamRole.valueOf(request.role()));

        return TeamMemberResponse.from(member);
    }

    @Transactional
    public void removeMember(Long teamId, Long memberId) {
        teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        teamMemberRepository.delete(member);
    }

    @Transactional
    public InviteResponse regenerateInviteCode(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

        String newInviteCode = generateInviteCode();
        team.updateInviteCode(newInviteCode);

        return new InviteResponse(newInviteCode);
    }

    @Transactional
    public TeamResponse joinByInviteCode(Long userId, String inviteCode) {
        Team team = teamRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INVITE_CODE));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (teamMemberRepository.existsByUserIdAndTeamId(userId, team.getId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_TEAM_MEMBER);
        }

        TeamMember member = TeamMember.builder()
                .user(user)
                .team(team)
                .role(TeamRole.VIEWER)
                .build();
        teamMemberRepository.save(member);

        return TeamResponse.from(team);
    }

    private String generateInviteCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
