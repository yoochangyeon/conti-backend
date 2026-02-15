package com.conti.domain.song.controller;

import com.conti.domain.song.dto.ArrangementCreateRequest;
import com.conti.domain.song.dto.ArrangementResponse;
import com.conti.domain.song.dto.ArrangementUpdateRequest;
import com.conti.domain.song.service.ArrangementService;
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

@Tag(name = "편곡", description = "곡별 편곡(어레인지먼트) 관리")
@RestController
@RequestMapping("/api/v1/teams/{teamId}/songs/{songId}/arrangements")
@RequiredArgsConstructor
public class ArrangementController {

    private final ArrangementService arrangementService;

    @Operation(summary = "편곡 목록 조회")
    @TeamAuth(roles = {"ADMIN", "VIEWER", "GUEST"})
    @GetMapping
    public ApiResponse<List<ArrangementResponse>> getArrangements(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "곡 ID") @PathVariable Long songId
    ) {
        return ApiResponse.ok(arrangementService.getArrangements(teamId, songId));
    }

    @Operation(summary = "편곡 상세 조회")
    @TeamAuth(roles = {"ADMIN", "VIEWER", "GUEST"})
    @GetMapping("/{arrangementId}")
    public ApiResponse<ArrangementResponse> getArrangement(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "곡 ID") @PathVariable Long songId,
            @Parameter(description = "편곡 ID") @PathVariable Long arrangementId
    ) {
        return ApiResponse.ok(arrangementService.getArrangement(teamId, songId, arrangementId));
    }

    @Operation(summary = "편곡 추가")
    @TeamAuth(roles = {"ADMIN"})
    @PostMapping
    public ApiResponse<ArrangementResponse> createArrangement(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "곡 ID") @PathVariable Long songId,
            @Valid @RequestBody ArrangementCreateRequest request
    ) {
        return ApiResponse.ok(arrangementService.createArrangement(teamId, songId, request));
    }

    @Operation(summary = "편곡 수정")
    @TeamAuth(roles = {"ADMIN"})
    @PatchMapping("/{arrangementId}")
    public ApiResponse<ArrangementResponse> updateArrangement(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "곡 ID") @PathVariable Long songId,
            @Parameter(description = "편곡 ID") @PathVariable Long arrangementId,
            @RequestBody ArrangementUpdateRequest request
    ) {
        return ApiResponse.ok(arrangementService.updateArrangement(teamId, songId, arrangementId, request));
    }

    @Operation(summary = "편곡 삭제", description = "기본 편곡은 삭제 불가")
    @TeamAuth(roles = {"ADMIN"})
    @DeleteMapping("/{arrangementId}")
    public ApiResponse<Void> deleteArrangement(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "곡 ID") @PathVariable Long songId,
            @Parameter(description = "편곡 ID") @PathVariable Long arrangementId
    ) {
        arrangementService.deleteArrangement(teamId, songId, arrangementId);
        return ApiResponse.ok();
    }
}
