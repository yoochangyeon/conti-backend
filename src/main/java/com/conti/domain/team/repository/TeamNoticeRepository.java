package com.conti.domain.team.repository;

import com.conti.domain.team.entity.TeamNotice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamNoticeRepository extends JpaRepository<TeamNotice, Long> {

    List<TeamNotice> findByTeamIdOrderByIsPinnedDescCreatedAtDesc(Long teamId);
}
