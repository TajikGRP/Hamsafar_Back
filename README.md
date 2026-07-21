# Hamsafar Backend

Spring Boot 4.1, Java 21, JPA, MySQL 기반의 로컬 개발용 Hamsafar API 서버입니다.

## 요구사항

- Java 21
- MySQL 8.x
- MySQL CLI(데모 데이터 입력 시)

## 로컬 데이터베이스

기본 연결값은 다음과 같습니다.

| 항목 | 기본값 |
|---|---|
| Host | `localhost` |
| Port | `3306` |
| Database | `hamsafar` |
| Username | `root` |
| Password | 빈 값 |

MySQL 계정에 데이터베이스 생성 권한이 있으면 JDBC의
`createDatabaseIfNotExist=true` 옵션으로 `hamsafar` 데이터베이스가 생성됩니다.

환경변수를 사용할 경우 예시 파일을 복사합니다.

```bash
cp .env.example .env
set -a
source .env
set +a
```

Spring Boot는 `.env`를 자동으로 읽지 않으므로 터미널에서 위와 같이 로드하거나
IDE Run Configuration에 동일한 값을 입력해야 합니다.

## 실행

```bash
./gradlew bootRun
```

기본 실행 주소는 `http://127.0.0.1:8080`입니다.

상태 확인:

```bash
```

JPA의 기본 로컬 설정은 `ddl-auto=update`입니다. 운영 환경에서 그대로 사용하면
의도하지 않은 스키마 변경이 생길 수 있으므로 로컬 개발에만 사용해야 합니다.

## 데모 데이터

애플리케이션을 한 번 실행해 JPA가 테이블을 만든 다음 MySQL 시드를 실행합니다.

비밀번호가 없는 로컬 root 계정:

```bash
mysql -h localhost -P 3306 -u root hamsafar < scripts/seed_demo_mysql.sql
```

비밀번호가 있는 계정:

```bash
mysql -h localhost -P 3306 -u root -p hamsafar < scripts/seed_demo_mysql.sql
```

시드는 반복 실행할 수 있으며 다음 데이터를 준비합니다.

- 기사 1명과 차량 1대
- 승객 3명
- 향후 일정 2건
- `APPROVED`, `PENDING`, `WAITLISTED` 예약 각 1건

## 테스트와 빌드

테스트는 MySQL 호환 모드의 인메모리 H2를 사용합니다.

```bash
./gradlew clean build
```

실행 JAR은 `build/libs/app.jar`에 생성됩니다.

## 주요 API 영역

- 사용자 및 기사 관리
- 차량 관리
- 일정 생성 및 상세 조회
- 출발지·목적지·날짜·인원수 기반 일정 검색
- 페이지네이션과 정렬
- 예약, 대기 예약, 취소 후 FIFO 자동 승급
