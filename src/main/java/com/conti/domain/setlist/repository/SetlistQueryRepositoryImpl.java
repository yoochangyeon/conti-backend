package com.conti.domain.setlist.repository;

import com.conti.domain.setlist.dto.SetlistSearchCondition;
import com.conti.domain.setlist.entity.Setlist;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;

import com.conti.domain.setlist.entity.WorshipType;

import java.time.LocalDate;
import java.util.List;

import static com.conti.domain.setlist.entity.QSetlist.setlist;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SetlistQueryRepositoryImpl implements SetlistQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Setlist> searchSetlists(Long teamId, SetlistSearchCondition condition, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(setlist.team.id.eq(teamId));

        builder.and(fromDateGoe(condition.fromDate()));
        builder.and(toDateLoe(condition.toDate()));
        builder.and(worshipTypeEquals(condition.worshipType()));

        List<Setlist> content = queryFactory
                .selectFrom(setlist)
                .where(builder)
                .orderBy(setlist.worshipDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(setlist.count())
                .from(setlist)
                .where(builder);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression fromDateGoe(LocalDate fromDate) {
        if (fromDate == null) {
            return null;
        }
        return setlist.worshipDate.goe(fromDate);
    }

    private BooleanExpression toDateLoe(LocalDate toDate) {
        if (toDate == null) {
            return null;
        }
        return setlist.worshipDate.loe(toDate);
    }

    private BooleanExpression worshipTypeEquals(WorshipType worshipType) {
        if (worshipType == null) {
            return null;
        }
        return setlist.worshipType.eq(worshipType);
    }
}
