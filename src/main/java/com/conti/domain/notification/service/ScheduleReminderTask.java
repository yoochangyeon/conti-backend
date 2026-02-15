package com.conti.domain.notification.service;

import com.conti.domain.notification.entity.NotificationType;
import com.conti.domain.schedule.entity.ScheduleStatus;
import com.conti.domain.schedule.entity.ServiceSchedule;
import com.conti.domain.schedule.repository.ServiceScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleReminderTask {

    private final ServiceScheduleRepository serviceScheduleRepository;
    private final NotificationService notificationService;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("M/d");

    @Scheduled(cron = "0 0 9 * * *")
    @Transactional(readOnly = true)
    public void sendReminders() {
        LocalDate today = LocalDate.now();
        LocalDate d1 = today.plusDays(1);
        LocalDate d2 = today.plusDays(2);

        sendRemindersForDate(d1, "내일");
        sendRemindersForDate(d2, "모레");
    }

    private void sendRemindersForDate(LocalDate worshipDate, String dayLabel) {
        List<ServiceSchedule> schedules = serviceScheduleRepository.findUpcomingByWorshipDate(worshipDate);

        for (ServiceSchedule schedule : schedules) {
            if (schedule.getStatus() == ScheduleStatus.DECLINED) {
                continue;
            }

            Long userId = schedule.getTeamMember().getUser().getId();
            String title = "봉사 리마인더";
            String message = dayLabel + " ("
                    + worshipDate.format(DATE_FORMAT) + ") "
                    + schedule.getSetlist().getTitle() + " - "
                    + schedule.getPosition().getDisplayName();

            notificationService.createNotification(
                    userId,
                    NotificationType.SCHEDULE_REMINDER,
                    title,
                    message,
                    "SETLIST",
                    schedule.getSetlist().getId()
            );
        }

        if (!schedules.isEmpty()) {
            log.info("Sent {} reminder(s) for worship date {}", schedules.size(), worshipDate);
        }
    }
}
