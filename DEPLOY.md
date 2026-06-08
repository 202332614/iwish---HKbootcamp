# iwish 배포 가이드 — GCP VM + Cloudflare

구성: **GCP Compute Engine VM** 안에서 직접 빌드·실행(Java jar + systemd) →
**Nginx** 리버스 프록시 → **Cloudflare**(도메인 + SSL).
업로드 이미지는 영구 보존하지 않음(VM 로컬 `uploads/`에만, 인스턴스 삭제 시 사라짐 — 데모 OK).

---

## 1. GCP VM 만들기 (Compute Engine)

1. 콘솔 → Compute Engine → VM 인스턴스 만들기
   - 머신: `e2-small`(2GB) 이상 권장(빌드 메모리), 리전 아무거나(예: asia-northeast3 서울)
   - 부팅 디스크: **Ubuntu 22.04 LTS**, 20GB
   - 방화벽: **HTTP 허용 / HTTPS 허용** 체크
2. **고정 외부 IP 예약**: VPC 네트워크 → IP 주소 → 외부 정적 주소 예약 → 이 VM에 연결
   (Cloudflare A 레코드가 가리킬 IP. 안 하면 재시작 시 IP가 바뀜)
3. 방화벽 확인: tcp **80, 443** 인바운드 허용(위 체크로 자동). 8080은 외부에 열지 않음.
4. 접속: 콘솔의 **SSH** 버튼 또는 `gcloud compute ssh <인스턴스명>`

## 2. 의존성 설치 (VM 안에서)

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk git nginx mysql-server
java -version   # 21 확인
```

## 3. MySQL 준비 (VM 안)

```bash
sudo mysql <<'SQL'
CREATE DATABASE wishlist CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'wishlist'@'localhost' IDENTIFIED BY '여기에_강한_비밀번호';
GRANT ALL PRIVILEGES ON wishlist.* TO 'wishlist'@'localhost';
FLUSH PRIVILEGES;
SQL
```

## 4. 소스 올리고 빌드 (VM 안에서 빌드)

소스를 VM으로: (A) git 사용 시 `git clone <repo>`  또는 (B) 로컬에서 scp
```bash
# (B) 로컬 PC에서 실행 — 프로젝트 폴더 통째로 업로드
scp -r ./wishlist <user>@<VM_IP>:~/wishlist
```

VM에서 빌드:
```bash
cd ~/wishlist
chmod +x ./gradlew
./gradlew bootJar -x test --no-daemon
# 결과: build/libs/wishlist-0.0.1-SNAPSHOT.jar
sudo mkdir -p /opt/iwish
sudo cp build/libs/wishlist-0.0.1-SNAPSHOT.jar /opt/iwish/app.jar
```

## 5. 환경변수 + systemd 서비스로 실행

```bash
sudo tee /opt/iwish/iwish.env >/dev/null <<'EOF'
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/wishlist?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
SPRING_DATASOURCE_USERNAME=wishlist
SPRING_DATASOURCE_PASSWORD=여기에_강한_비밀번호
PORT=8080
EOF
```

`/etc/systemd/system/iwish.service`:
```ini
[Unit]
Description=iwish Spring Boot app
After=network.target mysql.service

[Service]
User=www-data
WorkingDirectory=/opt/iwish
EnvironmentFile=/opt/iwish/iwish.env
ExecStart=/usr/bin/java -jar /opt/iwish/app.jar
SuccessExitStatus=143
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
```

```bash
sudo chown -R www-data:www-data /opt/iwish
sudo systemctl daemon-reload
sudo systemctl enable --now iwish
sudo systemctl status iwish      # active(running) 확인
journalctl -u iwish -f           # 기동 로그
curl -I http://localhost:8080/login   # 200/302 확인
```

## 6. Nginx 리버스 프록시 (80 → 8080)

`/etc/nginx/sites-available/iwish`:
```nginx
server {
    listen 80;
    server_name your-domain.com;          # 본인 도메인

    client_max_body_size 12m;             # 이미지 업로드(최대 10MB) 허용

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host              $host;
        proxy_set_header X-Real-IP         $remote_addr;
        proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```
```bash
sudo ln -s /etc/nginx/sites-available/iwish /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t && sudo systemctl reload nginx
```

> 앱은 `server.forward-headers-strategy=native`라 위 `X-Forwarded-Proto`로 HTTPS를 올바르게 인식한다(로그인/CSRF/리다이렉트 정상).

## 7. Cloudflare 도메인 + SSL

1. Cloudflare에 도메인 추가(네임서버를 Cloudflare로 변경 — 도메인 등록업체에서).
2. **DNS** → A 레코드 추가: `이름=@`(또는 서브도메인), `값=VM 고정 외부 IP`, **Proxy 상태=주황 구름(Proxied)**.
3. **SSL/TLS → 개요**: 모드 선택
   - **권장: Full (Strict)** — 아래 7-1로 오리진 인증서 설치
   - 빠른 대안: **Flexible** (오리진 인증서 불필요, 브라우저↔CF만 HTTPS). 데모면 이걸로 1분 컷.

### 7-1. Full (Strict)용 오리진 인증서 (권장)
1. Cloudflare → SSL/TLS → **오리진 서버** → **인증서 만들기**(15년) → 인증서/키 복사
2. VM에 저장:
```bash
sudo mkdir -p /etc/ssl/cloudflare
sudo nano /etc/ssl/cloudflare/origin.pem   # 인증서 붙여넣기
sudo nano /etc/ssl/cloudflare/origin.key   # 개인키 붙여넣기
```
3. Nginx에 443 추가(위 server 블록을 아래로 교체):
```nginx
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$host$request_uri;      # http→https
}
server {
    listen 443 ssl;
    server_name your-domain.com;
    ssl_certificate     /etc/ssl/cloudflare/origin.pem;
    ssl_certificate_key /etc/ssl/cloudflare/origin.key;

    client_max_body_size 12m;
    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host              $host;
        proxy_set_header X-Real-IP         $remote_addr;
        proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```
```bash
sudo nginx -t && sudo systemctl reload nginx
```
4. Cloudflare SSL/TLS → **Edge Certificates → Always Use HTTPS: On** (선택)

이제 `https://your-domain.com` 접속 → Cloudflare → Nginx(443) → 앱(8080).

## 8. 재배포(코드 수정 후)
```bash
cd ~/wishlist && git pull   # 또는 scp로 최신 소스
./gradlew bootJar -x test --no-daemon
sudo cp build/libs/wishlist-0.0.1-SNAPSHOT.jar /opt/iwish/app.jar
sudo systemctl restart iwish
```

## 참고
- 업로드 이미지는 `/opt/iwish/uploads/`(WorkingDirectory)에 저장 — 인스턴스 삭제 시 사라짐(영구보존 안 함, 합의됨).
- 시드 관리자 계정 `admin/admin1234`는 운영에선 비번 변경 권장.
- 메모리 적은 VM에서 빌드가 버겁다면, 로컬에서 `bootJar` 후 jar만 scp로 올려도 됨.
