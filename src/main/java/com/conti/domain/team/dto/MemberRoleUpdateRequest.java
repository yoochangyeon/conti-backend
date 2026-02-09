package com.conti.domain.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "멤버 역할 변경 요청")
public record MemberRoleUpdateRequest(
        @Schema(description = "변경할 역할 (ADMIN, VIEWER)", example = "VIEWER")
        @NotBlank String role
) {
}
