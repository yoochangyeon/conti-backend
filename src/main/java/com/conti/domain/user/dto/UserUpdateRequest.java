package com.conti.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "프로필 수정 요청")
public record UserUpdateRequest(
        @Schema(description = "이름", example = "홍길동")
        @NotBlank String name,
        @Schema(description = "프로필 이미지 URL")
        String profileImage
) {
}
