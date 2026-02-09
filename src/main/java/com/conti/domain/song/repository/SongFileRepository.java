package com.conti.domain.song.repository;

import com.conti.domain.song.entity.SongFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongFileRepository extends JpaRepository<SongFile, Long> {

    List<SongFile> findBySongId(Long songId);
}
