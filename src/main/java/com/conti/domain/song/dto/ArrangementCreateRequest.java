package com.conti.domain.song.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Schema(description = "편곡 생성 요청")
public record ArrangementCreateRequest(
        @Schema(description = "편곡 이름", example = "어쿠스틱 버전")
        @NotBlank String name,
        @Schema(description = "키", example = "G")
        String songKey,
        @Schema(description = "BPM", example = "120")
        Integer bpm,
        @Schema(description = "박자", example = "4/4")
        String meter,
        @Schema(description = "소요 시간 (분)", example = "5")
        Integer durationMinutes,
        @Schema(description = "설명")
        String description,
        @Schema(description = "섹션 목록")
        List<SongSectionRequest> sections
) {}
