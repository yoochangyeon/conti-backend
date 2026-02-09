package com.conti.domain.setlist.entity;

import com.conti.domain.song.entity.Song;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "setlist_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SetlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setlist_id")
    private Setlist setlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id")
    private Song song;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "song_key", length = 10)
    private String songKey;

    @Column(columnDefinition = "TEXT")
    private String memo;

    public void updateSongKey(String songKey) {
        this.songKey = songKey;
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }

    public void updateOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}
