package com.conti.domain.setlist.repository;

import com.conti.domain.setlist.entity.SetlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SetlistItemRepository extends JpaRepository<SetlistItem, Long> {

    List<SetlistItem> findBySetlistIdOrderByOrderIndex(Long setlistId);

    List<SetlistItem> findBySetlistId(Long setlistId);

    void deleteBySetlistId(Long setlistId);

    int countBySetlistId(Long setlistId);
}
