package com.conti.domain.song.repository;

import com.conti.domain.song.entity.SongSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongSectionRepository extends JpaRepository<SongSection, Long> {

    List<SongSection> findBySongIdOrderByOrderIndex(Long songId);

    void deleteBySongId(Long songId);
}
