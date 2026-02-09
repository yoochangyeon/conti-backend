package com.conti.domain.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "팀 생성 요청")
public record TeamCreateRequest(
        @Schema(description = "팀 이름", example = "사랑의교회 찬양팀")
        @NotBlank String name,
        @Schema(description = "팀 설명", example = "주일예배 찬양팀입니다")
        String description
) {
}
