package com.conti.domain.song.repository;

import com.conti.domain.song.dto.SongSearchCondition;
import com.conti.domain.song.dto.TopSongResponse;
import com.conti.domain.song.entity.Song;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static com.conti.domain.song.entity.QSong.song;
import static com.conti.domain.song.entity.QSongTag.songTag;
import static com.conti.domain.song.entity.QSongUsage.songUsage;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SongQueryRepositoryImpl implements SongQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Song> searchSongs(Long teamId, SongSearchCondition condition, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(song.team.id.eq(teamId));

        builder.and(keywordContains(condition.keyword()));
        builder.and(originalKeyEquals(condition.key()));
        builder.and(tagsIn(condition.tags()));
        builder.and(unusedWithinWeeks(condition.unusedWeeks()));
        builder.and(leaderIdEquals(condition.leaderId()));

        List<Song> content = queryFactory
                .selectFrom(song)
                .leftJoin(song.songTags, songTag).fetchJoin()
                .where(builder)
                .orderBy(song.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .distinct()
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(song.countDistinct())
                .from(song)
                .where(builder);

        // Join songTag in count query only when tags filter is active
        if (condition.tags() != null && !condition.tags().isEmpty()) {
            countQuery.leftJoin(song.songTags, songTag);
        }

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return song.title.containsIgnoreCase(keyword)
                .or(song.artist.containsIgnoreCase(keyword));
    }

    private BooleanExpression originalKeyEquals(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        return song.originalKey.eq(key);
    }

    private BooleanExpression tagsIn(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return song.id.in(
                JPAExpressions
                        .select(songTag.song.id)
                        .from(songTag)
                        .where(songTag.tag.in(tags))
        );
    }

    private BooleanExpression unusedWithinWeeks(Integer unusedWeeks) {
        if (unusedWeeks == null) {
            return null;
        }
        LocalDate sinceDate = LocalDate.now().minusWeeks(unusedWeeks);
        return song.id.notIn(
                JPAExpressions
                        .select(songUsage.song.id)
                        .from(songUsage)
                        .where(songUsage.usedAt.goe(sinceDate))
        );
    }

    private BooleanExpression leaderIdEquals(Long leaderId) {
        if (leaderId == null) {
            return null;
        }
        return song.id.in(
                JPAExpressions
                        .select(songUsage.song.id)
                        .from(songUsage)
                        .where(songUsage.leaderId.eq(leaderId))
        );
    }

    @Override
    public List<TopSongResponse> findTopSongs(Long teamId, LocalDate fromDate, LocalDate toDate, int limit) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(song.team.id.eq(teamId));
        if (fromDate != null) {
            builder.and(songUsage.usedAt.goe(fromDate));
        }
        if (toDate != null) {
            builder.and(songUsage.usedAt.loe(toDate));
        }

        return queryFactory
                .select(Projections.constructor(TopSongResponse.class,
                        song.id,
                        song.title,
                        song.artist,
                        song.originalKey,
                        songUsage.count(),
                        songUsage.usedAt.max()
                ))
                .from(songUsage)
                .join(songUsage.song, song)
                .where(builder)
                .groupBy(song.id, song.title, song.artist, song.originalKey)
                .orderBy(songUsage.count().desc())
                .limit(limit)
                .fetch();
    }
}
