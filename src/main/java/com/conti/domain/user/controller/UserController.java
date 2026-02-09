package com.conti.domain.user.controller;

import com.conti.domain.user.dto.UserResponse;
import com.conti.domain.user.dto.UserTeamResponse;
import com.conti.domain.user.dto.UserUpdateRequest;
import com.conti.domain.user.service.UserService;
import com.conti.global.auth.LoginUser;
import com.conti.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "사용자", description = "사용자 프로필 관리")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 프로필 조회")
    @GetMapping("/me")
    public ApiResponse<UserResponse> getMyProfile(@LoginUser Long userId) {
        UserResponse response = userService.getProfile(userId);
        return ApiResponse.ok(response);
    }

    @Operation(summary = "프로필 수정")
    @PatchMapping("/me")
    public ApiResponse<UserResponse> updateMyProfile(
            @LoginUser Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userService.updateProfile(userId, request);
        return ApiResponse.ok(response);
    }

    @Operation(summary = "내 팀 목록 조회")
    @GetMapping("/me/teams")
    public ApiResponse<List<UserTeamResponse>> getMyTeams(@LoginUser Long userId) {
        List<UserTeamResponse> response = userService.getUserTeams(userId);
        return ApiResponse.ok(response);
    }
}
