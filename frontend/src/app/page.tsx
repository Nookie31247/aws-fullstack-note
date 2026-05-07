'use client'

import { useCallback, useEffect, useRef, useState } from 'react'
import { noteApi, serverApi } from '@/lib/api'
import { Note, NoteRequest, ServerInfo } from '@/types/note'
import NoteCard from '@/components/NoteCard'
import NoteForm from '@/components/NoteForm'

export default function HomePage() {
  const [notes,       setNotes]       = useState<Note[]>([])
  const [serverInfo,  setServerInfo]  = useState<ServerInfo | null>(null)
  const [prevIp,      setPrevIp]      = useState<string>('')
  const [lbAlert,     setLbAlert]     = useState(false)
  const [loading,     setLoading]     = useState(true)
  const [error,       setError]       = useState<string | null>(null)
  const [successMsg,  setSuccessMsg]  = useState<string | null>(null)
  const [showForm,    setShowForm]    = useState(false)
  const [editNote,    setEditNote]    = useState<Note | null>(null)
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null)

  // ── 메모 목록 로드 ────────────────────────────────────────
  const loadNotes = useCallback(async () => {
    try {
      const data = await noteApi.getAll()
      setNotes(data)
      setError(null)
    } catch (e) {
      setError(e instanceof Error ? e.message : '서버 연결 오류')
    } finally {
      setLoading(false)
    }
  }, [])

  // ── 서버 정보 갱신 (10초마다) ────────────────────────────
  const refreshServerInfo = useCallback(async () => {
    try {
      const info = await serverApi.getInfo()
      setServerInfo(info)
      if (prevIp && prevIp !== info.serverIp) {
        setLbAlert(true)
        setTimeout(() => setLbAlert(false), 5000)
      }
      setPrevIp(info.serverIp)
    } catch {
      // 서버 정보 실패는 무시
    }
  }, [prevIp])

  useEffect(() => {
    loadNotes()
    refreshServerInfo()
    timerRef.current = setInterval(refreshServerInfo, 10_000)
    return () => {
      if (timerRef.current) clearInterval(timerRef.current)
    }
  }, [loadNotes, refreshServerInfo])

  // ── 알림 표시 헬퍼 ───────────────────────────────────────
  const showSuccess = (msg: string) => {
    setSuccessMsg(msg)
    setTimeout(() => setSuccessMsg(null), 3000)
  }

  // ── CRUD 핸들러 ───────────────────────────────────────────
  const handleCreate = async (req: NoteRequest) => {
    await noteApi.create(req)
    setShowForm(false)
    showSuccess('메모가 등록되었습니다.')
    loadNotes()
  }

  const handleUpdate = async (req: NoteRequest) => {
    if (!editNote) return
    await noteApi.update(editNote.id, req)
    setEditNote(null)
    showSuccess('메모가 수정되었습니다.')
    loadNotes()
  }

  const handleDelete = async (id: number) => {
    if (!confirm('삭제하시겠습니까?')) return
    await noteApi.delete(id)
    showSuccess('메모가 삭제되었습니다.')
    loadNotes()
  }

  // ── 렌더링 ────────────────────────────────────────────────
  return (
    <main className="min-h-screen bg-gray-50">

      {/* 헤더 */}
      <header className="bg-white border-b-2 border-orange-400 shadow-sm">
        <div className="max-w-3xl mx-auto px-4 py-4 flex items-center justify-between gap-4">
          <div>
            <h1 className="text-xl font-bold text-orange-500">AWS 학습 노트</h1>
            <p className="text-xs text-gray-400 mt-0.5">
              Next.js 15 · Spring Boot 3 · RDS 풀스택 실습
            </p>
          </div>
          <button
            onClick={() => { setShowForm(true); setEditNote(null) }}
            className="bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium
                       px-4 py-2 rounded-lg transition-colors"
          >
            + 새 메모
          </button>
        </div>
      </header>

      {/* 서버 정보 배너 — 로드밸런싱 시각화 */}
      <div className="bg-slate-800 text-slate-200 px-4 py-2 text-xs flex flex-wrap gap-4 items-center">
        <span>
          🖥 응답 서버 IP:{' '}
          <span className="font-mono font-bold text-yellow-300">
            {serverInfo?.serverIp ?? '조회 중...'}
          </span>
        </span>
        <span>📍 {serverInfo?.hostname ?? '—'}</span>
        {lbAlert && (
          <span className="bg-green-500 text-white px-2 py-0.5 rounded-full animate-pulse font-semibold">
            🔀 LB 전환 감지!
          </span>
        )}
      </div>

      <div className="max-w-3xl mx-auto px-4 py-6 space-y-4">

        {/* 오류 메시지 */}
        {error && (
          <div className="bg-red-50 border-l-4 border-red-400 text-red-700 text-sm p-4 rounded-lg">
            ❌ {error}
          </div>
        )}

        {/* 성공 메시지 */}
        {successMsg && (
          <div className="bg-green-50 border-l-4 border-green-400 text-green-700 text-sm p-4 rounded-lg">
            ✅ {successMsg}
          </div>
        )}

        {/* 생성 폼 */}
        {showForm && (
          <NoteForm
            onSubmit={handleCreate}
            onCancel={() => setShowForm(false)}
          />
        )}

        {/* 수정 폼 */}
        {editNote && (
          <NoteForm
            initialData={editNote}
            onSubmit={handleUpdate}
            onCancel={() => setEditNote(null)}
          />
        )}

        {/* 목록 */}
        {loading ? (
          <div className="text-center py-16 text-gray-400">로딩 중...</div>
        ) : notes.length === 0 ? (
          <div className="text-center py-16 text-gray-400">
            <p className="text-base">메모가 없습니다.</p>
            <p className="text-sm mt-1">새 메모 버튼을 눌러 시작하세요.</p>
          </div>
        ) : (
          <div className="grid gap-3">
            {notes.map(note => (
              <NoteCard
                key={note.id}
                note={note}
                onEdit={() => { setEditNote(note); setShowForm(false) }}
                onDelete={() => handleDelete(note.id)}
              />
            ))}
          </div>
        )}
      </div>
    </main>
  )
}
