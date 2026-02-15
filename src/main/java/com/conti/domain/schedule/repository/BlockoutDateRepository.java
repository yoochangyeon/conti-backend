package com.conti.domain.schedule.repository;

import com.conti.domain.schedule.entity.BlockoutDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BlockoutDateRepository extends JpaRepository<BlockoutDate, Long> {

    List<BlockoutDate> findByTeamMemberIdOrderByStartDate(Long teamMemberId);

    @Query("SELECT bd FROM BlockoutDate bd " +
           "WHERE bd.teamMember.id = :teamMemberId " +
           "AND bd.startDate <= :date AND bd.endDate >= :date")
    List<BlockoutDate> findByTeamMemberIdAndDateOverlapping(
            @Param("teamMemberId") Long teamMemberId,
            @Param("date") LocalDate date);

    @Query("SELECT bd FROM BlockoutDate bd " +
           "WHERE bd.teamMember.team.id = :teamId " +
           "AND bd.startDate <= :toDate AND bd.endDate >= :fromDate " +
           "ORDER BY bd.startDate")
    List<BlockoutDate> findByTeamIdAndDateRange(
            @Param("teamId") Long teamId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}
