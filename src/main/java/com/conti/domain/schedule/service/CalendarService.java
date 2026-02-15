package com.conti.domain.schedule.service;

import com.conti.domain.schedule.entity.ServiceSchedule;
import com.conti.domain.schedule.repository.ServiceScheduleRepository;
import com.conti.domain.team.entity.TeamMember;
import com.conti.domain.team.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

    private final ServiceScheduleRepository serviceScheduleRepository;
    private final TeamMemberRepository teamMemberRepository;

    private static final DateTimeFormatter ICAL_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    public String generateICalFeed(Long userId) {
        List<TeamMember> teamMembers = teamMemberRepository.findByUserId(userId);

        List<ServiceSchedule> allSchedules = teamMembers.stream()
                .flatMap(tm -> serviceScheduleRepository.findAllByTeamMemberId(tm.getId()).stream())
                .toList();

        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:-//Conti//Schedule//KO\r\n");
        sb.append("CALSCALE:GREGORIAN\r\n");
        sb.append("METHOD:PUBLISH\r\n");
        sb.append("X-WR-CALNAME:Conti Schedule\r\n");
        sb.append("X-WR-TIMEZONE:Asia/Seoul\r\n");

        for (ServiceSchedule schedule : allSchedules) {
            LocalDate worshipDate = schedule.getSetlist().getWorshipDate();
            String teamName = schedule.getSetlist().getTeam().getName();
            String worshipType = schedule.getSetlist().getWorshipType() != null
                    ? schedule.getSetlist().getWorshipType().getDisplayName()
                    : "";
            String positionName = schedule.getPosition().getDisplayName();
            String statusName = schedule.getStatus().getDisplayName();

            String summary = String.format("[%s] %s - %s", teamName, worshipType, positionName);
            String description = String.format("Position: %s\\nStatus: %s", positionName, statusName);

            if (schedule.getSetlist().getTitle() != null) {
                description += "\\nSetlist: " + schedule.getSetlist().getTitle();
            }

            sb.append("BEGIN:VEVENT\r\n");
            sb.append("UID:").append(schedule.getId()).append("@conti.app\r\n");
            sb.append("DTSTART;VALUE=DATE:").append(worshipDate.format(ICAL_DATE)).append("\r\n");
            sb.append("DTEND;VALUE=DATE:").append(worshipDate.plusDays(1).format(ICAL_DATE)).append("\r\n");
            sb.append("SUMMARY:").append(escapeICalText(summary)).append("\r\n");
            sb.append("DESCRIPTION:").append(escapeICalText(description)).append("\r\n");
            sb.append("STATUS:").append(mapStatus(schedule.getStatus().name())).append("\r\n");
            sb.append("DTSTAMP:").append(formatInstant(Instant.now())).append("\r\n");
            sb.append("END:VEVENT\r\n");
        }

        sb.append("END:VCALENDAR\r\n");
        return sb.toString();
    }

    private String mapStatus(String scheduleStatus) {
        return switch (scheduleStatus) {
            case "ACCEPTED" -> "CONFIRMED";
            case "DECLINED" -> "CANCELLED";
            default -> "TENTATIVE";
        };
    }

    private String escapeICalText(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace(",", "\\,")
                .replace(";", "\\;")
                .replace("\n", "\\n");
    }

    private String formatInstant(Instant instant) {
        return DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                .withZone(java.time.ZoneOffset.UTC)
                .format(instant);
    }
}
