package com.conti.domain.setlist.service;

import com.conti.domain.setlist.dto.SetlistNoteCreateRequest;
import com.conti.domain.setlist.dto.SetlistNoteResponse;
import com.conti.domain.setlist.dto.SetlistNoteUpdateRequest;
import com.conti.domain.setlist.entity.SetlistNote;
import com.conti.domain.setlist.repository.SetlistNoteRepository;
import com.conti.domain.setlist.repository.SetlistRepository;
import com.conti.domain.user.entity.User;
import com.conti.domain.user.repository.UserRepository;
import com.conti.global.error.BusinessException;
import com.conti.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SetlistNoteService {

    private final SetlistNoteRepository setlistNoteRepository;
    private final SetlistRepository setlistRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<SetlistNoteResponse> getNotes(Long setlistId, String position) {
        setlistRepository.findById(setlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETLIST_NOT_FOUND));

        List<SetlistNote> notes;
        if (position != null && !position.isBlank()) {
            notes = setlistNoteRepository.findBySetlistIdAndPositionOrderByCreatedAtDesc(setlistId, position);
        } else {
            notes = setlistNoteRepository.findBySetlistIdOrderByCreatedAtDesc(setlistId);
        }

        return notes.stream()
                .map(note -> {
                    String authorName = userRepository.findById(note.getAuthorId())
                            .map(User::getName)
                            .orElse("알 수 없음");
                    return SetlistNoteResponse.from(note, authorName);
                })
                .toList();
    }

    @Transactional
    public SetlistNoteResponse createNote(Long setlistId, Long userId, SetlistNoteCreateRequest request) {
        setlistRepository.findById(setlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETLIST_NOT_FOUND));

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        SetlistNote note = SetlistNote.builder()
                .setlistId(setlistId)
                .authorId(userId)
                .position(request.position())
                .content(request.content())
                .build();
        setlistNoteRepository.save(note);

        return SetlistNoteResponse.from(note, author.getName());
    }

    @Transactional
    public SetlistNoteResponse updateNote(Long noteId, SetlistNoteUpdateRequest request) {
        SetlistNote note = setlistNoteRepository.findById(noteId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETLIST_NOTE_NOT_FOUND));

        note.update(request.content(), request.position());

        String authorName = userRepository.findById(note.getAuthorId())
                .map(User::getName)
                .orElse("알 수 없음");

        return SetlistNoteResponse.from(note, authorName);
    }

    @Transactional
    public void deleteNote(Long noteId) {
        SetlistNote note = setlistNoteRepository.findById(noteId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETLIST_NOTE_NOT_FOUND));

        setlistNoteRepository.delete(note);
    }
}
