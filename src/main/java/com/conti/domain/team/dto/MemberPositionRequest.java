package com.conti.domain.team.dto;

import com.conti.domain.team.entity.Position;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "멤버 포지션 설정 요청")
public record MemberPositionRequest(
        @Schema(description = "포지션", example = "VOCAL")
        @NotNull Position position,
        @Schema(description = "주 포지션 여부", example = "true")
        boolean isPrimary
) {
}
