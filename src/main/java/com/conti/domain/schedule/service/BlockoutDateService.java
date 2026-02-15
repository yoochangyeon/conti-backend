package com.conti.domain.schedule.service;

import com.conti.domain.schedule.dto.BlockoutDateCreateRequest;
import com.conti.domain.schedule.dto.BlockoutDateResponse;
import com.conti.domain.schedule.entity.BlockoutDate;
import com.conti.domain.schedule.repository.BlockoutDateRepository;
import com.conti.domain.team.entity.TeamMember;
import com.conti.domain.team.entity.TeamRole;
import com.conti.domain.team.repository.TeamMemberRepository;
import com.conti.global.error.BusinessException;
import com.conti.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockoutDateService {

    private final BlockoutDateRepository blockoutDateRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public BlockoutDateResponse create(Long teamMemberId, Long userId, BlockoutDateCreateRequest request) {
        TeamMember targetMember = teamMemberRepository.findById(teamMemberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Validate date range
        if (request.endDate().isBefore(request.startDate())) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }

        // Check permission: self or ADMIN
        if (!targetMember.getUser().getId().equals(userId)) {
            TeamMember requestingMember = teamMemberRepository
                    .findByUserIdAndTeamId(userId, targetMember.getTeam().getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));

            if (requestingMember.getRole() != TeamRole.ADMIN) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
        }

        BlockoutDate blockoutDate = BlockoutDate.builder()
                .teamMember(targetMember)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .reason(request.reason())
                .build();

        BlockoutDate saved = blockoutDateRepository.save(blockoutDate);
        return BlockoutDateResponse.from(saved);
    }

    public List<BlockoutDateResponse> getForMember(Long teamMemberId) {
        return blockoutDateRepository.findByTeamMemberIdOrderByStartDate(teamMemberId).stream()
                .map(BlockoutDateResponse::from)
                .toList();
    }

    public List<BlockoutDateResponse> getForTeamInRange(Long teamId, LocalDate from, LocalDate to) {
        return blockoutDateRepository.findByTeamIdAndDateRange(teamId, from, to).stream()
                .map(BlockoutDateResponse::from)
                .toList();
    }

    @Transactional
    public void delete(Long blockoutId, Long userId) {
        BlockoutDate blockoutDate = blockoutDateRepository.findById(blockoutId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BLOCKOUT_NOT_FOUND));

        TeamMember owner = blockoutDate.getTeamMember();

        // Check permission: self or ADMIN
        if (!owner.getUser().getId().equals(userId)) {
            TeamMember requestingMember = teamMemberRepository
                    .findByUserIdAndTeamId(userId, owner.getTeam().getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));

            if (requestingMember.getRole() != TeamRole.ADMIN) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
        }

        blockoutDateRepository.delete(blockoutDate);
    }
}
