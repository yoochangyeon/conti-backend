package com.conti.domain.user.service;

import com.conti.domain.user.dto.TokenResponse;
import com.conti.domain.user.entity.Provider;
import com.conti.domain.user.entity.User;
import com.conti.domain.user.repository.UserRepository;
import com.conti.global.auth.jwt.JwtTokenProvider;
import com.conti.global.auth.jwt.TokenDto;
import com.conti.global.error.BusinessException;
import com.conti.global.error.ErrorCode;
import com.conti.infra.oauth.GoogleOAuth2Client;
import com.conti.infra.oauth.KakaoOAuth2Client;
import com.conti.infra.oauth.OAuthUserInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private KakaoOAuth2Client kakaoOAuth2Client;

    @Mock
    private GoogleOAuth2Client googleOAuth2Client;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("카카오 로그인 - 기존 사용자가 있으면 토큰을 발급한다")
        void loginWithKakao_existingUser() {
            // given
            String provider = "kakao";
            String code = "auth-code";
            OAuthUserInfo userInfo = new OAuthUserInfo(
                    "user@kakao.com", "KakaoUser", "http://img.kakao.com/profile.jpg", "KAKAO", "kakao-123");

            User existingUser = User.builder()
                    .email("user@kakao.com")
                    .name("KakaoUser")
                    .provider(Provider.KAKAO)
                    .providerId("kakao-123")
                    .build();

            given(kakaoOAuth2Client.getUserInfo(code)).willReturn(userInfo);
            given(userRepository.findByProviderAndProviderId(Provider.KAKAO, "kakao-123"))
                    .willReturn(Optional.of(existingUser));
            given(jwtTokenProvider.generateTokens(any()))
                    .willReturn(new TokenDto("access-token", "refresh-token"));

            // when
            TokenResponse response = authService.login(provider, code);

            // then
            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("카카오 로그인 - 신규 사용자면 생성 후 토큰을 발급한다")
        void loginWithKakao_newUser() {
            // given
            String provider = "kakao";
            String code = "auth-code";
            OAuthUserInfo userInfo = new OAuthUserInfo(
                    "new@kakao.com", "NewUser", null, "KAKAO", "kakao-456");

            User savedUser = User.builder()
                    .email("new@kakao.com")
                    .name("NewUser")
                    .provider(Provider.KAKAO)
                    .providerId("kakao-456")
                    .build();

            given(kakaoOAuth2Client.getUserInfo(code)).willReturn(userInfo);
            given(userRepository.findByProviderAndProviderId(Provider.KAKAO, "kakao-456"))
                    .willReturn(Optional.empty());
            given(userRepository.save(any(User.class))).willReturn(savedUser);
            given(jwtTokenProvider.generateTokens(any()))
                    .willReturn(new TokenDto("access-token", "refresh-token"));

            // when
            TokenResponse response = authService.login(provider, code);

            // then
            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("구글 로그인 - 기존 사용자가 있으면 토큰을 발급한다")
        void loginWithGoogle_existingUser() {
            // given
            String provider = "google";
            String code = "google-auth-code";
            OAuthUserInfo userInfo = new OAuthUserInfo(
                    "user@gmail.com", "GoogleUser", "http://img.google.com/profile.jpg", "GOOGLE", "google-123");

            User existingUser = User.builder()
                    .email("user@gmail.com")
                    .name("GoogleUser")
                    .provider(Provider.GOOGLE)
                    .providerId("google-123")
                    .build();

            given(googleOAuth2Client.getUserInfo(code)).willReturn(userInfo);
            given(userRepository.findByProviderAndProviderId(Provider.GOOGLE, "google-123"))
                    .willReturn(Optional.of(existingUser));
            given(jwtTokenProvider.generateTokens(any()))
                    .willReturn(new TokenDto("access-token", "refresh-token"));

            // when
            TokenResponse response = authService.login(provider, code);

            // then
            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");
        }

        @Test
        @DisplayName("지원하지 않는 provider면 예외를 던진다")
        void loginWithUnsupportedProvider_throwsException() {
            // given
            String provider = "facebook";
            String code = "auth-code";

            // when & then
            assertThatThrownBy(() -> authService.login(provider, code))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_INPUT);
        }
    }

    @Nested
    @DisplayName("refresh")
    class Refresh {

        @Test
        @DisplayName("유효한 refreshToken이면 새로운 accessToken을 발급한다")
        void refreshWithValidToken() {
            // given
            String refreshToken = "valid-refresh-token";
            given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
            given(jwtTokenProvider.getUserId(refreshToken)).willReturn(1L);
            given(jwtTokenProvider.generateAccessToken(1L)).willReturn("new-access-token");

            // when
            TokenResponse response = authService.refresh(refreshToken);

            // then
            assertThat(response.accessToken()).isEqualTo("new-access-token");
            assertThat(response.refreshToken()).isEqualTo(refreshToken);
        }

        @Test
        @DisplayName("유효하지 않은 refreshToken이면 예외를 던진다")
        void refreshWithInvalidToken_throwsException() {
            // given
            String refreshToken = "invalid-refresh-token";
            given(jwtTokenProvider.validateToken(refreshToken)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.refresh(refreshToken))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_TOKEN);
        }
    }
}
