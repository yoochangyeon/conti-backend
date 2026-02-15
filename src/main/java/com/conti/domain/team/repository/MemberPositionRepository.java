package com.conti.domain.team.repository;

import com.conti.domain.team.entity.MemberPosition;
import com.conti.domain.team.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberPositionRepository extends JpaRepository<MemberPosition, Long> {

    List<MemberPosition> findByTeamMemberId(Long teamMemberId);

    void deleteByTeamMemberId(Long teamMemberId);

    @Query("SELECT mp.teamMember.id FROM MemberPosition mp " +
           "WHERE mp.teamMember.team.id = :teamId AND mp.position = :position")
    List<Long> findTeamMemberIdsByTeamIdAndPosition(
            @Param("teamId") Long teamId,
            @Param("position") Position position);
}
