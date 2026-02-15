package com.conti.domain.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Enum 값 응답")
public record EnumResponse(
        @Schema(description = "Enum 이름", example = "VOCAL")
        String name,
        @Schema(description = "표시명", example = "보컬")
        String displayName
) {
}
