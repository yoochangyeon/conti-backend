package com.conti.domain.song.repository;

import com.conti.domain.song.dto.SongSearchCondition;
import com.conti.domain.song.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SongQueryRepository {

    Page<Song> searchSongs(Long teamId, SongSearchCondition condition, Pageable pageable);
}
