package com.conti.domain.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "팀 공지사항 수정 요청")
public record TeamNoticeUpdateRequest(
        @Schema(description = "공지 제목", example = "수정된 리허설 안내")
        @NotBlank String title,
        @Schema(description = "공지 내용", example = "일요일 오전 9시로 변경되었습니다.")
        String content
) {
}
