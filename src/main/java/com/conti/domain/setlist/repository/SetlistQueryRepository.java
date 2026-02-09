package com.conti.domain.setlist.repository;

import com.conti.domain.setlist.dto.SetlistSearchCondition;
import com.conti.domain.setlist.entity.Setlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SetlistQueryRepository {

    Page<Setlist> searchSetlists(Long teamId, SetlistSearchCondition condition, Pageable pageable);
}
