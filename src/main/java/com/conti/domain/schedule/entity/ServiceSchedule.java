package com.conti.domain.schedule.entity;

import com.conti.domain.setlist.entity.Setlist;
import com.conti.domain.team.entity.Position;
import com.conti.domain.team.entity.TeamMember;
import com.conti.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "service_schedules",
        uniqueConstraints = @UniqueConstraint(columnNames = {"setlist_id", "team_member_id", "position"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceSchedule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setlist_id")
    private Setlist setlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_member_id")
    private TeamMember teamMember;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Position position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ScheduleStatus status = ScheduleStatus.PENDING;

    @Column(name = "declined_reason", length = 200)
    private String declinedReason;

    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    public void accept() {
        this.status = ScheduleStatus.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
    }

    public void decline(String reason) {
        this.status = ScheduleStatus.DECLINED;
        this.declinedReason = reason;
        this.respondedAt = LocalDateTime.now();
    }

    public void markNotified() {
        this.notifiedAt = LocalDateTime.now();
    }
}
