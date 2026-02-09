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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoOAuth2Client kakaoOAuth2Client;
    private final GoogleOAuth2Client googleOAuth2Client;

    @Transactional
    public TokenResponse login(String provider, String code) {
        OAuthUserInfo userInfo = getOAuthUserInfo(provider, code);

        Provider providerEnum = Provider.valueOf(provider.toUpperCase());

        User user = userRepository.findByProviderAndProviderId(providerEnum, userInfo.providerId())
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(userInfo.email())
                        .name(userInfo.name())
                        .profileImage(userInfo.profileImage())
                        .provider(providerEnum)
                        .providerId(userInfo.providerId())
                        .build()));

        TokenDto tokenDto = jwtTokenProvider.generateTokens(user.getId());
        return new TokenResponse(tokenDto.accessToken(), tokenDto.refreshToken());
    }

    public TokenResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        String newAccessToken = jwtTokenProvider.generateAccessToken(userId);
        return new TokenResponse(newAccessToken, refreshToken);
    }

    private OAuthUserInfo getOAuthUserInfo(String provider, String code) {
        return switch (provider.toUpperCase()) {
            case "KAKAO" -> kakaoOAuth2Client.getUserInfo(code);
            case "GOOGLE" -> googleOAuth2Client.getUserInfo(code);
            default -> throw new BusinessException(ErrorCode.INVALID_INPUT);
        };
    }
}
