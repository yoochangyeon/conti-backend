package com.conti.domain.schedule.repository;

import com.conti.domain.schedule.entity.ServiceSchedule;
import com.conti.domain.team.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ServiceScheduleRepository extends JpaRepository<ServiceSchedule, Long> {

    @Query("SELECT ss FROM ServiceSchedule ss " +
           "JOIN FETCH ss.teamMember tm " +
           "JOIN FETCH tm.user " +
           "WHERE ss.setlist.id = :setlistId " +
           "ORDER BY ss.position, ss.createdAt")
    List<ServiceSchedule> findBySetlistIdWithMember(@Param("setlistId") Long setlistId);

    boolean existsBySetlistIdAndTeamMemberIdAndPosition(
            Long setlistId, Long teamMemberId, Position position);

    @Query("SELECT ss FROM ServiceSchedule ss " +
           "JOIN FETCH ss.setlist s " +
           "JOIN FETCH s.team " +
           "WHERE ss.teamMember.id = :teamMemberId " +
           "AND s.worshipDate >= CURRENT_DATE " +
           "ORDER BY s.worshipDate")
    List<ServiceSchedule> findUpcomingByTeamMemberId(@Param("teamMemberId") Long teamMemberId);

    @Query("SELECT ss FROM ServiceSchedule ss " +
           "JOIN FETCH ss.setlist s " +
           "JOIN FETCH s.team " +
           "WHERE ss.teamMember.id = :teamMemberId " +
           "ORDER BY s.worshipDate DESC")
    List<ServiceSchedule> findAllByTeamMemberId(@Param("teamMemberId") Long teamMemberId);

    void deleteBySetlistId(Long setlistId);

    @Query("SELECT ss FROM ServiceSchedule ss " +
           "JOIN FETCH ss.setlist s " +
           "JOIN FETCH ss.teamMember tm " +
           "JOIN FETCH tm.user " +
           "WHERE s.worshipDate = :worshipDate")
    List<ServiceSchedule> findUpcomingByWorshipDate(@Param("worshipDate") LocalDate worshipDate);

    @Query("SELECT ss FROM ServiceSchedule ss " +
           "JOIN FETCH ss.setlist s " +
           "JOIN FETCH ss.teamMember tm " +
           "JOIN FETCH tm.user " +
           "WHERE s.team.id = :teamId " +
           "AND s.worshipDate >= :fromDate " +
           "AND s.worshipDate <= :toDate " +
           "ORDER BY s.worshipDate, ss.position, ss.createdAt")
    List<ServiceSchedule> findByTeamIdAndDateRange(
            @Param("teamId") Long teamId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}
