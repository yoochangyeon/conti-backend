package com.conti.domain.setlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "세트리스트 노트 생성 요청")
public record SetlistNoteCreateRequest(
        @Schema(description = "노트 내용", example = "인트로에서 어쿠스틱 기타 솔로")
        @NotBlank String content,
        @Schema(description = "포지션 (null이면 전체 공유)", example = "ACOUSTIC_GUITAR")
        String position
) {
}
