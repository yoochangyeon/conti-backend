package com.conti.domain.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "팀 공지사항 생성 요청")
public record TeamNoticeCreateRequest(
        @Schema(description = "공지 제목", example = "이번 주 리허설 안내")
        @NotBlank String title,
        @Schema(description = "공지 내용", example = "토요일 오후 3시에 리허설이 있습니다.")
        String content
) {
}
