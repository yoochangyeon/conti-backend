package com.conti.domain.team.controller;

import com.conti.domain.team.dto.InviteResponse;
import com.conti.domain.team.dto.MemberRoleUpdateRequest;
import com.conti.domain.team.dto.TeamCreateRequest;
import com.conti.domain.team.dto.TeamMemberResponse;
import com.conti.domain.team.dto.TeamResponse;
import com.conti.domain.team.dto.TeamUpdateRequest;
import com.conti.domain.team.service.TeamService;
import com.conti.global.auth.LoginUser;
import com.conti.global.auth.TeamAuth;
import com.conti.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "팀", description = "팀 관리 및 멤버 초대")
@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @Operation(summary = "팀 생성")
    @PostMapping
    public ApiResponse<TeamResponse> createTeam(
            @LoginUser Long userId,
            @Valid @RequestBody TeamCreateRequest request
    ) {
        return ApiResponse.ok(teamService.createTeam(userId, request));
    }

    @Operation(summary = "팀 조회")
    @GetMapping("/{teamId}")
    public ApiResponse<TeamResponse> getTeam(
            @Parameter(description = "팀 ID") @PathVariable Long teamId) {
        return ApiResponse.ok(teamService.getTeam(teamId));
    }

    @Operation(summary = "팀 수정")
    @TeamAuth(roles = {"ADMIN"})
    @PatchMapping("/{teamId}")
    public ApiResponse<TeamResponse> updateTeam(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @RequestBody TeamUpdateRequest request
    ) {
        return ApiResponse.ok(teamService.updateTeam(teamId, request));
    }

    @Operation(summary = "팀 삭제")
    @TeamAuth(roles = {"ADMIN"})
    @DeleteMapping("/{teamId}")
    public ApiResponse<Void> deleteTeam(
            @Parameter(description = "팀 ID") @PathVariable Long teamId) {
        teamService.deleteTeam(teamId);
        return ApiResponse.ok();
    }

    @Operation(summary = "팀 멤버 목록")
    @TeamAuth(roles = {"ADMIN", "VIEWER", "GUEST"})
    @GetMapping("/{teamId}/members")
    public ApiResponse<List<TeamMemberResponse>> getMembers(
            @Parameter(description = "팀 ID") @PathVariable Long teamId) {
        return ApiResponse.ok(teamService.getMembers(teamId));
    }

    @Operation(summary = "멤버 추가")
    @TeamAuth(roles = {"ADMIN"})
    @PostMapping("/{teamId}/members")
    public ApiResponse<TeamMemberResponse> addMember(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "추가할 사용자 ID") @RequestParam Long userId
    ) {
        return ApiResponse.ok(teamService.addMember(teamId, userId));
    }

    @Operation(summary = "멤버 역할 변경")
    @TeamAuth(roles = {"ADMIN"})
    @PatchMapping("/{teamId}/members/{memberId}")
    public ApiResponse<TeamMemberResponse> updateRole(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "멤버 ID") @PathVariable Long memberId,
            @Valid @RequestBody MemberRoleUpdateRequest request
    ) {
        return ApiResponse.ok(teamService.updateMemberRole(teamId, memberId, request));
    }

    @Operation(summary = "멤버 제거")
    @TeamAuth(roles = {"ADMIN"})
    @DeleteMapping("/{teamId}/members/{memberId}")
    public ApiResponse<Void> removeMember(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "멤버 ID") @PathVariable Long memberId
    ) {
        teamService.removeMember(teamId, memberId);
        return ApiResponse.ok();
    }

    @Operation(summary = "초대 코드 재생성")
    @TeamAuth(roles = {"ADMIN"})
    @PostMapping("/{teamId}/invite")
    public ApiResponse<InviteResponse> regenerateInvite(
            @Parameter(description = "팀 ID") @PathVariable Long teamId) {
        return ApiResponse.ok(teamService.regenerateInviteCode(teamId));
    }

    @Operation(summary = "초대 코드로 팀 가입")
    @PostMapping("/join/{inviteCode}")
    public ApiResponse<TeamResponse> joinByInviteCode(
            @LoginUser Long userId,
            @Parameter(description = "초대 코드") @PathVariable String inviteCode
    ) {
        return ApiResponse.ok(teamService.joinByInviteCode(userId, inviteCode));
    }
}
