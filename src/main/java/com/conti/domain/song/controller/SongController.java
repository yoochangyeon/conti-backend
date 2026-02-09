package com.conti.domain.song.controller;

import com.conti.domain.song.dto.SongCreateRequest;
import com.conti.domain.song.dto.SongDetailResponse;
import com.conti.domain.song.dto.SongFileResponse;
import com.conti.domain.song.dto.SongResponse;
import com.conti.domain.song.dto.SongSearchCondition;
import com.conti.domain.song.dto.SongUpdateRequest;
import com.conti.domain.song.dto.SongUsageResponse;
import com.conti.domain.song.service.SongService;
import com.conti.global.auth.TeamAuth;
import com.conti.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "찬양", description = "찬양 곡 관리")
@RestController
@RequestMapping("/api/v1/teams/{teamId}/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    @Operation(summary = "찬양 목록 조회", description = "검색 조건으로 필터링 가능")
    @TeamAuth(roles = {"ADMIN", "VIEWER", "GUEST"})
    @GetMapping
    public ApiResponse<Page<SongResponse>> getSongs(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "검색 키워드 (제목/아티스트)") @RequestParam(required = false) String keyword,
            @Parameter(description = "태그 필터") @RequestParam(required = false) List<String> tags,
            @Parameter(description = "원키 필터") @RequestParam(required = false) String key,
            @Parameter(description = "미사용 주수 필터") @RequestParam(required = false) Integer unusedWeeks,
            @Parameter(description = "인도자 ID 필터") @RequestParam(required = false) Long leaderId,
            Pageable pageable
    ) {
        SongSearchCondition condition = new SongSearchCondition(keyword, tags, key, unusedWeeks, leaderId);
        return ApiResponse.ok(songService.getSongs(teamId, condition, pageable));
    }

    @Operation(summary = "찬양 추가")
    @TeamAuth(roles = {"ADMIN"})
    @PostMapping
    public ApiResponse<SongResponse> createSong(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Valid @RequestBody SongCreateRequest request
    ) {
        return ApiResponse.ok(songService.createSong(teamId, request));
    }

    @Operation(summary = "찬양 상세 조회")
    @TeamAuth(roles = {"ADMIN", "VIEWER", "GUEST"})
    @GetMapping("/{songId}")
    public ApiResponse<SongDetailResponse> getSong(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "곡 ID") @PathVariable Long songId
    ) {
        return ApiResponse.ok(songService.getSong(teamId, songId));
    }

    @Operation(summary = "찬양 수정")
    @TeamAuth(roles = {"ADMIN"})
    @PatchMapping("/{songId}")
    public ApiResponse<SongResponse> updateSong(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "곡 ID") @PathVariable Long songId,
            @RequestBody SongUpdateRequest request
    ) {
        return ApiResponse.ok(songService.updateSong(teamId, songId, request));
    }

    @Operation(summary = "찬양 삭제")
    @TeamAuth(roles = {"ADMIN"})
    @DeleteMapping("/{songId}")
    public ApiResponse<Void> deleteSong(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "곡 ID") @PathVariable Long songId
    ) {
        songService.deleteSong(teamId, songId);
        return ApiResponse.ok();
    }

    @Operation(summary = "찬양 사용 이력 조회")
    @TeamAuth(roles = {"ADMIN", "VIEWER", "GUEST"})
    @GetMapping("/{songId}/usages")
    public ApiResponse<List<SongUsageResponse>> getUsages(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "곡 ID") @PathVariable Long songId
    ) {
        return ApiResponse.ok(songService.getSongUsages(songId));
    }

    @Operation(summary = "악보 파일 업로드")
    @TeamAuth(roles = {"ADMIN"})
    @PostMapping(value = "/{songId}/files", consumes = "multipart/form-data")
    public ApiResponse<SongFileResponse> uploadFile(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "곡 ID") @PathVariable Long songId,
            @Parameter(description = "업로드할 파일") @RequestParam("file") MultipartFile file,
            @Parameter(description = "파일 타입") @RequestParam(required = false) String fileType
    ) {
        return ApiResponse.ok(songService.uploadFileWithS3(songId, file, fileType));
    }

    @Operation(summary = "악보 파일 URL 등록")
    @TeamAuth(roles = {"ADMIN"})
    @PostMapping("/{songId}/files/url")
    public ApiResponse<SongFileResponse> uploadFileByUrl(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "곡 ID") @PathVariable Long songId,
            @Parameter(description = "파일 이름") @RequestParam String fileName,
            @Parameter(description = "파일 URL") @RequestParam String fileUrl,
            @Parameter(description = "파일 타입") @RequestParam(required = false) String fileType,
            @Parameter(description = "파일 크기 (bytes)") @RequestParam(required = false) Long fileSize
    ) {
        return ApiResponse.ok(songService.uploadFile(songId, fileName, fileUrl, fileType, fileSize));
    }

    @Operation(summary = "악보 파일 삭제")
    @TeamAuth(roles = {"ADMIN"})
    @DeleteMapping("/{songId}/files/{fileId}")
    public ApiResponse<Void> deleteFile(
            @Parameter(description = "팀 ID") @PathVariable Long teamId,
            @Parameter(description = "곡 ID") @PathVariable Long songId,
            @Parameter(description = "파일 ID") @PathVariable Long fileId
    ) {
        songService.deleteFile(songId, fileId);
        return ApiResponse.ok();
    }
}
