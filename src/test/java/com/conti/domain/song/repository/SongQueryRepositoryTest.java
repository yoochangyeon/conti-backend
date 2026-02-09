package com.conti.domain.song.repository;

import com.conti.domain.song.dto.SongSearchCondition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SongQueryRepositoryTest {

    @Nested
    @DisplayName("SongSearchCondition")
    class SongSearchConditionTest {

        @Test
        @DisplayName("모든 필드가 null인 빈 조건을 생성한다")
        void emptyCondition() {
            // given & when
            SongSearchCondition condition = new SongSearchCondition(null, null, null, null, null);

            // then
            assertThat(condition.keyword()).isNull();
            assertThat(condition.tags()).isNull();
            assertThat(condition.key()).isNull();
            assertThat(condition.unusedWeeks()).isNull();
            assertThat(condition.leaderId()).isNull();
        }

        @Test
        @DisplayName("키워드 조건을 생성한다")
        void keywordCondition() {
            // given & when
            SongSearchCondition condition = new SongSearchCondition("찬양", null, null, null, null);

            // then
            assertThat(condition.keyword()).isEqualTo("찬양");
            assertThat(condition.tags()).isNull();
        }

        @Test
        @DisplayName("태그 조건을 생성한다")
        void tagsCondition() {
            // given
            List<String> tags = List.of("경배", "감사");

            // when
            SongSearchCondition condition = new SongSearchCondition(null, tags, null, null, null);

            // then
            assertThat(condition.tags()).containsExactly("경배", "감사");
        }

        @Test
        @DisplayName("빈 태그 리스트 조건을 생성한다")
        void emptyTagsCondition() {
            // given & when
            SongSearchCondition condition = new SongSearchCondition(null, Collections.emptyList(), null, null, null);

            // then
            assertThat(condition.tags()).isEmpty();
        }

        @Test
        @DisplayName("키 조건을 생성한다")
        void keyCondition() {
            // given & when
            SongSearchCondition condition = new SongSearchCondition(null, null, "G", null, null);

            // then
            assertThat(condition.key()).isEqualTo("G");
        }

        @Test
        @DisplayName("미사용 주수 조건을 생성한다")
        void unusedWeeksCondition() {
            // given & when
            SongSearchCondition condition = new SongSearchCondition(null, null, null, 4, null);

            // then
            assertThat(condition.unusedWeeks()).isEqualTo(4);
        }

        @Test
        @DisplayName("인도자 ID 조건을 생성한다")
        void leaderIdCondition() {
            // given & when
            SongSearchCondition condition = new SongSearchCondition(null, null, null, null, 1L);

            // then
            assertThat(condition.leaderId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("복합 조건을 생성한다")
        void multipleConditions() {
            // given
            List<String> tags = List.of("경배");

            // when
            SongSearchCondition condition = new SongSearchCondition("마커스", tags, "G", 4, 1L);

            // then
            assertThat(condition.keyword()).isEqualTo("마커스");
            assertThat(condition.tags()).containsExactly("경배");
            assertThat(condition.key()).isEqualTo("G");
            assertThat(condition.unusedWeeks()).isEqualTo(4);
            assertThat(condition.leaderId()).isEqualTo(1L);
        }
    }
}
