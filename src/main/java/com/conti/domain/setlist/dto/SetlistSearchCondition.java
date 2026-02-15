package com.conti.domain.setlist.dto;

import com.conti.domain.setlist.entity.WorshipType;

import java.time.LocalDate;

public record SetlistSearchCondition(
        LocalDate fromDate,
        LocalDate toDate,
        WorshipType worshipType
) {
}
