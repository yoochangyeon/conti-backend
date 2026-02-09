package com.conti.domain.song.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Schema(description = "찬양 생성 요청")
public record SongCreateRequest(
        @Schema(description = "찬양 제목", example = "이 땅의 모든 찬양")
        @NotBlank String title,
        @Schema(description = "아티스트/작곡가", example = "마커스워십")
        String artist,
        @Schema(description = "원키", example = "G")
        String originalKey,
        @Schema(description = "BPM", example = "120")
        Integer bpm,
        @Schema(description = "메모")
        String memo,
        @Schema(description = "유튜브 URL", example = "https://youtube.com/watch?v=...")
        String youtubeUrl,
        @Schema(description = "음원 URL")
        String musicUrl,
        @Schema(description = "태그 목록", example = "[\"찬양\", \"경배\"]")
        List<String> tags
) {
}
