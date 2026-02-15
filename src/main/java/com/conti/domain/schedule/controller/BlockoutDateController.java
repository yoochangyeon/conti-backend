package com.conti.domain.schedule.controller;

import com.conti.domain.schedule.dto.BlockoutDateCreateRequest;
import com.conti.domain.schedule.dto.BlockoutDateResponse;
import com.conti.domain.schedule.service.BlockoutDateService;
import com.conti.global.auth.LoginUser;
import com.conti.global.auth.TeamAuth;
import com.conti.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "부재 일정", description = "멤버 부재 일정 관리")
@RestController
@RequestMapping("/api/v1/teams/{teamId}")
@RequiredArgsConstructor
public class BlockoutDateController {

    private final BlockoutDateService blockoutDateService;

    @Operation(summary = "멤버 부재 일정 조회")
    @TeamAuth(roles = {"ADMIN", "VIEWER"})
    @GetMapping("/members/{memberId}/blockouts")
    public ApiResponse<List<BlockoutDateResponse>> getMemberBlockouts(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "멤버 ID") @PathVariable Long memberId
    ) {
        return ApiResponse.ok(blockoutDateService.getForMember(memberId));
    }

    @Operation(summary = "멤버 부재 일정 생성")
    @TeamAuth(roles = {"ADMIN", "VIEWER"})
    @PostMapping("/members/{memberId}/blockouts")
    public ApiResponse<BlockoutDateResponse> createBlockout(
            @LoginUser Long userId,
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "멤버 ID") @PathVariable Long memberId,
            @Valid @RequestBody BlockoutDateCreateRequest request
    ) {
        return ApiResponse.ok(blockoutDateService.create(memberId, userId, request));
    }

    @Operation(summary = "부재 일정 삭제")
    @TeamAuth(roles = {"ADMIN", "VIEWER"})
    @DeleteMapping("/blockouts/{blockoutId}")
    public ApiResponse<Void> deleteBlockout(
            @LoginUser Long userId,
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "부재 일정 ID") @PathVariable Long blockoutId
    ) {
        blockoutDateService.delete(blockoutId, userId);
        return ApiResponse.ok();
    }

    @Operation(summary = "팀 부재 일정 조회 (날짜 범위)")
    @TeamAuth(roles = {"ADMIN"})
    @GetMapping("/blockouts")
    public ApiResponse<List<BlockoutDateResponse>> getTeamBlockouts(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "시작 날짜") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "종료 날짜") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ApiResponse.ok(blockoutDateService.getForTeamInRange(teamId, fromDate, toDate));
    }
}
