package com.conti.global.auth.jwt;

public record TokenDto(
        String accessToken,
        String refreshToken
) {
}
