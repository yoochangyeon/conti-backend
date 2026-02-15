package com.conti.domain.setlist.repository;

import com.conti.domain.setlist.entity.SetlistNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SetlistNoteRepository extends JpaRepository<SetlistNote, Long> {

    List<SetlistNote> findBySetlistIdOrderByCreatedAtDesc(Long setlistId);

    List<SetlistNote> findBySetlistIdAndPositionOrderByCreatedAtDesc(Long setlistId, String position);
}
