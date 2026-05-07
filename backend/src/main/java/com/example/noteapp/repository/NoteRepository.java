package com.example.noteapp.repository;

import com.example.noteapp.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    // 작성자별 조회 (최신순)
    List<Note> findByAuthorOrderByCreatedAtDesc(String author);

    // 제목 키워드 검색 (대소문자 무관)
    List<Note> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String keyword);
}
