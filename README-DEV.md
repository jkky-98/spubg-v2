# 개발 환경 설정 가이드

## 방법 1: 개발용 Docker Compose 사용 (권장)

소스 코드를 볼륨으로 마운트하여 코드 변경 시 자동으로 반영됩니다.

```bash
# 개발 모드로 실행
docker compose -f docker-compose.dev.yml up -d

# 로그 확인
docker compose -f docker-compose.dev.yml logs -f app

# 중지
docker compose -f docker-compose.dev.yml down
```

**장점:**
- 코드 변경 시 자동 반영 (Spring Boot DevTools 사용)
- MySQL도 함께 실행
- 환경이 일관됨

**단점:**
- 첫 빌드가 조금 느릴 수 있음

## 방법 2: 로컬에서 실행 (가장 빠름)

MySQL만 Docker로 실행하고, Spring Boot는 로컬에서 실행합니다.

```bash
# 1. MySQL만 실행
docker compose up -d mysql

# 2. 로컬에서 Spring Boot 실행
./gradlew bootRun
```

**장점:**
- 가장 빠른 개발 경험
- 즉시 코드 변경 반영
- IDE 디버깅 가능

**단점:**
- 로컬에 Java 21 필요

## 방법 3: 프로덕션용 Docker Compose (배포용)

```bash
# 프로덕션 모드로 실행 (코드 변경 시 재빌드 필요)
docker compose up -d --build
```

## 개발 팁

### 코드 변경 시
- **방법 1 사용 시**: 저장하면 자동으로 재시작됨 (Spring Boot DevTools)
- **방법 2 사용 시**: 저장하면 즉시 반영됨 (Hot Reload)

### 로그 확인
```bash
# 개발 모드
docker compose -f docker-compose.dev.yml logs -f app

# 프로덕션 모드
docker compose logs -f app
```

### 데이터베이스 초기화
```bash
docker compose down -v  # 볼륨까지 삭제
docker compose up -d
```

