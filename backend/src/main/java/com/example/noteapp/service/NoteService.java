package com.example.noteapp.service;

import com.example.noteapp.dto.NoteRequestDto;
import com.example.noteapp.dto.NoteResponseDto;
import com.example.noteapp.entity.Note;
import com.example.noteapp.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;

    /**
     * 현재 서버 IP 조회
     * ALB가 여러 EC2에 트래픽을 분산할 때 어느 서버가 처리했는지 확인용
     */
    private String getServerIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "unknown";
        }
    }

    // 전체 조회
    @Transactional(readOnly = true)
    public List<NoteResponseDto> findAll() {
        return noteRepository.findAll()
                .stream()
                .map(NoteResponseDto::from)
                .collect(Collectors.toList());
    }

    // 단건 조회
    @Transactional(readOnly = true)
    public NoteResponseDto findById(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("메모를 찾을 수 없습니다. id=" + id));
        return NoteResponseDto.from(note);
    }

    // 생성
    @Transactional
    public NoteResponseDto create(NoteRequestDto dto) {
        Note note = new Note(
                dto.getTitle(),
                dto.getContent(),
                dto.getAuthor(),
                getServerIp()  // 생성 서버 IP 자동 주입
        );
        return NoteResponseDto.from(noteRepository.save(note));
    }

    // 수정
    @Transactional
    public NoteResponseDto update(Long id, NoteRequestDto dto) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("메모를 찾을 수 없습니다. id=" + id));
        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        note.setAuthor(dto.getAuthor());
        note.setServerIp(getServerIp());  // 수정 서버 IP 갱신
        // @Transactional + dirty checking → 자동 UPDATE
        return NoteResponseDto.from(note);
    }

    // 삭제
    @Transactional
    public void delete(Long id) {
        if (!noteRepository.existsById(id)) {
            throw new IllegalArgumentException("메모를 찾을 수 없습니다. id=" + id);
        }
        noteRepository.deleteById(id);
    }
}
