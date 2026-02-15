package com.conti.domain.setlist.service;

import com.conti.domain.setlist.dto.ReorderRequest;
import com.conti.domain.setlist.dto.SetlistCopyRequest;
import com.conti.domain.setlist.dto.SetlistCreateRequest;
import com.conti.domain.setlist.dto.SetlistDetailResponse;
import com.conti.domain.setlist.dto.SetlistItemRequest;
import com.conti.domain.setlist.dto.SetlistItemResponse;
import com.conti.domain.setlist.dto.SetlistResponse;
import com.conti.domain.setlist.dto.SetlistSearchCondition;
import com.conti.domain.setlist.dto.SetlistUpdateRequest;
import com.conti.domain.setlist.entity.Setlist;
import com.conti.domain.setlist.entity.SetlistItem;
import com.conti.domain.setlist.entity.SetlistItemType;
import com.conti.domain.setlist.entity.SetlistTemplate;
import com.conti.domain.setlist.entity.SetlistTemplateItem;
import com.conti.domain.setlist.repository.SetlistItemRepository;
import com.conti.domain.setlist.repository.SetlistRepository;
import com.conti.domain.setlist.repository.SetlistTemplateRepository;
import com.conti.domain.notification.entity.NotificationType;
import com.conti.domain.notification.service.NotificationService;
import com.conti.domain.schedule.entity.ServiceSchedule;
import com.conti.domain.schedule.repository.ServiceScheduleRepository;
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
    private final SetlistTemplateRepository setlistTemplateRepository;
    private final ServiceScheduleRepository serviceScheduleRepository;
    private final NotificationService notificationService;

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

    @Transactional
    public SetlistResponse createSetlistFromTemplate(Long teamId, Long userId, SetlistCreateRequest request, Long templateId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

        SetlistTemplate template = setlistTemplateRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND));

        Setlist setlist = Setlist.builder()
                .team(team)
                .creatorId(userId)
                .title(request.title())
                .worshipDate(request.worshipDate())
                .worshipType(request.worshipType() != null ? request.worshipType() : template.getWorshipType())
                .leaderId(request.leaderId())
                .memo(request.memo())
                .build();

        Setlist savedSetlist = setlistRepository.save(setlist);

        for (SetlistTemplateItem templateItem : template.getItems()) {
            SetlistItem item = SetlistItem.builder()
                    .setlist(savedSetlist)
                    .itemType(templateItem.getItemType())
                    .title(templateItem.getTitle())
                    .song(templateItem.getSong())
                    .orderIndex(templateItem.getOrderIndex())
                    .durationMinutes(templateItem.getDurationMinutes())
                    .color(templateItem.getColor())
                    .servicePhase(templateItem.getServicePhase())
                    .build();
            savedSetlist.getSetlistItems().add(item);
        }

        setlistRepository.flush();
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

        // Notify scheduled members about setlist update
        notifyScheduledMembers(setlist, "콘티가 수정되었습니다: " + setlist.getTitle());

        return SetlistResponse.from(setlist);
    }

    @Transactional
    public void deleteSetlist(Long setlistId) {
        Setlist setlist = setlistRepository.findById(setlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETLIST_NOT_FOUND));

        setlistRepository.delete(setlist);
    }

    @Transactional
    public SetlistResponse copySetlist(Long setlistId, Long userId, SetlistCopyRequest request) {
        Setlist source = setlistRepository.findById(setlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETLIST_NOT_FOUND));

        Setlist copy = Setlist.builder()
                .team(source.getTeam())
                .creatorId(userId)
                .title(request.title())
                .worshipDate(request.worshipDate())
                .worshipType(request.worshipType() != null ? request.worshipType() : source.getWorshipType())
                .memo(source.getMemo())
                .build();

        Setlist savedCopy = setlistRepository.save(copy);

        int index = 0;
        for (SetlistItem sourceItem : source.getSetlistItems()) {
            SetlistItem copiedItem = SetlistItem.builder()
                    .setlist(savedCopy)
                    .itemType(sourceItem.getItemType())
                    .title(sourceItem.getTitle())
                    .song(sourceItem.getSong())
                    .orderIndex(index++)
                    .songKey(sourceItem.getSongKey())
                    .durationMinutes(sourceItem.getDurationMinutes())
                    .memo(sourceItem.getMemo())
                    .color(sourceItem.getColor())
                    .servicePhase(sourceItem.getServicePhase())
                    .build();
            savedCopy.getSetlistItems().add(copiedItem);
        }

        setlistRepository.flush();
        return SetlistResponse.from(savedCopy);
    }

    @Transactional
    public SetlistItemResponse addItem(Long setlistId, SetlistItemRequest request) {
        Setlist setlist = setlistRepository.findById(setlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETLIST_NOT_FOUND));

        SetlistItemType itemType = request.itemType() != null ? request.itemType() : SetlistItemType.SONG;
        int orderIndex = setlist.getSetlistItems().size();

        Song song = null;
        if (itemType == SetlistItemType.SONG) {
            if (request.songId() == null) {
                throw new BusinessException(ErrorCode.SONG_ID_REQUIRED);
            }
            song = songRepository.findById(request.songId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.SONG_NOT_FOUND));
        } else if (itemType != SetlistItemType.HEADER) {
            if (request.title() == null || request.title().isBlank()) {
                throw new BusinessException(ErrorCode.ITEM_TITLE_REQUIRED);
            }
        }

        SetlistItem item = SetlistItem.builder()
                .setlist(setlist)
                .itemType(itemType)
                .title(request.title())
                .song(song)
                .orderIndex(orderIndex)
                .songKey(request.songKey())
                .durationMinutes(request.durationMinutes())
                .memo(request.memo())
                .color(request.color())
                .servicePhase(request.servicePhase())
                .build();

        setlist.getSetlistItems().add(item);
        setlistRepository.flush();

        // Create SongUsage record only for SONG items
        if (itemType == SetlistItemType.SONG) {
            SongUsage songUsage = SongUsage.builder()
                    .song(song)
                    .setlist(setlist)
                    .usedKey(request.songKey())
                    .usedAt(setlist.getWorshipDate())
                    .build();
            songUsageRepository.save(songUsage);
        }

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
        if (request.title() != null) {
            item.updateTitle(request.title());
        }
        if (request.durationMinutes() != null) {
            item.updateDurationMinutes(request.durationMinutes());
        }
        if (request.color() != null) {
            item.updateColor(request.color());
        }
        if (request.servicePhase() != null) {
            item.updateServicePhase(request.servicePhase());
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

    private void notifyScheduledMembers(Setlist setlist, String message) {
        List<ServiceSchedule> schedules = serviceScheduleRepository
                .findBySetlistIdWithMember(setlist.getId());

        for (ServiceSchedule schedule : schedules) {
            Long userId = schedule.getTeamMember().getUser().getId();
            notificationService.createNotification(
                    userId,
                    NotificationType.SETLIST_UPDATED,
                    "콘티 수정 알림",
                    message,
                    "SETLIST",
                    setlist.getId()
            );
        }
    }
}
