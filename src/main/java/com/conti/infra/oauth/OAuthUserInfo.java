package com.conti.infra.oauth;

public record OAuthUserInfo(
        String email,
        String name,
        String profileImage,
        String provider,
        String providerId
) {
}
