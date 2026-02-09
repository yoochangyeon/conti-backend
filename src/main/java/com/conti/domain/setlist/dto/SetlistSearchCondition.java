package com.conti.domain.setlist.dto;

import java.time.LocalDate;

public record SetlistSearchCondition(
        LocalDate fromDate,
        LocalDate toDate,
        String worshipType
) {
}
