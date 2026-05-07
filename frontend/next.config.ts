import type { NextConfig } from 'next'

const nextConfig: NextConfig = {
  // 환경변수: 로컬은 .env.local, 운영은 .env.production
  env: {
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080',
  },
}

export default nextConfig
