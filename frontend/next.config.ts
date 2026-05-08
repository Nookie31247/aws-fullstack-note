import type { NextConfig } from 'next'

const nextConfig: NextConfig = {
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://internal-fullstack-alb-backend-821091328.ap-northeast-2.elb.amazonaws.com:8080/api/:path*',
      },
    ]
  },
  async headers() {
    return [
      {
        source: '/api/:path*',
        headers: [
          {
            key: 'X-Forwarded-Host',
            value: 'nookie-server.store',
          },
        ],
      },
    ]
  },
}

export default nextConfig
