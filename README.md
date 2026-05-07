# 풀스택 노트 앱 — AWS HA 실습 예제

## ⚠️ 버전 안내

| 구분 | 적용 버전       | 사유 |
|------|-------------|------|
| Spring Boot | **3.5.14**  | 3.5.14는 버전만 교체 가능 (build.gradle 1줄) |
| Next.js | **15.5.15** | Next.js 6 (2018년)은 App Router·TypeScript 미지원. 현재 stable 최신 사용 |
| Java | **17**      | Spring Boot 3.x 필수 요구사항 |

### Spring Boot 버전 변경 방법
```groovy
// backend/build.gradle
id 'org.springframework.boot' version '3.5.14'  // 버전만 교체
```

---

## 🗂 프로젝트 구조

```
fullstack-note-app/
├── backend/                    ← Spring Boot 3.3.5 (Java 17)
│   ├── build.gradle
│   ├── settings.gradle
│   └── src/
│       ├── main/
│       │   ├── java/com/example/noteapp/
│       │   │   ├── NoteAppApplication.java
│       │   │   ├── entity/Note.java
│       │   │   ├── dto/NoteRequestDto.java
│       │   │   ├── dto/NoteResponseDto.java
│       │   │   ├── repository/NoteRepository.java
│       │   │   ├── service/NoteService.java
│       │   │   ├── controller/NoteController.java
│       │   │   ├── controller/ServerInfoController.java
│       │   │   ├── config/CorsConfig.java
│       │   │   └── exception/GlobalExceptionHandler.java
│       │   └── resources/application.yml
│       └── test/
│           ├── java/.../NoteServiceTest.java   ← H2 단위 테스트 5개
│           └── resources/application-test.yml
├── frontend/                   ← Next.js 15.5.15 (App Router)
│   ├── package.json
│   ├── next.config.ts
│   ├── tsconfig.json
│   ├── tailwind.config.ts
│   ├── .env.local              ← 로컬용 (localhost:8080)
│   ├── .env.production         ← 운영용 ([BE-ALB-DNS])
│   └── src/
│       ├── app/
│       │   ├── layout.tsx
│       │   ├── page.tsx        ← 메인 페이지 (CRUD + LB 시각화)
│       │   └── globals.css
│       ├── components/
│       │   ├── NoteCard.tsx
│       │   └── NoteForm.tsx
│       ├── lib/api.ts          ← Spring Boot API 클라이언트
│       └── types/note.ts
└── sql/
    └── schema.sql              ← DB 초기화 SQL
```

---

## 🚀 로컬 실행 방법

### 1. MySQL DB 초기화
```bash
mysql -u root -p < sql/schema.sql
```

### 2. 백엔드 실행 (Spring Boot)
```bash
cd backend

# 테스트 (MySQL 없이 H2로 실행)
./gradlew test

# 로컬 서버 실행 (MySQL 필요)
export DB_HOST=localhost DB_USER=root DB_PASS=password
./gradlew bootRun
# → http://localhost:8080
```

### 3. 프론트엔드 실행 (Next.js)
```bash
cd frontend
npm install
npm run dev
# → http://localhost:3000
```

---

## 🔌 REST API

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | /api/notes | 전체 조회 |
| GET | /api/notes/{id} | 단건 조회 |
| POST | /api/notes | 생성 |
| PUT | /api/notes/{id} | 수정 |
| DELETE | /api/notes/{id} | 삭제 |
| GET | /api/server-info | 응답 서버 IP (로드밸런싱 확인) |
| GET | /actuator/health | ALB 헬스 체크 |

---

## ☁️ AWS 배포 환경변수

### Backend EC2 `/opt/app/.env`
```
DB_HOST=[RDS 엔드포인트]
DB_PORT=3306
DB_USER=admin
DB_PASS=[RDS 암호]
SERVER_PORT=8080
CORS_ORIGINS=http://[FE-ALB-DNS]
```

### Frontend `.env.production`
```
NEXT_PUBLIC_API_URL=http://[BE-ALB-DNS]
```
