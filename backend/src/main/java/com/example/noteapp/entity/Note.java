package com.example.noteapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
@Getter
@Setter
@NoArgsConstructor
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 100)
    private String author;

    /**
     * 응답한 App 서버 IP — 로드밸런싱 확인용
     * ALB가 여러 EC2에 트래픽을 분산할 때 어느 서버가 응답했는지 시각화
     */
    @Column(name = "server_ip", length = 50)
    private String serverIp;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Note(String title, String content, String author, String serverIp) {
        this.title     = title;
        this.content   = content;
        this.author    = author;
        this.serverIp  = serverIp;
    }
}
