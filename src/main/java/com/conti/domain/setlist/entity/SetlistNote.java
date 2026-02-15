package com.conti.domain.setlist.entity;

import com.conti.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "setlist_notes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SetlistNote extends BaseEntity {

    @Column(name = "setlist_id", nullable = false)
    private Long setlistId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(length = 50)
    private String position;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    public void update(String content, String position) {
        this.content = content;
        this.position = position;
    }
}
