package com.conti.domain.setlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "세트리스트 노트 수정 요청")
public record SetlistNoteUpdateRequest(
        @Schema(description = "노트 내용", example = "수정된 인트로 기타 솔로 노트")
        @NotBlank String content,
        @Schema(description = "포지션 (null이면 전체 공유)", example = "ACOUSTIC_GUITAR")
        String position
) {
}
