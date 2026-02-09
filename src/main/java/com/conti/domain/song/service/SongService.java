package com.conti.domain.song.service;

import com.conti.domain.setlist.repository.SetlistRepository;
import com.conti.domain.song.dto.SongCreateRequest;
import com.conti.domain.song.dto.SongDetailResponse;
import com.conti.domain.song.dto.SongFileResponse;
import com.conti.domain.song.dto.SongResponse;
import com.conti.domain.song.dto.SongSearchCondition;
import com.conti.domain.song.dto.SongUpdateRequest;
import com.conti.domain.song.dto.SongUsageResponse;
import com.conti.domain.song.dto.TagResponse;
import com.conti.domain.song.entity.Song;
import com.conti.domain.song.entity.SongFile;
import com.conti.domain.song.entity.SongTag;
import com.conti.domain.song.entity.SongUsage;
import com.conti.domain.song.repository.SongFileRepository;
import com.conti.domain.song.repository.SongRepository;
import com.conti.domain.song.repository.SongTagRepository;
import com.conti.domain.song.repository.SongUsageRepository;
import com.conti.domain.team.entity.Team;
import com.conti.domain.team.repository.TeamRepository;
import com.conti.global.error.BusinessException;
import com.conti.global.error.ErrorCode;
import com.conti.infra.s3.S3FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SongService {

    private final SongRepository songRepository;
    private final SongTagRepository songTagRepository;
    private final SongFileRepository songFileRepository;
    private final SongUsageRepository songUsageRepository;
    private final TeamRepository teamRepository;
    private final SetlistRepository setlistRepository;
    private final S3FileService s3FileService;

    public Page<SongResponse> getSongs(Long teamId, SongSearchCondition condition, Pageable pageable) {
        return songRepository.searchSongs(teamId, condition, pageable)
                .map(SongResponse::from);
    }

    @Transactional
    public SongResponse createSong(Long teamId, SongCreateRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

        Song song = Song.builder()
                .team(team)
                .title(request.title())
                .artist(request.artist())
                .originalKey(request.originalKey())
                .bpm(request.bpm())
                .memo(request.memo())
                .youtubeUrl(request.youtubeUrl())
                .musicUrl(request.musicUrl())
                .build();

        if (request.tags() != null) {
            for (String tagName : request.tags()) {
                SongTag songTag = SongTag.builder()
                        .song(song)
                        .tag(tagName)
                        .build();
                song.getSongTags().add(songTag);
            }
        }

        Song savedSong = songRepository.save(song);
        return SongResponse.from(savedSong);
    }

    public SongDetailResponse getSong(Long teamId, Long songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SONG_NOT_FOUND));

        long usageCount = songUsageRepository.findBySongId(songId).size();

        return SongDetailResponse.from(song, usageCount);
    }

    @Transactional
    public SongResponse updateSong(Long teamId, Long songId, SongUpdateRequest request) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SONG_NOT_FOUND));

        if (request.title() != null) {
            song.updateTitle(request.title());
        }
        if (request.artist() != null) {
            song.updateArtist(request.artist());
        }
        if (request.originalKey() != null) {
            song.updateOriginalKey(request.originalKey());
        }
        if (request.bpm() != null) {
            song.updateBpm(request.bpm());
        }
        if (request.memo() != null) {
            song.updateMemo(request.memo());
        }
        if (request.youtubeUrl() != null) {
            song.updateYoutubeUrl(request.youtubeUrl());
        }
        if (request.musicUrl() != null) {
            song.updateMusicUrl(request.musicUrl());
        }
        if (request.tags() != null) {
            song.getSongTags().clear();
            for (String tagName : request.tags()) {
                SongTag songTag = SongTag.builder()
                        .song(song)
                        .tag(tagName)
                        .build();
                song.getSongTags().add(songTag);
            }
        }

        return SongResponse.from(song);
    }

    @Transactional
    public void deleteSong(Long teamId, Long songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SONG_NOT_FOUND));

        songRepository.delete(song);
    }

    public List<SongUsageResponse> getSongUsages(Long songId) {
        List<SongUsage> usages = songUsageRepository.findBySongId(songId);

        return usages.stream()
                .map(SongUsageResponse::from)
                .toList();
    }

    @Transactional
    public SongFileResponse uploadFile(Long songId, String fileName, String fileUrl, String fileType, Long fileSize) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SONG_NOT_FOUND));

        SongFile songFile = SongFile.builder()
                .song(song)
                .fileName(fileName)
                .fileUrl(fileUrl)
                .fileType(fileType)
                .fileSize(fileSize)
                .build();

        song.getSongFiles().add(songFile);
        songRepository.flush();

        return SongFileResponse.from(songFile);
    }

    @Transactional
    public SongFileResponse uploadFileWithS3(Long songId, MultipartFile file, String fileType) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SONG_NOT_FOUND));

        String fileUrl = s3FileService.uploadFile(file, "songs/" + songId);
        String fileName = file.getOriginalFilename();
        Long fileSize = file.getSize();

        SongFile songFile = SongFile.builder()
                .song(song)
                .fileName(fileName)
                .fileUrl(fileUrl)
                .fileType(fileType)
                .fileSize(fileSize)
                .build();

        song.getSongFiles().add(songFile);
        songRepository.flush();

        return SongFileResponse.from(songFile);
    }

    @Transactional
    public void deleteFile(Long songId, Long fileId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SONG_NOT_FOUND));

        boolean removed = song.getSongFiles().removeIf(file -> file.getId().equals(fileId));
        if (!removed) {
            throw new BusinessException(ErrorCode.SONG_NOT_FOUND);
        }
    }

    public List<TagResponse> getTeamTags(Long teamId) {
        return songTagRepository.findTagsWithCountByTeamId(teamId);
    }
}
