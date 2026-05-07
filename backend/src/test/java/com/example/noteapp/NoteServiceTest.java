package com.example.noteapp;

import com.example.noteapp.dto.NoteRequestDto;
import com.example.noteapp.dto.NoteResponseDto;
import com.example.noteapp.repository.NoteRepository;
import com.example.noteapp.service.NoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")      // application-test.yml (H2)
@Transactional               // 각 테스트 후 롤백
class NoteServiceTest {

    @Autowired
    private NoteService noteService;

    @Autowired
    private NoteRepository noteRepository;

    @BeforeEach
    void setUp() {
        noteRepository.deleteAll();

        NoteRequestDto dto1 = new NoteRequestDto();
        dto1.setTitle("AWS 학습 시작");
        dto1.setContent("VPC부터 시작합니다.");
        dto1.setAuthor("admin");
        noteService.create(dto1);

        NoteRequestDto dto2 = new NoteRequestDto();
        dto2.setTitle("3-Tier 아키텍처");
        dto2.setContent("Web-App-DB 분리");
        dto2.setAuthor("admin");
        noteService.create(dto2);
    }

    @Test
    @DisplayName("전체 조회 — 2건 반환")
    void findAll_returns2Notes() {
        List<NoteResponseDto> result = noteService.findAll();
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting("title")
                .containsExactlyInAnyOrder("AWS 학습 시작", "3-Tier 아키텍처");
    }

    @Test
    @DisplayName("단건 조회 — 제목 일치")
    void findById_returnsCorrectNote() {
        Long id = noteService.findAll().get(0).getId();
        NoteResponseDto result = noteService.findById(id);
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getTitle()).isNotBlank();
    }

    @Test
    @DisplayName("생성 — serverIp 자동 주입")
    void create_injectsServerIp() {
        NoteRequestDto dto = new NoteRequestDto();
        dto.setTitle("이중화 학습");
        dto.setContent("ALB + ASG");
        dto.setAuthor("student");

        NoteResponseDto result = noteService.create(dto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getTitle()).isEqualTo("이중화 학습");
        assertThat(result.getServerIp()).isNotBlank();
    }

    @Test
    @DisplayName("수정 — 제목·내용 변경 확인")
    void update_changesFields() {
        Long id = noteService.findAll().get(0).getId();

        NoteRequestDto dto = new NoteRequestDto();
        dto.setTitle("수정된 제목");
        dto.setContent("수정된 내용");
        dto.setAuthor("editor");

        NoteResponseDto result = noteService.update(id, dto);
        assertThat(result.getTitle()).isEqualTo("수정된 제목");
        assertThat(result.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("삭제 후 조회 — 404 예외")
    void delete_thenFindById_throws404() {
        Long id = noteService.findAll().get(0).getId();
        noteService.delete(id);

        assertThatThrownBy(() -> noteService.findById(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("찾을 수 없습니다");
    }

    @Test
    @DisplayName("없는 ID 조회 — 예외 발생")
    void findById_invalidId_throws() {
        assertThatThrownBy(() -> noteService.findById(99999L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
