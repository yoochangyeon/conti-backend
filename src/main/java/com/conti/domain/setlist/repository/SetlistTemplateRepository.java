package com.conti.domain.setlist.repository;

import com.conti.domain.setlist.entity.SetlistTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SetlistTemplateRepository extends JpaRepository<SetlistTemplate, Long> {

    List<SetlistTemplate> findByTeamIdOrderByCreatedAtDesc(Long teamId);
}
