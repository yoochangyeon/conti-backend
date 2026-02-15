package com.conti.domain.song.repository;

import com.conti.domain.song.entity.SongUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SongUsageRepository extends JpaRepository<SongUsage, Long> {

    List<SongUsage> findBySongId(Long songId);

    long countBySongId(Long songId);

    @Query("SELECT MAX(u.usedAt) FROM SongUsage u WHERE u.song.id = :songId")
    LocalDate findLastUsedAt(@Param("songId") Long songId);

    @Query("SELECT YEAR(u.usedAt) as year, MONTH(u.usedAt) as month, COUNT(u) as cnt " +
            "FROM SongUsage u WHERE u.song.id = :songId " +
            "GROUP BY YEAR(u.usedAt), MONTH(u.usedAt) " +
            "ORDER BY YEAR(u.usedAt) DESC, MONTH(u.usedAt) DESC")
    List<Object[]> findMonthlyUsage(@Param("songId") Long songId);

    @Query("SELECT u.usedKey, COUNT(u) FROM SongUsage u " +
            "WHERE u.song.id = :songId AND u.usedKey IS NOT NULL " +
            "GROUP BY u.usedKey ORDER BY COUNT(u) DESC")
    List<Object[]> findKeyDistribution(@Param("songId") Long songId);

    @Query("SELECT u.leaderId, COUNT(u) FROM SongUsage u " +
            "WHERE u.song.id = :songId AND u.leaderId IS NOT NULL " +
            "GROUP BY u.leaderId ORDER BY COUNT(u) DESC")
    List<Object[]> findLeaderBreakdown(@Param("songId") Long songId);
}
