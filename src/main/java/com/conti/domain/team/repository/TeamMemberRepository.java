package com.conti.domain.team.repository;

import com.conti.domain.team.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findByTeamId(Long teamId);

    List<TeamMember> findByUserId(Long userId);

    Optional<TeamMember> findByUserIdAndTeamId(Long userId, Long teamId);

    boolean existsByUserIdAndTeamId(Long userId, Long teamId);
}
