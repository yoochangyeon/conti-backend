package com.conti.domain.team.service;

import com.conti.domain.team.dto.MemberPositionRequest;
import com.conti.domain.team.dto.MemberPositionResponse;
import com.conti.domain.team.entity.MemberPosition;
import com.conti.domain.team.entity.Position;
import com.conti.domain.team.entity.TeamMember;
import com.conti.domain.team.repository.MemberPositionRepository;
import com.conti.domain.team.repository.TeamMemberRepository;
import com.conti.global.error.BusinessException;
import com.conti.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberPositionService {

    private final MemberPositionRepository memberPositionRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public List<MemberPositionResponse> setPositions(Long teamMemberId, List<MemberPositionRequest> requests) {
        TeamMember teamMember = teamMemberRepository.findById(teamMemberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        memberPositionRepository.deleteByTeamMemberId(teamMemberId);
        teamMember.getPositions().clear();

        for (MemberPositionRequest request : requests) {
            MemberPosition position = MemberPosition.builder()
                    .teamMember(teamMember)
                    .position(request.position())
                    .primary(request.isPrimary())
                    .build();
            teamMember.getPositions().add(position);
        }

        return teamMember.getPositions().stream()
                .map(MemberPositionResponse::from)
                .toList();
    }

    public List<MemberPositionResponse> getPositions(Long teamMemberId) {
        return memberPositionRepository.findByTeamMemberId(teamMemberId).stream()
                .map(MemberPositionResponse::from)
                .toList();
    }

    public List<Long> getMemberIdsByPosition(Long teamId, Position position) {
        return memberPositionRepository.findTeamMemberIdsByTeamIdAndPosition(teamId, position);
    }
}
