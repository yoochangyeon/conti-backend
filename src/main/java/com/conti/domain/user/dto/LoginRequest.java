package com.conti.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "소셜 로그인 요청")
public record LoginRequest(
        @Schema(description = "OAuth 인가 코드", example = "4/0AX4XfWh...")
        @NotBlank String code
) {
}
