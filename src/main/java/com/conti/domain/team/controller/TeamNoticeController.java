package com.conti.domain.team.controller;

import com.conti.domain.team.dto.TeamNoticeCreateRequest;
import com.conti.domain.team.dto.TeamNoticeResponse;
import com.conti.domain.team.dto.TeamNoticeUpdateRequest;
import com.conti.domain.team.service.TeamNoticeService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "팀 공지사항", description = "팀 공지사항 관리")
@RestController
@RequestMapping("/api/v1/teams/{teamId}/notices")
@RequiredArgsConstructor
public class TeamNoticeController {

    private final TeamNoticeService teamNoticeService;

    @Operation(summary = "공지사항 목록 조회")
    @TeamAuth(roles = {"VIEWER"})
    @GetMapping
    public ApiResponse<List<TeamNoticeResponse>> getNotices(
            @Parameter(description = "팀 ID") @PathVariable Long teamId
    ) {
        return ApiResponse.ok(teamNoticeService.getNotices(teamId));
    }

    @Operation(summary = "공지사항 생성")
    @TeamAuth(roles = {"ADMIN"})
    @PostMapping
    public ApiResponse<TeamNoticeResponse> createNotice(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @LoginUser Long userId,
            @Valid @RequestBody TeamNoticeCreateRequest request
    ) {
        return ApiResponse.ok(teamNoticeService.createNotice(teamId, userId, request));
    }

    @Operation(summary = "공지사항 수정")
    @TeamAuth(roles = {"ADMIN"})
    @PatchMapping("/{noticeId}")
    public ApiResponse<TeamNoticeResponse> updateNotice(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "공지 ID") @PathVariable Long noticeId,
            @Valid @RequestBody TeamNoticeUpdateRequest request
    ) {
        return ApiResponse.ok(teamNoticeService.updateNotice(teamId, noticeId, request));
    }

    @Operation(summary = "공지사항 삭제")
    @TeamAuth(roles = {"ADMIN"})
    @DeleteMapping("/{noticeId}")
    public ApiResponse<Void> deleteNotice(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "공지 ID") @PathVariable Long noticeId
    ) {
        teamNoticeService.deleteNotice(teamId, noticeId);
        return ApiResponse.ok();
    }

    @Operation(summary = "공지사항 고정/해제")
    @TeamAuth(roles = {"ADMIN"})
    @PatchMapping("/{noticeId}/pin")
    public ApiResponse<TeamNoticeResponse> togglePin(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "공지 ID") @PathVariable Long noticeId
    ) {
        return ApiResponse.ok(teamNoticeService.togglePin(teamId, noticeId));
    }
}
