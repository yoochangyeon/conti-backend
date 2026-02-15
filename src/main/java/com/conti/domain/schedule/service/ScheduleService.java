package com.conti.domain.schedule.service;

import com.conti.domain.schedule.dto.ScheduleBulkCreateRequest;
import com.conti.domain.schedule.dto.ScheduleBulkResult;
import com.conti.domain.schedule.dto.ScheduleConflictResponse;
import com.conti.domain.schedule.dto.ScheduleCreateRequest;
import com.conti.domain.schedule.dto.ScheduleRespondRequest;
import com.conti.domain.schedule.dto.ScheduleMatrixResponse;
import com.conti.domain.schedule.dto.ScheduleSignupRequest;
import com.conti.domain.schedule.dto.ServiceScheduleResponse;
import com.conti.domain.schedule.entity.BlockoutDate;
import com.conti.domain.schedule.entity.ScheduleStatus;
import com.conti.domain.schedule.entity.ServiceSchedule;
import com.conti.domain.schedule.repository.BlockoutDateRepository;
import com.conti.domain.schedule.repository.ServiceScheduleRepository;
import com.conti.domain.setlist.entity.Setlist;
import com.conti.domain.setlist.repository.SetlistRepository;
import com.conti.domain.team.entity.MemberPosition;
import com.conti.domain.team.entity.Position;
import com.conti.domain.team.entity.TeamMember;
import com.conti.domain.team.repository.MemberPositionRepository;
import com.conti.domain.team.repository.TeamMemberRepository;
import com.conti.domain.notification.entity.NotificationType;
import com.conti.domain.notification.service.NotificationService;
import com.conti.global.error.BusinessException;
import com.conti.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private final ServiceScheduleRepository serviceScheduleRepository;
    private final SetlistRepository setlistRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final BlockoutDateRepository blockoutDateRepository;
    private final MemberPositionRepository memberPositionRepository;
    private final NotificationService notificationService;

    @Transactional
    public ScheduleBulkResult scheduleMembers(Long setlistId, ScheduleBulkCreateRequest request) {
        Setlist setlist = setlistRepository.findById(setlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETLIST_NOT_FOUND));

        List<ServiceScheduleResponse> created = new ArrayList<>();
        List<ScheduleConflictResponse> conflicts = new ArrayList<>();

        for (ScheduleCreateRequest scheduleRequest : request.schedules()) {
            TeamMember teamMember = teamMemberRepository.findById(scheduleRequest.teamMemberId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            // Check for duplicate
            if (serviceScheduleRepository.existsBySetlistIdAndTeamMemberIdAndPosition(
                    setlistId, scheduleRequest.teamMemberId(), scheduleRequest.position())) {
                throw new BusinessException(ErrorCode.DUPLICATE_SCHEDULE);
            }

            // Check for blockout conflicts
            List<BlockoutDate> blockouts = blockoutDateRepository
                    .findByTeamMemberIdAndDateOverlapping(
                            scheduleRequest.teamMemberId(), setlist.getWorshipDate());

            for (BlockoutDate blockout : blockouts) {
                conflicts.add(new ScheduleConflictResponse(
                        scheduleRequest.teamMemberId(),
                        teamMember.getUser().getName(),
                        scheduleRequest.position().name(),
                        "BLOCKOUT",
                        blockout.getStartDate() + " ~ " + blockout.getEndDate()
                                + (blockout.getReason() != null ? " " + blockout.getReason() : "")
                ));
            }

            // Still create the schedule (admin override)
            ServiceSchedule schedule = ServiceSchedule.builder()
                    .setlist(setlist)
                    .teamMember(teamMember)
                    .position(scheduleRequest.position())
                    .build();

            ServiceSchedule saved = serviceScheduleRepository.save(schedule);
            created.add(ServiceScheduleResponse.from(saved));

            // Notify assigned member
            notificationService.createNotification(
                    teamMember.getUser().getId(),
                    NotificationType.SCHEDULE_ASSIGNED,
                    "봉사 배정 알림",
                    setlist.getTitle() + " - " + scheduleRequest.position().getDisplayName() + "으로 배정되었습니다",
                    "SETLIST",
                    setlistId
            );
        }

        return new ScheduleBulkResult(created, conflicts);
    }

    @Transactional
    public ServiceScheduleResponse respond(Long scheduleId, Long userId, ScheduleRespondRequest request) {
        ServiceSchedule schedule = serviceScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));

        // Only assigned member can respond
        if (!schedule.getTeamMember().getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // Only PENDING can be responded to
        if (schedule.getStatus() != ScheduleStatus.PENDING) {
            throw new BusinessException(ErrorCode.SCHEDULE_ALREADY_RESPONDED);
        }

        if (request.accept()) {
            schedule.accept();
        } else {
            if (request.declinedReason() == null || request.declinedReason().isBlank()) {
                throw new BusinessException(ErrorCode.DECLINE_REASON_REQUIRED);
            }
            schedule.decline(request.declinedReason());
        }

        // Notify setlist creator about the response
        String memberName = schedule.getTeamMember().getUser().getName();
        String statusText = request.accept() ? "수락" : "거절";
        notificationService.createNotification(
                schedule.getSetlist().getCreatorId(),
                NotificationType.SCHEDULE_RESPONSE,
                "배정 응답 알림",
                memberName + "님이 " + schedule.getPosition().getDisplayName() + " 배정을 " + statusText + "했습니다",
                "SETLIST",
                schedule.getSetlist().getId()
        );

        return ServiceScheduleResponse.from(schedule);
    }

    public List<ServiceScheduleResponse> getSchedulesForSetlist(Long setlistId) {
        return serviceScheduleRepository.findBySetlistIdWithMember(setlistId).stream()
                .map(ServiceScheduleResponse::from)
                .toList();
    }

    public List<ServiceScheduleResponse> getMySchedules(Long teamId, Long userId) {
        TeamMember teamMember = teamMemberRepository.findByUserIdAndTeamId(userId, teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return serviceScheduleRepository.findUpcomingByTeamMemberId(teamMember.getId()).stream()
                .map(ServiceScheduleResponse::from)
                .toList();
    }

    @Transactional
    public ServiceScheduleResponse signup(Long setlistId, Long userId, ScheduleSignupRequest request) {
        Setlist setlist = setlistRepository.findById(setlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETLIST_NOT_FOUND));

        TeamMember teamMember = teamMemberRepository.findByUserIdAndTeamId(userId, setlist.getTeam().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Check user has position qualification
        boolean hasPosition = memberPositionRepository.findByTeamMemberId(teamMember.getId()).stream()
                .anyMatch(mp -> mp.getPosition() == request.position());
        if (!hasPosition) {
            throw new BusinessException(ErrorCode.POSITION_NOT_QUALIFIED);
        }

        // Check for duplicate
        if (serviceScheduleRepository.existsBySetlistIdAndTeamMemberIdAndPosition(
                setlistId, teamMember.getId(), request.position())) {
            throw new BusinessException(ErrorCode.DUPLICATE_SCHEDULE);
        }

        ServiceSchedule schedule = ServiceSchedule.builder()
                .setlist(setlist)
                .teamMember(teamMember)
                .position(request.position())
                .build();

        // Auto-accept since user is signing up themselves
        schedule.accept();

        ServiceSchedule saved = serviceScheduleRepository.save(schedule);
        return ServiceScheduleResponse.from(saved);
    }

    public ScheduleMatrixResponse getScheduleMatrix(Long teamId, LocalDate fromDate, LocalDate toDate) {
        List<ServiceSchedule> schedules = serviceScheduleRepository
                .findByTeamIdAndDateRange(teamId, fromDate, toDate);

        // Collect unique dates (sorted)
        Set<LocalDate> dateSet = new LinkedHashSet<>();
        // Collect unique positions that appear in the data
        Set<Position> positionSet = new LinkedHashSet<>();
        // Map date -> setlist info
        Map<LocalDate, ServiceSchedule> dateSetlistMap = new LinkedHashMap<>();

        for (ServiceSchedule s : schedules) {
            LocalDate date = s.getSetlist().getWorshipDate();
            dateSet.add(date);
            positionSet.add(s.getPosition());
            dateSetlistMap.putIfAbsent(date, s);
        }

        List<LocalDate> dates = dateSet.stream().sorted().toList();

        // Use all Position enum values so the matrix always shows consistent rows
        List<Position> positions = Arrays.asList(Position.values());

        List<String> positionNames = positions.stream().map(Position::name).toList();
        List<String> positionDisplayNames = positions.stream().map(Position::getDisplayName).toList();

        // Build date setlist info
        List<ScheduleMatrixResponse.DateSetlistInfo> dateSetlists = dates.stream()
                .map(date -> {
                    ServiceSchedule ss = dateSetlistMap.get(date);
                    return new ScheduleMatrixResponse.DateSetlistInfo(
                            date,
                            ss.getSetlist().getId(),
                            ss.getSetlist().getWorshipType() != null ? ss.getSetlist().getWorshipType().name() : null,
                            ss.getSetlist().getWorshipType() != null ? ss.getSetlist().getWorshipType().getDisplayName() : null,
                            ss.getSetlist().getTitle()
                    );
                })
                .toList();

        // Group schedules: (date, position) -> list of members
        Map<String, List<ScheduleMatrixResponse.CellMember>> cellMap = schedules.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getSetlist().getWorshipDate() + "|" + s.getPosition().name(),
                        Collectors.mapping(
                                s -> new ScheduleMatrixResponse.CellMember(
                                        s.getId(),
                                        s.getTeamMember().getId(),
                                        s.getTeamMember().getUser().getName(),
                                        s.getTeamMember().getUser().getProfileImage(),
                                        s.getStatus().name(),
                                        s.getStatus().getDisplayName()
                                ),
                                Collectors.toList()
                        )
                ));

        // Build cells for all date x position combinations
        List<ScheduleMatrixResponse.MatrixCell> cells = new ArrayList<>();
        for (LocalDate date : dates) {
            for (Position pos : positions) {
                String key = date + "|" + pos.name();
                List<ScheduleMatrixResponse.CellMember> members = cellMap.getOrDefault(key, List.of());
                cells.add(new ScheduleMatrixResponse.MatrixCell(date, pos.name(), members));
            }
        }

        return new ScheduleMatrixResponse(positionNames, positionDisplayNames, dates, dateSetlists, cells);
    }

    @Transactional
    public void removeSchedule(Long scheduleId) {
        ServiceSchedule schedule = serviceScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));

        serviceScheduleRepository.delete(schedule);
    }
}
