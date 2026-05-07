-- =====================================================
-- schema.sql — note_app 데이터베이스 초기화
-- 실행: mysql -h [RDS 엔드포인트] -u admin -p < schema.sql
-- =====================================================

CREATE DATABASE IF NOT EXISTS note_app
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE note_app;

CREATE TABLE IF NOT EXISTS notes (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    title      VARCHAR(200) NOT NULL,
    content    TEXT,
    author     VARCHAR(100) NOT NULL DEFAULT 'anonymous',
    server_ip  VARCHAR(50)  COMMENT '응답 App 서버 IP (로드밸런싱 확인)',
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 초기 데이터
INSERT INTO notes (title, content, author, server_ip) VALUES
  ('AWS 학습 시작',    'VPC, EC2, RDS, ALB, ASG 순서로 학습합니다.', 'admin', 'Init'),
  ('3-Tier 아키텍처', 'Web(Next.js) - App(Spring Boot) - DB(RDS) 계층 분리가 핵심입니다.', 'admin', 'Init'),
  ('이중화 핵심',      'ALB+ASG로 App이중화, RDS Multi-AZ로 DB이중화를 구성합니다.', 'admin', 'Init');

SELECT id, title, author, created_at FROM notes;
