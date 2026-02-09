package com.conti.domain.song.repository;

import com.conti.domain.song.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongRepository extends JpaRepository<Song, Long>, SongQueryRepository {

    Page<Song> findByTeamId(Long teamId, Pageable pageable);
}
