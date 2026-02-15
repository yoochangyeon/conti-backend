package com.conti.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "디바이스 토큰 등록 요청")
public record DeviceTokenRequest(
        @Schema(description = "FCM 토큰", example = "dGVzdC10b2tlbg==")
        @NotBlank(message = "FCM 토큰은 필수입니다")
        String fcmToken,

        @Schema(description = "플랫폼", example = "ANDROID")
        @NotBlank(message = "플랫폼은 필수입니다")
        String platform
) {
}
