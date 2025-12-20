# Docker Compose를 사용한 개발 환경 설정

## 사전 요구사항
- Docker
- Docker Compose

## 설정 방법

### 1. .env 파일 생성
프로젝트 루트에 `.env` 파일을 생성하고 다음 내용을 추가하세요:

```env
# MySQL 설정
MYSQL_ROOT_PASSWORD=rootpassword
MYSQL_DATABASE=spubgv2
MYSQL_USER=spubgv2_user
MYSQL_PASSWORD=spubgv2_password
MYSQL_PORT=3306

# Application 설정
APP_PORT=8080

# 커스텀 환경변수 예시
APP_NAME=spubgv2
APP_VERSION=1.0.0
APP_ENV=development
```

### 2. Docker Compose로 실행
```bash
# 서비스 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 서비스 중지
docker-compose down

# 볼륨까지 삭제 (데이터 초기화)
docker-compose down -v
```

### 3. 로컬에서 실행 (Docker 없이)
MySQL만 Docker로 실행하고 Spring Boot는 로컬에서 실행할 수도 있습니다:

```bash
# MySQL만 실행
docker-compose up -d mysql

# Spring Boot 실행 (로컬)
./gradlew bootRun
```

## 환경변수 사용 방법

### @Value 어노테이션 사용
```java
@Value("${app.name}")
private String appName;

@Value("${MYSQL_DATABASE:spubgv2}")  // 기본값 설정 가능
private String mysqlDatabase;
```

### Config 클래스 사용
`AppConfig` 클래스를 참고하세요.

### 테스트
환경변수가 제대로 로드되었는지 확인:
```bash
curl http://localhost:8080/api/env/test
```

## 포트
- Spring Boot: http://localhost:8080
- MySQL: localhost:3306

