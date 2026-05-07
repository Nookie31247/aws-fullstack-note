'use client'

import { Note } from '@/types/note'

interface Props {
  note:     Note
  onEdit:   () => void
  onDelete: () => void
}

export default function NoteCard({ note, onEdit, onDelete }: Props) {
  const formatDate = (s: string) =>
    new Date(s).toLocaleString('ko-KR', {
      dateStyle: 'short',
      timeStyle: 'short',
    })

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-5
                    hover:shadow-md transition-shadow">
      {/* 제목 + 버튼 */}
      <div className="flex items-start justify-between mb-2 gap-3">
        <h2 className="text-base font-semibold text-gray-800 leading-snug">
          {note.title}
        </h2>
        <div className="flex gap-2 flex-shrink-0">
          <button
            onClick={onEdit}
            className="text-xs px-3 py-1 rounded-lg bg-blue-50 text-blue-600
                       hover:bg-blue-100 transition-colors"
          >
            수정
          </button>
          <button
            onClick={onDelete}
            className="text-xs px-3 py-1 rounded-lg bg-red-50 text-red-500
                       hover:bg-red-100 transition-colors"
          >
            삭제
          </button>
        </div>
      </div>

      {/* 내용 */}
      {note.content && (
        <p className="text-gray-600 text-sm leading-relaxed mb-3 whitespace-pre-line">
          {note.content}
        </p>
      )}

      {/* 메타 정보 */}
      <div className="flex flex-wrap items-center gap-3 text-xs text-gray-400
                      border-t border-gray-50 pt-3">
        <span>✍ {note.author}</span>
        {/* 로드밸런싱 확인용 — ALB가 다른 EC2로 라우팅하면 IP가 바뀜 */}
        <span className="font-mono bg-slate-100 px-2 py-0.5 rounded text-slate-500">
          🖥 {note.serverIp}
        </span>
        <span className="ml-auto">{formatDate(note.createdAt)}</span>
      </div>
    </div>
  )
}
