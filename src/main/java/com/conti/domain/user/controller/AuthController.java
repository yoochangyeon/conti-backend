package com.conti.domain.user.controller;

import com.conti.domain.user.dto.LoginRequest;
import com.conti.domain.user.dto.TokenResponse;
import com.conti.domain.user.service.AuthService;
import com.conti.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "인증", description = "소셜 로그인 및 토큰 관리")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "소셜 로그인", description = "카카오/구글 OAuth 인가 코드로 로그인합니다")
    @PostMapping("/login/{provider}")
    public ApiResponse<TokenResponse> login(
            @Parameter(description = "OAuth 프로바이더 (kakao, google)") @PathVariable String provider,
            @Valid @RequestBody LoginRequest request) {
        TokenResponse tokenResponse = authService.login(provider, request.code());
        return ApiResponse.ok(tokenResponse);
    }

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 액세스 토큰을 갱신합니다")
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        TokenResponse tokenResponse = authService.refresh(refreshToken);
        return ApiResponse.ok(tokenResponse);
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.ok();
    }
}
