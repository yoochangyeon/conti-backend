package com.conti.domain.song.dto;

import com.conti.domain.song.entity.SongFile;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "악보 파일 응답")
public record SongFileResponse(
        @Schema(description = "파일 ID", example = "1")
        Long id,
        @Schema(description = "파일 이름", example = "이 땅의 모든 찬양.pdf")
        String fileName,
        @Schema(description = "파일 URL")
        String fileUrl,
        @Schema(description = "파일 타입", example = "pdf")
        String fileType,
        @Schema(description = "파일 크기 (bytes)", example = "1024000")
        Long fileSize,
        @Schema(description = "생성 일시")
        LocalDateTime createdAt
) {

    public static SongFileResponse from(SongFile file) {
        return new SongFileResponse(
                file.getId(),
                file.getFileName(),
                file.getFileUrl(),
                file.getFileType(),
                file.getFileSize(),
                file.getCreatedAt()
        );
    }
}
