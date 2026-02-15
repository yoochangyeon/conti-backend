package com.conti.domain.song.service;

import com.conti.domain.setlist.repository.SetlistRepository;
import com.conti.domain.song.dto.SongCreateRequest;
import com.conti.domain.song.dto.SongDetailResponse;
import com.conti.domain.song.dto.SongFileResponse;
import com.conti.domain.song.dto.SongResponse;
import com.conti.domain.song.dto.SongSearchCondition;
import com.conti.domain.song.dto.SongSectionRequest;
import com.conti.domain.song.dto.SongSectionResponse;
import com.conti.domain.song.dto.SongStatsResponse;
import com.conti.domain.song.dto.SongUpdateRequest;
import com.conti.domain.song.dto.SongUsageResponse;
import com.conti.domain.song.dto.TagResponse;
import com.conti.domain.song.dto.TopSongResponse;
import com.conti.domain.song.entity.SectionType;
import com.conti.domain.song.entity.Song;
import com.conti.domain.song.entity.SongFile;
import com.conti.domain.song.entity.SongSection;
import com.conti.domain.song.entity.SongTag;
import com.conti.domain.song.entity.SongUsage;
import com.conti.domain.song.repository.SongFileRepository;
import com.conti.domain.song.repository.SongRepository;
import com.conti.domain.song.repository.SongSectionRepository;
import com.conti.domain.song.repository.SongTagRepository;
import com.conti.domain.song.repository.SongUsageRepository;
import com.conti.domain.team.entity.Team;
import com.conti.domain.team.repository.TeamRepository;
import com.conti.domain.user.entity.User;
import com.conti.domain.user.repository.UserRepository;
import com.conti.global.error.BusinessException;
import com.conti.global.error.ErrorCode;
import com.conti.infra.s3.S3FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SongService {

    private final SongRepository songRepository;
    private final SongTagRepository songTagRepository;
    private final SongFileRepository songFileRepository;
    private final SongSectionRepository songSectionRepository;
    private final SongUsageRepository songUsageRepository;
    private final TeamRepository teamRepository;
    private final SetlistRepository setlistRepository;
    private final UserRepository userRepository;
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

        if (request.sections() != null) {
            for (SongSectionRequest sectionReq : request.sections()) {
                SongSection section = SongSection.builder()
                        .song(song)
                        .sectionType(SectionType.valueOf(sectionReq.sectionType()))
                        .orderIndex(sectionReq.orderIndex())
                        .label(sectionReq.label())
                        .chords(sectionReq.chords())
                        .buildUpLevel(sectionReq.buildUpLevel())
                        .memo(sectionReq.memo())
                        .build();
                song.getSongSections().add(section);
            }
        }

        Song savedSong = songRepository.save(song);
        return SongResponse.from(savedSong);
    }

    public SongDetailResponse getSong(Long teamId, Long songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SONG_NOT_FOUND));

        long usageCount = songUsageRepository.countBySongId(songId);
        LocalDate lastUsedAt = songUsageRepository.findLastUsedAt(songId);

        return SongDetailResponse.from(song, usageCount, lastUsedAt);
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
        if (request.sections() != null) {
            song.getSongSections().clear();
            for (SongSectionRequest sectionReq : request.sections()) {
                SongSection section = SongSection.builder()
                        .song(song)
                        .sectionType(SectionType.valueOf(sectionReq.sectionType()))
                        .orderIndex(sectionReq.orderIndex())
                        .label(sectionReq.label())
                        .chords(sectionReq.chords())
                        .buildUpLevel(sectionReq.buildUpLevel())
                        .memo(sectionReq.memo())
                        .build();
                song.getSongSections().add(section);
            }
        }

        return SongResponse.from(song);
    }

    @Transactional
    public List<SongSectionResponse> updateSections(Long teamId, Long songId, List<SongSectionRequest> requests) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SONG_NOT_FOUND));

        song.getSongSections().clear();
        songRepository.flush();

        for (SongSectionRequest sectionReq : requests) {
            SongSection section = SongSection.builder()
                    .song(song)
                    .sectionType(SectionType.valueOf(sectionReq.sectionType()))
                    .orderIndex(sectionReq.orderIndex())
                    .label(sectionReq.label())
                    .chords(sectionReq.chords())
                    .buildUpLevel(sectionReq.buildUpLevel())
                    .memo(sectionReq.memo())
                    .build();
            song.getSongSections().add(section);
        }

        songRepository.flush();

        return song.getSongSections().stream()
                .map(SongSectionResponse::from)
                .toList();
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

    public List<TopSongResponse> getTopSongs(Long teamId, LocalDate fromDate, LocalDate toDate, int limit) {
        return songRepository.findTopSongs(teamId, fromDate, toDate, limit);
    }

    public SongStatsResponse getSongStats(Long teamId, Long songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SONG_NOT_FOUND));

        long totalCount = songUsageRepository.countBySongId(songId);
        LocalDate lastUsedAt = songUsageRepository.findLastUsedAt(songId);

        List<SongStatsResponse.MonthlyUsage> monthlyUsages = songUsageRepository.findMonthlyUsage(songId)
                .stream()
                .map(row -> new SongStatsResponse.MonthlyUsage(
                        ((Number) row[0]).intValue(),
                        ((Number) row[1]).intValue(),
                        ((Number) row[2]).longValue()))
                .toList();

        List<SongStatsResponse.KeyUsage> keyDistribution = songUsageRepository.findKeyDistribution(songId)
                .stream()
                .map(row -> new SongStatsResponse.KeyUsage(
                        (String) row[0],
                        ((Number) row[1]).longValue()))
                .toList();

        List<SongStatsResponse.LeaderUsage> leaderBreakdown = songUsageRepository.findLeaderBreakdown(songId)
                .stream()
                .map(row -> {
                    Long leaderId = ((Number) row[0]).longValue();
                    String leaderName = userRepository.findById(leaderId)
                            .map(User::getName)
                            .orElse("Unknown");
                    return new SongStatsResponse.LeaderUsage(leaderId, leaderName, ((Number) row[1]).longValue());
                })
                .toList();

        return new SongStatsResponse(
                song.getId(),
                song.getTitle(),
                song.getArtist(),
                totalCount,
                lastUsedAt,
                monthlyUsages,
                keyDistribution,
                leaderBreakdown
        );
    }

    public List<TagResponse> getTeamTags(Long teamId) {
        return songTagRepository.findTagsWithCountByTeamId(teamId);
    }
}
