package com.conti.domain.setlist.entity;

import com.conti.domain.schedule.entity.ServiceSchedule;
import com.conti.domain.team.entity.Team;
import com.conti.global.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "setlists")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Setlist extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(length = 200)
    private String title;

    @Column(name = "worship_date", nullable = false)
    private LocalDate worshipDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "worship_type", length = 50)
    private WorshipType worshipType;

    @Column(name = "leader_id")
    private Long leaderId;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @OneToMany(mappedBy = "setlist", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SetlistItem> setlistItems = new ArrayList<>();

    @OneToMany(mappedBy = "setlist", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ServiceSchedule> schedules = new ArrayList<>();

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateWorshipDate(LocalDate worshipDate) {
        this.worshipDate = worshipDate;
    }

    public void updateWorshipType(WorshipType worshipType) {
        this.worshipType = worshipType;
    }

    public void updateLeaderId(Long leaderId) {
        this.leaderId = leaderId;
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }
}
