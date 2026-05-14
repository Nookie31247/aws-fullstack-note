#!/bin/bash

# 시작 템플릿 이름: fullstack-lt-fe
# AMI: Ubuntu 24.04 LTS / 유형: t3.micro
# SG: fullstack-sg-fe-ec2
# IAM 프로파일: fullstack-role-ec2
# User Data

set -euo pipefail

# 로그 파일로 모든 출력 리다이렉트
exec > >(tee /var/log/userdata.log) 2>&1
echo "[$(date)] User Data 시작"

apt-get update -y && apt-get install -y nginx curl unzip
curl -fsSL https://deb.nodesource.com/setup_20.x | bash -
apt-get install -y nodejs && npm install -g pm2

# awscli 설치
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o /tmp/awscliv2.zip
unzip /tmp/awscliv2.zip -d /tmp
/tmp/aws/install
rm /tmp/awscliv2.zip
rm -r /tmp/aws
 
# Nginx 설정
cat > /etc/nginx/sites-available/default <<'NGINXEOF'
server {
    listen 80 default_server;
    server_name _;
    location /health { access_log off;
        return 200 '{"status":"ok"}';
        add_header Content-Type application/json; }
    location / {
        proxy_pass http://127.0.0.1:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr; }
}
NGINXEOF
nginx -t && systemctl enable nginx && systemctl reload nginx
 
# PM2 부팅 자동 시작 등록 (EC2 재부팅 후 Next.js 자동 시작)
env PATH=$PATH:/usr/bin pm2 startup systemd -u ubuntu --hp /home/ubuntu
# → systemd 유닛 파일 자동 생성됨

# /opt/frontend 디렉터리 생성
mkdir -p /opt/frontend
 
# 초기 플레이스홀더 서버 (Next.js 배포 전 ALB 헬스 체크 통과용)
cat > /opt/frontend/placeholder.js << 'JSEOF'
const http = require('http')
http.createServer((req, res) => {
  const body = req.url === '/health'
    ? JSON.stringify({status:'ok', note:'initializing'})
    : '<h1>서비스 준비 중입니다...</h1>'
  const ct = req.url === '/health' ? 'application/json' : 'text/html'
  res.writeHead(200, {'Content-Type': ct})
  res.end(body)
}).listen(3000)
JSEOF
chown ubuntu:ubuntu /opt/frontend/placeholder.js
sudo -u ubuntu pm2 start /opt/frontend/placeholder.js --name note-frontend
sudo -u ubuntu pm2 save
 
# SSM Agent
systemctl enable amazon-ssm-agent 2>/dev/null || true
systemctl start  amazon-ssm-agent 2>/dev/null || \
  snap start amazon-ssm-agent 2>/dev/null || true

# S3에서 프론트엔드 다운로드 후 배포
aws s3 cp s3://fullstack-deploy-971581687587/frontend/frontend-latest.zip \
  /tmp/fe.zip --region ap-northeast-2

unzip -o /tmp/fe.zip -d /tmp/fe-new

# placeholder 서버 중지
sudo -u ubuntu pm2 stop note-frontend 2>/dev/null || true
sudo -u ubuntu pm2 delete note-frontend 2>/dev/null || true

# 빌드 결과물 복사
cp -r /tmp/fe-new/.next /opt/frontend/
cp -r /tmp/fe-new/public /opt/frontend/ 2>/dev/null || true
cp /tmp/fe-new/package.json /opt/frontend/
cp /tmp/fe-new/package-lock.json /opt/frontend/
cp /tmp/fe-new/next.config.ts /opt/frontend/ 2>/dev/null || true

chown -R ubuntu:ubuntu /opt/frontend

# 의존성 설치 후 Next.js 시작
cd /opt/frontend && npm install --production
sudo -u ubuntu pm2 start npm --name note-frontend -- start
sudo -u ubuntu pm2 save

# 헬스 체크 (최대 60초 대기)
for i in $(seq 1 12); do
  STATUS=$(curl -sf -o /dev/null -w "%{http_code}" http://localhost:3000 2>/dev/null || echo "000")
  if [ "$STATUS" = "200" ]; then
    echo "[$(date)] 프론트엔드 헬스 체크 성공"
    break
  fi
  echo "[$(date)] 대기 중... ($i/12)"
  sleep 5
done

# 임시 파일 정리
rm -rf /tmp/fe.zip /tmp/fe-new