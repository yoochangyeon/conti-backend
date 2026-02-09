package com.conti.domain.setlist.repository;

import com.conti.domain.setlist.entity.Setlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SetlistRepository extends JpaRepository<Setlist, Long>, SetlistQueryRepository {

    Page<Setlist> findByTeamId(Long teamId, Pageable pageable);
}
