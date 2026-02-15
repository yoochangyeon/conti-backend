package com.conti.domain.song.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "편곡 수정 요청")
public record ArrangementUpdateRequest(
        @Schema(description = "편곡 이름") String name,
        @Schema(description = "키") String songKey,
        @Schema(description = "BPM") Integer bpm,
        @Schema(description = "박자") String meter,
        @Schema(description = "소요 시간 (분)") Integer durationMinutes,
        @Schema(description = "설명") String description,
        @Schema(description = "섹션 목록 (전체 교체)") List<SongSectionRequest> sections
) {}
