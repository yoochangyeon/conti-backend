package com.conti.domain.setlist.repository;

import com.conti.domain.setlist.dto.SetlistSearchCondition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class SetlistQueryRepositoryTest {

    @Nested
    @DisplayName("SetlistSearchCondition")
    class SetlistSearchConditionTest {

        @Test
        @DisplayName("모든 필드가 null인 빈 조건을 생성한다")
        void emptyCondition() {
            // given & when
            SetlistSearchCondition condition = new SetlistSearchCondition(null, null, null);

            // then
            assertThat(condition.fromDate()).isNull();
            assertThat(condition.toDate()).isNull();
            assertThat(condition.worshipType()).isNull();
        }

        @Test
        @DisplayName("시작 날짜 조건을 생성한다")
        void fromDateCondition() {
            // given
            LocalDate fromDate = LocalDate.of(2026, 1, 1);

            // when
            SetlistSearchCondition condition = new SetlistSearchCondition(fromDate, null, null);

            // then
            assertThat(condition.fromDate()).isEqualTo(LocalDate.of(2026, 1, 1));
            assertThat(condition.toDate()).isNull();
            assertThat(condition.worshipType()).isNull();
        }

        @Test
        @DisplayName("종료 날짜 조건을 생성한다")
        void toDateCondition() {
            // given
            LocalDate toDate = LocalDate.of(2026, 12, 31);

            // when
            SetlistSearchCondition condition = new SetlistSearchCondition(null, toDate, null);

            // then
            assertThat(condition.fromDate()).isNull();
            assertThat(condition.toDate()).isEqualTo(LocalDate.of(2026, 12, 31));
        }

        @Test
        @DisplayName("예배 유형 조건을 생성한다")
        void worshipTypeCondition() {
            // given & when
            SetlistSearchCondition condition = new SetlistSearchCondition(null, null, "주일1부");

            // then
            assertThat(condition.worshipType()).isEqualTo("주일1부");
        }

        @Test
        @DisplayName("날짜 범위 조건을 생성한다")
        void dateRangeCondition() {
            // given
            LocalDate fromDate = LocalDate.of(2026, 2, 1);
            LocalDate toDate = LocalDate.of(2026, 2, 28);

            // when
            SetlistSearchCondition condition = new SetlistSearchCondition(fromDate, toDate, null);

            // then
            assertThat(condition.fromDate()).isEqualTo(LocalDate.of(2026, 2, 1));
            assertThat(condition.toDate()).isEqualTo(LocalDate.of(2026, 2, 28));
        }

        @Test
        @DisplayName("복합 조건을 생성한다")
        void multipleConditions() {
            // given
            LocalDate fromDate = LocalDate.of(2026, 2, 1);
            LocalDate toDate = LocalDate.of(2026, 2, 28);

            // when
            SetlistSearchCondition condition = new SetlistSearchCondition(fromDate, toDate, "주일2부");

            // then
            assertThat(condition.fromDate()).isEqualTo(LocalDate.of(2026, 2, 1));
            assertThat(condition.toDate()).isEqualTo(LocalDate.of(2026, 2, 28));
            assertThat(condition.worshipType()).isEqualTo("주일2부");
        }
    }
}
