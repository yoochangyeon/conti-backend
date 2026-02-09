package com.conti.domain.user.controller;

import com.conti.domain.user.dto.TokenResponse;
import com.conti.domain.user.entity.Provider;
import com.conti.domain.user.entity.User;
import com.conti.domain.user.repository.UserRepository;
import com.conti.global.auth.jwt.JwtTokenProvider;
import com.conti.global.auth.jwt.TokenDto;
import com.conti.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "개발", description = "개발/테스트 전용 API")
@RestController
@RequestMapping("/api/v1/dev")
@RequiredArgsConstructor
public class DevAuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "테스트 로그인", description = "OAuth 없이 테스트 계정으로 로그인합니다")
    @PostMapping("/test-login")
    public ApiResponse<TokenResponse> testLogin() {
        return testLoginWithIndex(1);
    }

    @Operation(summary = "테스트 로그인 (멀티유저)", description = "인덱스별 다른 테스트 계정으로 로그인합니다 (E2E 테스트용)")
    @PostMapping("/test-login/{userIndex}")
    public ApiResponse<TokenResponse> testLoginWithIndex(@PathVariable int userIndex) {
        String suffix = userIndex == 1 ? "" : String.valueOf(userIndex);
        String email = "test" + suffix + "@conti.com";
        String name = "TestUser" + suffix;
        String providerId = "test-id" + suffix;

        User user = userRepository.findByProviderAndProviderId(Provider.KAKAO, providerId)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(email)
                        .name(name)
                        .provider(Provider.KAKAO)
                        .providerId(providerId)
                        .build()));

        TokenDto tokenDto = jwtTokenProvider.generateTokens(user.getId());
        return ApiResponse.ok(new TokenResponse(tokenDto.accessToken(), tokenDto.refreshToken()));
    }
}
