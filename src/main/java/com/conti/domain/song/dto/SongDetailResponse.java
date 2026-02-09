package com.conti.domain.song.dto;

import com.conti.domain.song.entity.Song;
import com.conti.domain.song.entity.SongTag;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "찬양 상세 응답")
public record SongDetailResponse(
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
        LocalDateTime createdAt,
        @Schema(description = "메모")
        String memo,
        @Schema(description = "유튜브 URL")
        String youtubeUrl,
        @Schema(description = "음원 URL")
        String musicUrl,
        @Schema(description = "악보 파일 목록")
        List<SongFileResponse> files,
        @Schema(description = "콘티 사용 횟수", example = "5")
        long usageCount
) {

    public static SongDetailResponse from(Song song, long usageCount) {
        List<String> tagNames = song.getSongTags().stream()
                .map(SongTag::getTag)
                .toList();

        List<SongFileResponse> fileResponses = song.getSongFiles().stream()
                .map(SongFileResponse::from)
                .toList();

        return new SongDetailResponse(
                song.getId(),
                song.getTitle(),
                song.getArtist(),
                song.getOriginalKey(),
                song.getBpm(),
                tagNames,
                song.getCreatedAt(),
                song.getMemo(),
                song.getYoutubeUrl(),
                song.getMusicUrl(),
                fileResponses,
                usageCount
        );
    }
}
