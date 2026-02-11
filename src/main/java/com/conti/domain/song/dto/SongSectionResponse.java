package com.conti.domain.song.dto;

import com.conti.domain.song.entity.SongSection;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "곡 섹션 응답")
public record SongSectionResponse(
        @Schema(description = "섹션 ID", example = "1")
        Long id,
        @Schema(description = "섹션 타입", example = "CHORUS")
        String sectionType,
        @Schema(description = "순서", example = "0")
        Integer orderIndex,
        @Schema(description = "라벨", example = "1절")
        String label,
        @Schema(description = "코드 진행", example = "G - D - Em - C")
        String chords,
        @Schema(description = "빌드업 레벨 (1-5)", example = "3")
        Integer buildUpLevel,
        @Schema(description = "메모")
        String memo
) {

    public static SongSectionResponse from(SongSection section) {
        return new SongSectionResponse(
                section.getId(),
                section.getSectionType().name(),
                section.getOrderIndex(),
                section.getLabel(),
                section.getChords(),
                section.getBuildUpLevel(),
                section.getMemo()
        );
    }
}
