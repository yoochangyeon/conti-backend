package com.conti.domain.song.repository;

import com.conti.domain.song.entity.SongArrangement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SongArrangementRepository extends JpaRepository<SongArrangement, Long> {

    List<SongArrangement> findBySongIdOrderByIsDefaultDescCreatedAtAsc(Long songId);

    Optional<SongArrangement> findBySongIdAndIsDefaultTrue(Long songId);
}
