package com.conti.domain.setlist.dto;

import com.conti.domain.setlist.entity.WorshipType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Schema(description = "콘티 템플릿 생성 요청")
public record SetlistTemplateCreateRequest(
        @Schema(description = "템플릿 이름", example = "주일 1부 기본 콘티")
        @NotBlank String name,
        @Schema(description = "설명")
        String description,
        @Schema(description = "예배 타입", example = "SUNDAY_1ST")
        WorshipType worshipType,
        @Schema(description = "템플릿 항목 목록")
        List<SetlistTemplateItemRequest> items
) {
}
