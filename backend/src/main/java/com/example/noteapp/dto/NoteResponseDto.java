package com.example.noteapp.dto;

import com.example.noteapp.entity.Note;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// =====================================================
// 응답 DTO — 엔티티 직접 노출 방지
// =====================================================
@Getter
@Setter
public class NoteResponseDto {

    private Long          id;
    private String        title;
    private String        content;
    private String        author;
    private String        serverIp;    // 응답 서버 IP (로드밸런싱 확인)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Entity → DTO 변환
    public static NoteResponseDto from(Note note) {
        NoteResponseDto dto = new NoteResponseDto();
        dto.id        = note.getId();
        dto.title     = note.getTitle();
        dto.content   = note.getContent();
        dto.author    = note.getAuthor();
        dto.serverIp  = note.getServerIp();
        dto.createdAt = note.getCreatedAt();
        dto.updatedAt = note.getUpdatedAt();
        return dto;
    }
}
