#!/bin/bash

# 시작 템플릿 이름: fullstack-lt-be
# AMI: Ubuntu 24.04 LTS / 유형: t3.micro
# SG: fullstack-sg-be-ec2
# IAM 프로파일: fullstack-role-ec2 (SSM + S3 + CloudWatch)
# User Data (핵심 — 환경변수로 DB 접속 정보 주입)

set -euo pipefail

# 로그 파일로 모든 출력 리다이렉트
exec > >(tee /var/log/userdata.log) 2>&1
echo "[$(date)] User Data 시작"

apt-get update -y && apt-get install -y openjdk-17-jdk curl unzip

# awscli 설치
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o /tmp/awscliv2.zip
unzip /tmp/awscliv2.zip -d /tmp
/tmp/aws/install
rm /tmp/awscliv2.zip
rm -r /tmp/aws

# IMDSv2 메타데이터
TOKEN=$(curl -sf -X PUT http://169.254.169.254/latest/api/token \
 -H 'X-aws-ec2-metadata-token-ttl-seconds: 21600')
LOCAL_IP=$(curl -sf -H "X-aws-ec2-metadata-token: $TOKEN" \
 http://169.254.169.254/latest/meta-data/local-ipv4)
mkdir -p /opt/app /opt/app/logs
# mysql-client 설치 (RDS 초기화용 — 11단계에서 사용)
apt-get install -y mysql-client
# Spring Boot 환경변수 설정 파일
# ⚠ [RDS 엔드포인트]와 [RDS 암호]는 14단계(BE ALB 생성) 이후 실제값으로 교체
# ⚠ [FE-ALB-DNS]는 18단계(FE ALB 생성) 이후 실제값으로 교체
cat > /opt/app/.env <<ENVEOF
DB_HOST=fullstack-mysql-db.crqssuuq048f.ap-northeast-2.rds.amazonaws.com
DB_PORT=3306
DB_USER=admin
DB_PASS=qwer1234
SERVER_PORT=8080
CORS_ORIGINS=https://nookie-server.store
ENVEOF
# systemd 서비스 등록
cat > /etc/systemd/system/note-app.service <<SVCEOF
[Unit]
Description=Note App Spring Boot
After=network.target
[Service]
User=ubuntu
WorkingDirectory=/opt/app
EnvironmentFile=/opt/app/.env
ExecStart=/usr/bin/java -jar /opt/app/note-app.jar
StandardOutput=append:/opt/app/logs/app.log
StandardError=append:/opt/app/logs/app.log
Restart=always
RestartSec=10
[Install]
WantedBy=multi-user.target
SVCEOF
systemctl daemon-reload
systemctl enable note-app
# SSM Agent
systemctl enable amazon-ssm-agent 2>/dev/null || true
systemctl start amazon-ssm-agent 2>/dev/null || \
 snap start amazon-ssm-agent 2>/dev/null || true
# S3에서 JAR 다운로드 후 서비스 시작
aws s3 cp s3://fullstack-deploy-971581687587/backend/note-app-latest.jar \
  /opt/app/note-app.jar --region ap-northeast-2
chown ubuntu:ubuntu /opt/app/note-app.jar
systemctl start note-app

# 헬스 체크 (최대 60초 대기)
for i in $(seq 1 12); do
  STATUS=$(curl -sf -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health 2>/dev/null || echo "000")
  if [ "$STATUS" = "200" ]; then
    echo "[$(date)] 헬스 체크 성공"
    break
  fi
  echo "[$(date)] 대기 중... ($i/12)"
  sleep 5
done