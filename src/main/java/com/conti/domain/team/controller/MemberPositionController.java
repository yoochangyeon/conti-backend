package com.conti.domain.team.controller;

import com.conti.domain.team.dto.MemberPositionRequest;
import com.conti.domain.team.dto.MemberPositionResponse;
import com.conti.domain.team.dto.TeamMemberResponse;
import com.conti.domain.team.entity.Position;
import com.conti.domain.team.repository.TeamMemberRepository;
import com.conti.domain.team.service.MemberPositionService;
import com.conti.global.auth.TeamAuth;
import com.conti.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "멤버 포지션", description = "멤버 포지션 관리")
@RestController
@RequestMapping("/api/v1/teams/{teamId}")
@RequiredArgsConstructor
public class MemberPositionController {

    private final MemberPositionService memberPositionService;
    private final TeamMemberRepository teamMemberRepository;

    @Operation(summary = "멤버 포지션 설정", description = "멤버의 포지션을 전체 교체합니다")
    @TeamAuth(roles = {"ADMIN"})
    @PutMapping("/members/{memberId}/positions")
    public ApiResponse<List<MemberPositionResponse>> setPositions(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "멤버 ID") @PathVariable Long memberId,
            @Valid @RequestBody List<MemberPositionRequest> requests
    ) {
        return ApiResponse.ok(memberPositionService.setPositions(memberId, requests));
    }

    @Operation(summary = "포지션별 멤버 조회")
    @TeamAuth(roles = {"ADMIN", "VIEWER"})
    @GetMapping("/positions/{position}/members")
    public ApiResponse<List<TeamMemberResponse>> getMembersByPosition(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "포지션") @PathVariable Position position
    ) {
        List<Long> memberIds = memberPositionService.getMemberIdsByPosition(teamId, position);
        List<TeamMemberResponse> responses = memberIds.stream()
                .map(id -> teamMemberRepository.findById(id).orElse(null))
                .filter(member -> member != null)
                .map(TeamMemberResponse::from)
                .toList();
        return ApiResponse.ok(responses);
    }
}
