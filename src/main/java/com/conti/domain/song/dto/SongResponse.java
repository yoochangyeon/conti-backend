package com.conti.domain.song.dto;

import com.conti.domain.song.entity.Song;
import com.conti.domain.song.entity.SongTag;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "찬양 응답")
public record SongResponse(
        @Schema(description = "곡 ID", example = "1")
        Long id,
        @Schema(description = "찬양 제목", example = "이 땅의 모든 찬양")
        String title,
        @Schema(description = "아티스트/작곡가", example = "마커스워십")
        String artist,
        @Schema(description = "원키", example = "G")
        String originalKey,
        @Schema(description = "BPM", example = "120")
        Integer bpm,
        @Schema(description = "태그 목록")
        List<String> tags,
        @Schema(description = "생성 일시")
        LocalDateTime createdAt
) {

    public static SongResponse from(Song song) {
        List<String> tagNames = song.getSongTags().stream()
                .map(SongTag::getTag)
                .toList();

        return new SongResponse(
                song.getId(),
                song.getTitle(),
                song.getArtist(),
                song.getOriginalKey(),
                song.getBpm(),
                tagNames,
                song.getCreatedAt()
        );
    }
}
