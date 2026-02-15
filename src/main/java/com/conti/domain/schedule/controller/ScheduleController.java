package com.conti.domain.schedule.controller;

import com.conti.domain.schedule.dto.ScheduleBulkCreateRequest;
import com.conti.domain.schedule.dto.ScheduleBulkResult;
import com.conti.domain.schedule.dto.ScheduleMatrixResponse;
import com.conti.domain.schedule.dto.ScheduleRespondRequest;
import com.conti.domain.schedule.dto.ScheduleSignupRequest;
import com.conti.domain.schedule.dto.ServiceScheduleResponse;
import com.conti.domain.schedule.service.ScheduleService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "봉사 스케줄", description = "봉사 배정/응답 관리")
@RestController
@RequestMapping("/api/v1/teams/{teamId}")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Operation(summary = "콘티 스케줄 조회")
    @TeamAuth(roles = {"ADMIN", "VIEWER"})
    @GetMapping("/setlists/{setlistId}/schedules")
    public ApiResponse<List<ServiceScheduleResponse>> getSchedules(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "콘티 ID") @PathVariable Long setlistId
    ) {
        return ApiResponse.ok(scheduleService.getSchedulesForSetlist(setlistId));
    }

    @Operation(summary = "멤버 일괄 배정", description = "콘티에 멤버를 일괄 배정합니다")
    @TeamAuth(roles = {"ADMIN"})
    @PostMapping("/setlists/{setlistId}/schedules")
    public ApiResponse<ScheduleBulkResult> scheduleMembers(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "콘티 ID") @PathVariable Long setlistId,
            @Valid @RequestBody ScheduleBulkCreateRequest request
    ) {
        return ApiResponse.ok(scheduleService.scheduleMembers(setlistId, request));
    }

    @Operation(summary = "셀프 사인업", description = "본인이 직접 오픈 포지션에 사인업합니다")
    @TeamAuth(roles = {"ADMIN", "VIEWER"})
    @PostMapping("/setlists/{setlistId}/schedules/signup")
    public ApiResponse<ServiceScheduleResponse> signup(
            @LoginUser Long userId,
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "콘티 ID") @PathVariable Long setlistId,
            @Valid @RequestBody ScheduleSignupRequest request
    ) {
        return ApiResponse.ok(scheduleService.signup(setlistId, userId, request));
    }

    @Operation(summary = "스케줄 삭제")
    @TeamAuth(roles = {"ADMIN"})
    @DeleteMapping("/schedules/{scheduleId}")
    public ApiResponse<Void> removeSchedule(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "스케줄 ID") @PathVariable Long scheduleId
    ) {
        scheduleService.removeSchedule(scheduleId);
        return ApiResponse.ok();
    }

    @Operation(summary = "스케줄 응답 (수락/거절)")
    @TeamAuth(roles = {"ADMIN", "VIEWER"})
    @PatchMapping("/schedules/{scheduleId}/respond")
    public ApiResponse<ServiceScheduleResponse> respond(
            @LoginUser Long userId,
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "스케줄 ID") @PathVariable Long scheduleId,
            @Valid @RequestBody ScheduleRespondRequest request
    ) {
        return ApiResponse.ok(scheduleService.respond(scheduleId, userId, request));
    }

    @Operation(summary = "나의 스케줄 조회")
    @TeamAuth(roles = {"ADMIN", "VIEWER"})
    @GetMapping("/my-schedules")
    public ApiResponse<List<ServiceScheduleResponse>> getMySchedules(
            @LoginUser Long userId,
            @Parameter(description = "팀 ID") @PathVariable Long teamId
    ) {
        return ApiResponse.ok(scheduleService.getMySchedules(teamId, userId));
    }

    @Operation(summary = "스케줄 매트릭스 조회", description = "날짜 x 포지션 매트릭스 형태로 스케줄을 조회합니다")
    @TeamAuth(roles = {"ADMIN", "VIEWER"})
    @GetMapping("/schedules/matrix")
    public ApiResponse<ScheduleMatrixResponse> getScheduleMatrix(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "시작일") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "종료일") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.ok(scheduleService.getScheduleMatrix(teamId, from, to));
    }
}
