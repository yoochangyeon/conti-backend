package com.conti.domain.user.dto;

import com.conti.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 프로필 응답")
public record UserResponse(
        @Schema(description = "사용자 ID", example = "1")
        Long id,
        @Schema(description = "이메일", example = "user@example.com")
        String email,
        @Schema(description = "이름", example = "홍길동")
        String name,
        @Schema(description = "프로필 이미지 URL")
        String profileImage
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getProfileImage()
        );
    }
}
