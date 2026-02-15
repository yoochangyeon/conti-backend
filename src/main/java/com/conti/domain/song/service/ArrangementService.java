package com.conti.domain.song.service;

import com.conti.domain.song.dto.ArrangementCreateRequest;
import com.conti.domain.song.dto.ArrangementResponse;
import com.conti.domain.song.dto.ArrangementUpdateRequest;
import com.conti.domain.song.dto.SongSectionRequest;
import com.conti.domain.song.entity.SectionType;
import com.conti.domain.song.entity.Song;
import com.conti.domain.song.entity.SongArrangement;
import com.conti.domain.song.entity.SongSection;
import com.conti.domain.song.repository.SongArrangementRepository;
import com.conti.domain.song.repository.SongRepository;
import com.conti.global.error.BusinessException;
import com.conti.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArrangementService {

    private final SongRepository songRepository;
    private final SongArrangementRepository arrangementRepository;

    public List<ArrangementResponse> getArrangements(Long teamId, Long songId) {
        return arrangementRepository.findBySongIdOrderByIsDefaultDescCreatedAtAsc(songId)
                .stream()
                .map(ArrangementResponse::from)
                .toList();
    }

    public ArrangementResponse getArrangement(Long teamId, Long songId, Long arrangementId) {
        SongArrangement arrangement = arrangementRepository.findById(arrangementId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SONG_NOT_FOUND));
        return ArrangementResponse.from(arrangement);
    }

    @Transactional
    public ArrangementResponse createArrangement(Long teamId, Long songId, ArrangementCreateRequest request) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SONG_NOT_FOUND));

        SongArrangement arrangement = SongArrangement.builder()
                .song(song)
                .name(request.name())
                .songKey(request.songKey())
                .bpm(request.bpm())
                .meter(request.meter())
                .durationMinutes(request.durationMinutes())
                .description(request.description())
                .isDefault(false)
                .build();

        if (request.sections() != null) {
            for (SongSectionRequest sectionReq : request.sections()) {
                SongSection section = SongSection.builder()
                        .song(song)
                        .arrangement(arrangement)
                        .sectionType(SectionType.valueOf(sectionReq.sectionType()))
                        .orderIndex(sectionReq.orderIndex())
                        .label(sectionReq.label())
                        .chords(sectionReq.chords())
                        .buildUpLevel(sectionReq.buildUpLevel())
                        .memo(sectionReq.memo())
                        .build();
                arrangement.getSections().add(section);
            }
        }

        SongArrangement saved = arrangementRepository.save(arrangement);
        return ArrangementResponse.from(saved);
    }

    @Transactional
    public ArrangementResponse updateArrangement(Long teamId, Long songId, Long arrangementId,
                                                  ArrangementUpdateRequest request) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SONG_NOT_FOUND));

        SongArrangement arrangement = arrangementRepository.findById(arrangementId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SONG_NOT_FOUND));

        if (request.name() != null) {
            arrangement.updateName(request.name());
        }
        if (request.songKey() != null) {
            arrangement.updateSongKey(request.songKey());
        }
        if (request.bpm() != null) {
            arrangement.updateBpm(request.bpm());
        }
        if (request.meter() != null) {
            arrangement.updateMeter(request.meter());
        }
        if (request.durationMinutes() != null) {
            arrangement.updateDurationMinutes(request.durationMinutes());
        }
        if (request.description() != null) {
            arrangement.updateDescription(request.description());
        }

        if (request.sections() != null) {
            arrangement.getSections().clear();
            arrangementRepository.flush();

            for (SongSectionRequest sectionReq : request.sections()) {
                SongSection section = SongSection.builder()
                        .song(song)
                        .arrangement(arrangement)
                        .sectionType(SectionType.valueOf(sectionReq.sectionType()))
                        .orderIndex(sectionReq.orderIndex())
                        .label(sectionReq.label())
                        .chords(sectionReq.chords())
                        .buildUpLevel(sectionReq.buildUpLevel())
                        .memo(sectionReq.memo())
                        .build();
                arrangement.getSections().add(section);
            }
        }

        return ArrangementResponse.from(arrangement);
    }

    @Transactional
    public void deleteArrangement(Long teamId, Long songId, Long arrangementId) {
        SongArrangement arrangement = arrangementRepository.findById(arrangementId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SONG_NOT_FOUND));

        if (arrangement.getIsDefault()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        arrangementRepository.delete(arrangement);
    }
}
