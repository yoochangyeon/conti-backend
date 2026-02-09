package com.conti.domain.song.repository;

import com.conti.domain.song.entity.SongUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongUsageRepository extends JpaRepository<SongUsage, Long> {

    List<SongUsage> findBySongId(Long songId);
}
