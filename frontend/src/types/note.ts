// src/types/note.ts

export interface Note {
  id:        number
  title:     string
  content:   string
  author:    string
  serverIp:  string       // 응답 서버 IP (로드밸런싱 확인용)
  createdAt: string
  updatedAt: string
}

export interface NoteRequest {
  title:   string
  content: string
  author:  string
}

export interface ServerInfo {
  serverIp:  string
  hostname:  string
  timestamp: string
  message:   string
}
