package com.conti.domain.setlist.service;

import com.conti.domain.setlist.dto.ReorderRequest;
import com.conti.domain.setlist.dto.SetlistCreateRequest;
import com.conti.domain.setlist.dto.SetlistDetailResponse;
import com.conti.domain.setlist.dto.SetlistItemRequest;
import com.conti.domain.setlist.dto.SetlistItemResponse;
import com.conti.domain.setlist.dto.SetlistResponse;
import com.conti.domain.setlist.dto.SetlistSearchCondition;
import com.conti.domain.setlist.dto.SetlistUpdateRequest;
import com.conti.domain.setlist.entity.Setlist;
import com.conti.domain.setlist.entity.SetlistItem;
import com.conti.domain.setlist.repository.SetlistItemRepository;
import com.conti.domain.setlist.repository.SetlistRepository;
import com.conti.domain.song.entity.Song;
import com.conti.domain.song.entity.SongUsage;
import com.conti.domain.song.repository.SongRepository;
import com.conti.domain.song.repository.SongUsageRepository;
import com.conti.domain.team.entity.Team;
import com.conti.domain.team.repository.TeamRepository;
import com.conti.global.error.BusinessException;
import com.conti.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SetlistService {

    private final SetlistRepository setlistRepository;
    private final SetlistItemRepository setlistItemRepository;
    private final SongRepository songRepository;
    private final SongUsageRepository songUsageRepository;
    private final TeamRepository teamRepository;

    public Page<SetlistResponse> getSetlists(Long teamId, SetlistSearchCondition condition, Pageable pageable) {
        return setlistRepository.searchSetlists(teamId, condition, pageable)
                .map(SetlistResponse::from);
    }

    @Transactional
    public SetlistResponse createSetlist(Long teamId, Long userId, SetlistCreateRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

        Setlist setlist = Setlist.builder()
                .team(team)
                .creatorId(userId)
                .title(request.title())
                .worshipDate(request.worshipDate())
                .worshipType(request.worshipType())
                .leaderId(request.leaderId())
                .memo(request.memo())
                .build();

        Setlist savedSetlist = setlistRepository.save(setlist);
        return SetlistResponse.from(savedSetlist);
    }

    public SetlistDetailResponse getSetlist(Long setlistId) {
        Setlist setlist = setlistRepository.findById(setlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETLIST_NOT_FOUND));

        return SetlistDetailResponse.from(setlist);
    }

    @Transactional
    public SetlistResponse updateSetlist(Long setlistId, SetlistUpdateRequest request) {
        Setlist setlist = setlistRepository.findById(setlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETLIST_NOT_FOUND));

        if (request.title() != null) {
            setlist.updateTitle(request.title());
        }
        if (request.worshipDate() != null) {
            setlist.updateWorshipDate(request.worshipDate());
        }
        if (request.worshipType() != null) {
            setlist.updateWorshipType(request.worshipType());
        }
        if (request.leaderId() != null) {
            setlist.updateLeaderId(request.leaderId());
        }
        if (request.memo() != null) {
            setlist.updateMemo(request.memo());
        }

        return SetlistResponse.from(setlist);
    }

    @Transactional
    public void deleteSetlist(Long setlistId) {
        Setlist setlist = setlistRepository.findById(setlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETLIST_NOT_FOUND));

        setlistRepository.delete(setlist);
    }

    @Transactional
    public SetlistItemResponse addItem(Long setlistId, SetlistItemRequest request) {
        Setlist setlist = setlistRepository.findById(setlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETLIST_NOT_FOUND));

        Song song = songRepository.findById(request.songId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SONG_NOT_FOUND));

        int orderIndex = setlist.getSetlistItems().size();

        SetlistItem item = SetlistItem.builder()
                .setlist(setlist)
                .song(song)
                .orderIndex(orderIndex)
                .songKey(request.songKey())
                .memo(request.memo())
                .build();

        setlist.getSetlistItems().add(item);
        setlistRepository.flush();

        // Create SongUsage record
        SongUsage songUsage = SongUsage.builder()
                .song(song)
                .setlist(setlist)
                .usedKey(request.songKey())
                .usedAt(setlist.getWorshipDate())
                .build();
        songUsageRepository.save(songUsage);

        return SetlistItemResponse.from(item);
    }

    @Transactional
    public SetlistItemResponse updateItem(Long setlistId, Long itemId, SetlistItemRequest request) {
        SetlistItem item = setlistItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETLIST_NOT_FOUND));

        if (request.songKey() != null) {
            item.updateSongKey(request.songKey());
        }
        if (request.memo() != null) {
            item.updateMemo(request.memo());
        }

        return SetlistItemResponse.from(item);
    }

    @Transactional
    public void removeItem(Long setlistId, Long itemId) {
        SetlistItem item = setlistItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETLIST_NOT_FOUND));

        setlistItemRepository.delete(item);
    }

    @Transactional
    public void reorderItems(Long setlistId, ReorderRequest request) {
        List<Long> itemIds = request.itemIds();

        for (int i = 0; i < itemIds.size(); i++) {
            SetlistItem item = setlistItemRepository.findById(itemIds.get(i))
                    .orElseThrow(() -> new BusinessException(ErrorCode.SETLIST_NOT_FOUND));
            item.updateOrderIndex(i);
        }
    }
}
