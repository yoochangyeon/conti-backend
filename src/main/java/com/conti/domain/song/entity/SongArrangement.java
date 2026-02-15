package com.conti.domain.song.entity;

import com.conti.global.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "song_arrangements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SongArrangement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id")
    private Song song;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "song_key", length = 10)
    private String songKey;

    private Integer bpm;

    @Column(length = 10)
    private String meter;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @OneToMany(mappedBy = "arrangement", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SongSection> sections = new ArrayList<>();

    public void updateName(String name) {
        this.name = name;
    }

    public void updateSongKey(String songKey) {
        this.songKey = songKey;
    }

    public void updateBpm(Integer bpm) {
        this.bpm = bpm;
    }

    public void updateMeter(String meter) {
        this.meter = meter;
    }

    public void updateDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
}
