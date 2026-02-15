package com.conti.domain.team.service;

import com.conti.domain.team.dto.TeamNoticeCreateRequest;
import com.conti.domain.team.dto.TeamNoticeResponse;
import com.conti.domain.team.dto.TeamNoticeUpdateRequest;
import com.conti.domain.team.entity.TeamNotice;
import com.conti.domain.team.repository.TeamNoticeRepository;
import com.conti.domain.team.repository.TeamRepository;
import com.conti.domain.user.entity.User;
import com.conti.domain.user.repository.UserRepository;
import com.conti.global.error.BusinessException;
import com.conti.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamNoticeService {

    private final TeamNoticeRepository teamNoticeRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TeamNoticeResponse> getNotices(Long teamId) {
        teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

        return teamNoticeRepository.findByTeamIdOrderByIsPinnedDescCreatedAtDesc(teamId)
                .stream()
                .map(notice -> {
                    String authorName = userRepository.findById(notice.getAuthorId())
                            .map(User::getName)
                            .orElse("알 수 없음");
                    return TeamNoticeResponse.from(notice, authorName);
                })
                .toList();
    }

    @Transactional
    public TeamNoticeResponse createNotice(Long teamId, Long userId, TeamNoticeCreateRequest request) {
        teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        TeamNotice notice = TeamNotice.builder()
                .teamId(teamId)
                .authorId(userId)
                .title(request.title())
                .content(request.content())
                .build();
        teamNoticeRepository.save(notice);

        return TeamNoticeResponse.from(notice, author.getName());
    }

    @Transactional
    public TeamNoticeResponse updateNotice(Long teamId, Long noticeId, TeamNoticeUpdateRequest request) {
        teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

        TeamNotice notice = teamNoticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));

        notice.update(request.title(), request.content());

        String authorName = userRepository.findById(notice.getAuthorId())
                .map(User::getName)
                .orElse("알 수 없음");

        return TeamNoticeResponse.from(notice, authorName);
    }

    @Transactional
    public void deleteNotice(Long teamId, Long noticeId) {
        teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

        TeamNotice notice = teamNoticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));

        teamNoticeRepository.delete(notice);
    }

    @Transactional
    public TeamNoticeResponse togglePin(Long teamId, Long noticeId) {
        teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

        TeamNotice notice = teamNoticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));

        notice.togglePin();

        String authorName = userRepository.findById(notice.getAuthorId())
                .map(User::getName)
                .orElse("알 수 없음");

        return TeamNoticeResponse.from(notice, authorName);
    }
}
