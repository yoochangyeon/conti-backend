package com.conti.domain.setlist.entity;

import com.conti.domain.song.entity.Song;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "setlist_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class SetlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setlist_id")
    private Setlist setlist;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 30)
    @Builder.Default
    private SetlistItemType itemType = SetlistItemType.SONG;

    @Column(length = 200)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id")
    private Song song;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "song_key", length = 10)
    private String songKey;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(length = 20)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_phase", length = 20)
    private ServicePhase servicePhase;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void updateSongKey(String songKey) {
        this.songKey = songKey;
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }

    public void updateOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public void updateColor(String color) {
        this.color = color;
    }

    public void updateServicePhase(ServicePhase servicePhase) {
        this.servicePhase = servicePhase;
    }

    public String getDisplayTitle() {
        if (itemType == SetlistItemType.SONG && song != null) {
            return song.getTitle();
        }
        return title;
    }
}
