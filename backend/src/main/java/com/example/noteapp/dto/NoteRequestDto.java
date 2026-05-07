package com.example.noteapp.dto;

import com.example.noteapp.entity.Note;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// =====================================================
// 생성·수정 요청 DTO
// =====================================================
@Getter
@Setter
public class NoteRequestDto {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자 이내여야 합니다.")
    private String title;

    private String content;

    @NotBlank(message = "작성자는 필수입니다.")
    @Size(max = 100, message = "작성자는 100자 이내여야 합니다.")
    private String author;
}
