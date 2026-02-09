package com.conti.domain.song.dto;

import java.util.List;

public record SongSearchCondition(
        String keyword,
        List<String> tags,
        String key,
        Integer unusedWeeks,
        Long leaderId
) {
}
