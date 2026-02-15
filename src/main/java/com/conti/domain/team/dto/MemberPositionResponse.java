package com.conti.domain.team.dto;

import com.conti.domain.team.entity.MemberPosition;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "멤버 포지션 응답")
public record MemberPositionResponse(
        @Schema(description = "포지션 ID", example = "1")
        Long id,
        @Schema(description = "포지션", example = "VOCAL")
        String position,
        @Schema(description = "포지션 표시명", example = "보컬")
        String displayName,
        @Schema(description = "주 포지션 여부", example = "true")
        boolean isPrimary
) {

    public static MemberPositionResponse from(MemberPosition memberPosition) {
        return new MemberPositionResponse(
                memberPosition.getId(),
                memberPosition.getPosition().name(),
                memberPosition.getPosition().getDisplayName(),
                memberPosition.isPrimary()
        );
    }
}
