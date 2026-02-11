package com.conti.domain.song.entity;

import com.conti.domain.team.entity.Team;
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
@Table(name = "songs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Song extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 100)
    private String artist;

    @Column(name = "original_key", length = 10)
    private String originalKey;

    private Integer bpm;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(name = "youtube_url", length = 500)
    private String youtubeUrl;

    @Column(name = "music_url", length = 500)
    private String musicUrl;

    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SongTag> songTags = new ArrayList<>();

    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SongFile> songFiles = new ArrayList<>();

    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SongSection> songSections = new ArrayList<>();

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateArtist(String artist) {
        this.artist = artist;
    }

    public void updateOriginalKey(String originalKey) {
        this.originalKey = originalKey;
    }

    public void updateBpm(Integer bpm) {
        this.bpm = bpm;
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }

    public void updateYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    public void updateMusicUrl(String musicUrl) {
        this.musicUrl = musicUrl;
    }
}
