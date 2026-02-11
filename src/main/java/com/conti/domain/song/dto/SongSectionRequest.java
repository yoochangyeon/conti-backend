package com.conti.domain.song.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "곡 섹션 요청")
public record SongSectionRequest(
        @Schema(description = "섹션 타입", example = "CHORUS")
        @NotBlank String sectionType,
        @Schema(description = "순서", example = "0")
        @NotNull Integer orderIndex,
        @Schema(description = "라벨", example = "1절")
        String label,
        @Schema(description = "코드 진행", example = "G - D - Em - C")
        String chords,
        @Schema(description = "빌드업 레벨 (1-5)", example = "3")
        @Min(1) @Max(5) Integer buildUpLevel,
        @Schema(description = "메모")
        String memo
) {
}
