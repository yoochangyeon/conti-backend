package com.conti.domain.song.repository;

import com.conti.domain.song.dto.TagResponse;
import com.conti.domain.song.entity.SongTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SongTagRepository extends JpaRepository<SongTag, Long> {

    List<SongTag> findBySongId(Long songId);

    void deleteBySongId(Long songId);

    @Query("SELECT DISTINCT st.tag FROM SongTag st JOIN st.song s WHERE s.team.id = :teamId")
    List<String> findDistinctTagsByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT new com.conti.domain.song.dto.TagResponse(st.tag, COUNT(st)) " +
            "FROM SongTag st JOIN st.song s WHERE s.team.id = :teamId " +
            "GROUP BY st.tag ORDER BY COUNT(st) DESC")
    List<TagResponse> findTagsWithCountByTeamId(@Param("teamId") Long teamId);
}
