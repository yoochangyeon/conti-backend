package com.conti.domain.setlist.service;

import com.conti.domain.setlist.dto.SetlistTemplateCreateRequest;
import com.conti.domain.setlist.dto.SetlistTemplateItemRequest;
import com.conti.domain.setlist.dto.SetlistTemplateResponse;
import com.conti.domain.setlist.entity.SetlistItemType;
import com.conti.domain.setlist.entity.SetlistTemplate;
import com.conti.domain.setlist.entity.SetlistTemplateItem;
import com.conti.domain.setlist.repository.SetlistTemplateRepository;
import com.conti.domain.song.entity.Song;
import com.conti.domain.song.repository.SongRepository;
import com.conti.domain.team.entity.Team;
import com.conti.domain.team.repository.TeamRepository;
import com.conti.global.error.BusinessException;
import com.conti.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SetlistTemplateService {

    private final SetlistTemplateRepository templateRepository;
    private final TeamRepository teamRepository;
    private final SongRepository songRepository;

    public List<SetlistTemplateResponse> getTemplates(Long teamId) {
        return templateRepository.findByTeamIdOrderByCreatedAtDesc(teamId).stream()
                .map(SetlistTemplateResponse::from)
                .toList();
    }

    public SetlistTemplateResponse getTemplate(Long templateId) {
        SetlistTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND));
        return SetlistTemplateResponse.from(template);
    }

    @Transactional
    public SetlistTemplateResponse createTemplate(Long teamId, SetlistTemplateCreateRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

        SetlistTemplate template = SetlistTemplate.builder()
                .team(team)
                .name(request.name())
                .description(request.description())
                .worshipType(request.worshipType())
                .build();

        SetlistTemplate savedTemplate = templateRepository.save(template);

        if (request.items() != null) {
            for (int i = 0; i < request.items().size(); i++) {
                SetlistTemplateItemRequest itemReq = request.items().get(i);
                SetlistItemType itemType = itemReq.itemType() != null ? itemReq.itemType() : SetlistItemType.SONG;

                Song song = null;
                if (itemType == SetlistItemType.SONG && itemReq.songId() != null) {
                    song = songRepository.findById(itemReq.songId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.SONG_NOT_FOUND));
                }

                SetlistTemplateItem item = SetlistTemplateItem.builder()
                        .template(savedTemplate)
                        .itemType(itemType)
                        .orderIndex(i)
                        .song(song)
                        .title(itemReq.title())
                        .description(itemReq.description())
                        .durationMinutes(itemReq.durationMinutes())
                        .color(itemReq.color())
                        .servicePhase(itemReq.servicePhase())
                        .build();
                savedTemplate.getItems().add(item);
            }
            templateRepository.flush();
        }

        return SetlistTemplateResponse.from(savedTemplate);
    }

    @Transactional
    public SetlistTemplateResponse updateTemplate(Long templateId, SetlistTemplateCreateRequest request) {
        SetlistTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND));

        if (request.name() != null) {
            template.updateName(request.name());
        }
        if (request.description() != null) {
            template.updateDescription(request.description());
        }
        if (request.worshipType() != null) {
            template.updateWorshipType(request.worshipType());
        }

        if (request.items() != null) {
            template.getItems().clear();
            templateRepository.flush();

            for (int i = 0; i < request.items().size(); i++) {
                SetlistTemplateItemRequest itemReq = request.items().get(i);
                SetlistItemType itemType = itemReq.itemType() != null ? itemReq.itemType() : SetlistItemType.SONG;

                Song song = null;
                if (itemType == SetlistItemType.SONG && itemReq.songId() != null) {
                    song = songRepository.findById(itemReq.songId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.SONG_NOT_FOUND));
                }

                SetlistTemplateItem item = SetlistTemplateItem.builder()
                        .template(template)
                        .itemType(itemType)
                        .orderIndex(i)
                        .song(song)
                        .title(itemReq.title())
                        .description(itemReq.description())
                        .durationMinutes(itemReq.durationMinutes())
                        .color(itemReq.color())
                        .servicePhase(itemReq.servicePhase())
                        .build();
                template.getItems().add(item);
            }
        }

        return SetlistTemplateResponse.from(template);
    }

    @Transactional
    public void deleteTemplate(Long templateId) {
        SetlistTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND));
        templateRepository.delete(template);
    }
}
