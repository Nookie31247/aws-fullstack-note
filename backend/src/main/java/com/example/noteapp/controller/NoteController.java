package com.example.noteapp.controller;

import com.example.noteapp.dto.NoteRequestDto;
import com.example.noteapp.dto.NoteResponseDto;
import com.example.noteapp.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    // GET /api/notes — 전체 조회
    @GetMapping
    public ResponseEntity<List<NoteResponseDto>> findAll() {
        return ResponseEntity.ok(noteService.findAll());
    }

    // GET /api/notes/{id} — 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<NoteResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(noteService.findById(id));
    }

    // POST /api/notes — 생성
    @PostMapping
    public ResponseEntity<NoteResponseDto> create(@Valid @RequestBody NoteRequestDto dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(noteService.create(dto));
    }

    // PUT /api/notes/{id} — 수정
    @PutMapping("/{id}")
    public ResponseEntity<NoteResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody NoteRequestDto dto) {
        return ResponseEntity.ok(noteService.update(id, dto));
    }

    // DELETE /api/notes/{id} — 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        noteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
