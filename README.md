# 🧥 iwish — 옷 위시리스트 & 예산 관리

> 사고 싶은 옷을 한곳에 모으고, **월 예산** 안에서 우선순위를 정하고, **룩북(코디)** 으로 묶어 관리하는 웹 서비스.
> 4차 해커톤 · Java Spring Boot 프로젝트.

---

## 1. 프로젝트 소개

쇼핑몰을 돌아다니다 마음에 든 옷을 캡처·메모로 흩어두면 결국 잊어버리고 충동구매로 이어진다.
**iwish** 는 갖고 싶은 옷을 한곳에 저장하고, **월 예산 대비 사용률**을 한눈에 보여주며,
우선순위(별점)와 **코디 룩북**으로 "무엇을 먼저 살지"를 계획적으로 정리하도록 돕는다.

- 무드: 정제된 셀렉트샵 / 매거진 감성 (민트 포인트 `#10D9C4`, Pretendard)
- 상품 이미지가 주인공이 되는 카드형 UI

## 2. 주요 기능

| 분류 | 기능 |
|------|------|
| 회원 | 회원가입 · 로그인/로그아웃 · **로그인 유지(remember-me, 14일)** |
| 위시리스트 | 등록 · 조회(본인 것만) · 수정 · 삭제 · **구매완료 토글** · 우선순위(★1~5) · 카테고리 |
| 썸네일 | **상품 URL의 대표 이미지(og:image) 자동 추출** · 또는 **이미지 직접 업로드** |
| 예산 대시보드 | 월 예산 설정 · 위시 총액(미구매) · 잔액 · **사용률 프로그레스바** · 예산 초과 경고 |
| 검색·필터 | 키워드(상품명/쇼핑몰) · 카테고리 · 우선순위 · 구매여부 |
| 룩북(코디) | 코디 느낌별 모음(상품 ↔ 룩북 **N:M**) · 위시템 담기/빼기 · **"코디 총액"** |
| 통계 | 총 위시템/금액 · 구매 완료율 · 카테고리별 금액 비중 · 갖고 싶은 정도(우선순위 분포) |

> 모든 데이터는 **본인 소유만** 접근 가능(Service 계층에서 소유자 검증). 상태 변경은 전부 POST 폼 + **CSRF** 토큰.

## 3. 기술 스택

- **Backend**: Java 21, Spring Boot 3.5, Spring MVC
- **보안**: Spring Security (폼 로그인, BCrypt 해시, CSRF, remember-me 해시 토큰)
- **데이터**: Spring Data JPA / Hibernate, MySQL 8
- **뷰**: Thymeleaf + thymeleaf-extras-springsecurity6
- **이미지**: Jsoup(og:image 파싱), 멀티파트 파일 업로드
- **프론트**: Tailwind CSS(CDN) + Pretendard, 디자인 토큰/프래그먼트 기반
- **빌드/배포**: Gradle, GCP Compute Engine VM + Nginx + Cloudflare(도메인·SSL)

## 4. 화면 / 라우트

| 화면 | 경로 | 설명 |
|------|------|------|
| 랜딩 | `GET /` | 서비스 소개, 로그인/회원가입 진입 |
| 로그인 | `GET/POST /login` | 폼 로그인(+로그인 유지) |
| 회원가입 | `GET/POST /signup` | |
| 위시리스트 | `GET /wishlist` | 예산 대시보드 + 검색/필터 + 카드 그리드 |
| 위시템 등록/수정 | `GET/POST /wishlist/new`, `/wishlist/{id}` | 이미지 업로드/자동추출 |
| 구매토글/삭제 | `POST /wishlist/{id}/toggle`, `/delete` | |
| 예산 설정 | `POST /budget` | |
| 룩북 목록 | `GET /collections` | |
| 룩북 생성/수정 | `GET/POST /collections/new`, `/collections/{id}` | |
| 룩북 상세 | `GET /collections/{id}` | 담기/빼기, 코디 총액 |
| 담기/빼기 | `POST /collections/{id}/items`, `/items/{wishId}/delete` | |
| 통계 | `GET /stats` | |

## 5. 도메인 모델

- **User** `1 ── N` **WishItem**
- **Collection** `N ── M` **WishItem** (단방향, 조인테이블 `collection_wish_item`)

| 엔티티 | 주요 필드 |
|--------|-----------|
| `User` | username(unique), password(BCrypt), role(USER/ADMIN), budget, createdAt |
| `WishItem` | name, price, shopName, category, priority(1~5), productUrl, thumbnailUrl, purchased, user |
| `Collection` | name, description, styleTag, coverImageUrl, user, items(Set\<WishItem\>) |

## 6. 디렉터리 구조

```
src/main/java/com/hackathon/wishlist
├─ config/        SecurityConfig, WebConfig, DataInitializer
├─ controller/    Home, Wish, Collection, Stats
├─ domain/        User, WishItem, Collection, Role
├─ dto/           WishForm, WishFilter, BudgetSummary, CollectionForm/Card, Stats*
├─ repository/    User, Wish, Collection
└─ service/       Wish, Collection, Stats, User, CustomUserDetails, OgImageExtractor, FileStorage
src/main/resources
├─ templates/     index, login, signup, wishlist, wish-form, collections, collection-*, stats
│   └─ fragments/ layout.html (assets/appHeader/footer/logo 프래그먼트, 디자인 토큰)
└─ application.properties
```

## 7. 로컬 실행

전제: JDK 21, MySQL 8

```bash
# 1) DB 생성
mysql -u root -p -e "CREATE DATABASE wishlist CHARACTER SET utf8mb4;"

# 2) 접속정보 (기본값: localhost / root / wishlist1234 — 다르면 환경변수로)
#    SPRING_DATASOURCE_URL / SPRING_DATASOURCE_USERNAME / SPRING_DATASOURCE_PASSWORD

# 3) 실행
./gradlew bootRun
# http://localhost:8080
```

- 시드 관리자 계정: `admin / admin1234` (최초 1회 자동 생성)
- 테이블은 `ddl-auto=update` 로 자동 생성

## 8. 배포 (GCP VM + Cloudflare)

GCP Compute Engine VM(Ubuntu 22.04) 안에서 **직접 빌드·실행**(jar + systemd) → **Nginx** 리버스 프록시 → **Cloudflare** 도메인 + SSL.

- 단계별 전체 가이드: **[DEPLOY.md](DEPLOY.md)**
- 배포 URL: `http://<VM 외부 IP>` (도메인 연결 시 `https://<도메인>`)

> 업로드 이미지는 VM 로컬 `uploads/` 에 저장(영구 보존하지 않음 — 데모용).

## 9. 보안 메모

- 비밀번호 BCrypt 단방향 해시 저장
- 모든 상태 변경 요청에 CSRF 토큰(Thymeleaf 폼 자동 포함, 멀티파트 폼 포함)
- 리소스 접근 시 Service 계층에서 소유자 검증(타인 데이터 404)
- 운영 시 DB 접속정보·remember-me 키는 환경변수로 주입 권장
