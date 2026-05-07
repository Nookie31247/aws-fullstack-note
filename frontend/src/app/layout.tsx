import type { Metadata } from 'next'
import './globals.css'

export const metadata: Metadata = {
  title:       'AWS 학습 노트',
  description: 'Next.js 15 + Spring Boot 3 + RDS 풀스택 실습',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="ko">
      <body className="bg-gray-50 min-h-screen">
        {children}
      </body>
    </html>
  )
}
