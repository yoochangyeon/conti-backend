package com.conti.domain.song.repository;

import com.conti.domain.song.dto.SongSearchCondition;
import com.conti.domain.song.dto.TopSongResponse;
import com.conti.domain.song.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface SongQueryRepository {

    Page<Song> searchSongs(Long teamId, SongSearchCondition condition, Pageable pageable);

    List<TopSongResponse> findTopSongs(Long teamId, LocalDate fromDate, LocalDate toDate, int limit);
}
