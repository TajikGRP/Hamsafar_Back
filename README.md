# Hamsafar Backend

Spring Boot 4.1 / Java 21 / JPA 기반 Hamsafar API 서버입니다. 데이터베이스는 Supabase PostgreSQL, 애플리케이션 런타임은 Render Docker Web Service를 기준으로 구성되어 있습니다.

## 1. 배포 구성

```text
Client
  -> Render HTTPS Load Balancer
  -> Spring Boot container (0.0.0.0:$PORT)
  -> HikariCP (max 5 connections per instance)
  -> Supavisor session pooler :5432
  -> Supabase PostgreSQL / app schema
```

핵심 선택은 다음과 같습니다.

- Render는 JVM 네이티브 런타임을 제공하지 않으므로 Docker를 사용합니다.
- Render에서 Supabase로 연결할 때는 장기 실행되는 Spring/JPA 서버에 맞는 **Supavisor session mode(포트 5432)** 를 기본으로 사용합니다.
- Hibernate가 운영 스키마를 임의 변경하지 않도록 `ddl-auto=validate`를 사용합니다.
- 스키마 변경은 Flyway 마이그레이션으로 관리합니다.
- 애플리케이션 테이블은 Supabase Data API가 기본 노출하는 `public`이 아니라 `app` 스키마에 둡니다.
- `/actuator/health`는 HTTP 서버와 DB 연결을 함께 확인하는 Render Health Check 경로입니다.

## 2. 필요한 도구와 의존성

### 로컬 도구

- Java 21
- Git
- Docker: Render와 동일한 이미지의 로컬 검증에 사용
- 선택: PostgreSQL `psql`
- 선택: Render CLI 2.7+

### 주요 Gradle 의존성

| 목적 | 의존성 |
|---|---|
| ORM | `spring-boot-starter-data-jpa` |
| PostgreSQL JDBC | `org.postgresql:postgresql` |
| DB 마이그레이션 | `flyway-core`, `flyway-database-postgresql` |
| 배포 상태 확인 | `spring-boot-starter-actuator` |
| 로컬 단위 테스트 | H2 test runtime |

MySQL Connector는 제거되었습니다. 기존 MySQL 데이터가 있다면 Supabase 연결만으로 데이터가 이전되지 않습니다. 별도의 export/import와 데이터 검증이 필요합니다.

## 3. Supabase 프로젝트 생성

1. [Supabase Dashboard](https://supabase.com/dashboard)에서 `New project`를 선택합니다.
2. 조직, 프로젝트명, 강한 Database Password를 설정합니다.
3. Hamsafar 운영 구성은 Supabase `Northeast Asia (Seoul, ap-northeast-2)`, Render `Singapore`를 사용합니다.
   - Supabase는 Seoul을 지원하지만 Render는 Seoul 리전을 제공하지 않으므로 가장 가까운 Render 지원 리전인 Singapore를 선택합니다.
   - 현재 `render.yaml`은 `singapore`로 고정되어 있습니다.
   - 두 서비스 간 DB 통신은 외부 리전 통신이므로 배포 후 실제 쿼리 지연시간을 측정해야 합니다.
   - Render Region은 서비스 생성 후 직접 변경할 수 없으므로 최초 생성 시 반드시 확인하십시오.
4. 프로젝트가 준비되면 상단의 `Connect`를 엽니다.
5. `Session pooler`를 선택하고 Host, Port, Database, User 값을 복사합니다.

### JPA에서 Supabase API Key가 필요한가?

필요하지 않습니다. 이 프로젝트는 Supabase Data API가 아니라 PostgreSQL JDBC로 DB에 직접 연결합니다.

필수 값은 다음 세 가지입니다.

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

`SUPABASE_URL`, publishable key, secret/service-role key는 Supabase Auth, Storage 또는 Data API를 추가할 때만 필요합니다. 특히 secret/service-role key를 브라우저나 모바일 앱에 노출하면 안 됩니다.

## 4. 데이터베이스 연결 문자열

### 권장: Supavisor session mode

Supabase Dashboard의 `Connect -> Session pooler` 값을 사용합니다. Host와 프로젝트 ref를 추측하지 말고 Dashboard에서 그대로 복사하십시오.

```dotenv
DB_URL="jdbc:postgresql://aws-0-<REGION>.pooler.supabase.com:5432/postgres?sslmode=require&ApplicationName=hamsafar"
DB_USERNAME="postgres.<PROJECT_REF>"
DB_PASSWORD="<DATABASE_PASSWORD>"
DB_SCHEMA=app
```

포트 `5432`는 session mode입니다. 장기 실행되는 Spring Boot + HikariCP에 가장 무난합니다.

### 더 강한 인증서 검증: `verify-full`

`sslmode=require`는 통신을 암호화하지만 서버 인증서와 호스트 이름을 검증하지 않습니다. 운영에서는 Supabase Dashboard의 `Database Settings -> SSL Configuration`에서 CA 인증서를 다운로드하고 `verify-full`을 권장합니다.

Render에서 인증서를 Secret File `/etc/secrets/supabase-ca.crt`로 등록한 뒤 URL을 다음과 같이 설정합니다.

```dotenv
DB_URL="jdbc:postgresql://<SESSION_POOLER_HOST>:5432/postgres?sslmode=verify-full&sslrootcert=/etc/secrets/supabase-ca.crt&ApplicationName=hamsafar"
```

### 사용하지 않는 것이 좋은 연결 방식

| 연결 방식 | 판단 |
|---|---|
| Direct `db.<ref>.supabase.co:5432` | 장기 서버에는 적합하지만 기본적으로 IPv6 endpoint입니다. Render 조합에서는 IPv4 Supavisor session pooler가 더 안전합니다. |
| Transaction pooler `:6543` | 서버리스/짧은 연결용입니다. prepared statement와 세션 기능 제약 때문에 JPA 기본값으로 부적합합니다. |
| JDBC URL에 비밀번호 포함 | 특수문자 URL encoding 오류와 로그 노출 위험이 있습니다. 비밀번호는 `DB_PASSWORD`로 분리하십시오. |

부득이하게 transaction mode를 사용한다면 PostgreSQL JDBC URL에 `prepareThreshold=0`을 추가하고 충분한 회귀 테스트를 해야 합니다. 단순히 포트만 `6543`으로 변경해서는 안 됩니다.

## 5. 로컬 환경 변수 설정

```bash
cp .env.example .env
```

`.env`의 placeholder를 Supabase Dashboard 값으로 바꾼 다음 현재 셸에 로드합니다.

```bash
set -a
source .env
set +a
```

Spring Boot는 `.env` 파일을 자동으로 읽지 않습니다. IDE를 사용할 경우 Run Configuration에도 동일한 환경 변수를 넣어야 합니다.

### 환경 변수 목록

| 변수 | 필수 | 설명 |
|---|---:|---|
| `DB_URL` | O | PostgreSQL JDBC URL. Render에서는 session pooler 권장 |
| `DB_USERNAME` | O | 보통 `postgres.<PROJECT_REF>` |
| `DB_PASSWORD` | O | Supabase Database Password |
| `DB_SCHEMA` | O | 기본값 `app` |
| `FLYWAY_ENABLED` | O | 운영에서 `true` |
| `DDL_AUTO` | O | 운영에서 반드시 `validate` |
| `DB_POOL_MAX_SIZE` | O | 기본값 5. 총 연결 수는 인스턴스 수를 곱해 계산 |
| `PORT` | 로컬만 | 로컬 기본값 8080. Render가 자동 주입 |

`.env`는 Git에서 제외되어 있습니다. 실제 비밀번호를 `.env.example`, `render.yaml`, README 또는 소스에 넣지 마십시오.

## 6. 스키마와 Flyway

초기 스키마는 다음 파일에 있습니다.

```text
src/main/resources/db/migration/V1__create_hamsafar_schema.sql
```

애플리케이션이 시작될 때 Flyway가 다음 작업을 수행합니다.

1. `app` 스키마 생성
2. 사용자, 기사, 차량, 운행 일정, 예약 테이블 생성
3. FK, unique, check constraint와 조회 인덱스 생성
4. 테이블 RLS 활성화
5. Hibernate가 엔티티와 스키마 일치 여부 검증

`app` 스키마를 Supabase의 Exposed Schemas에 추가하지 마십시오. 현재는 `anon`/`authenticated` 정책이 없기 때문에 Data API로 이 테이블들을 직접 사용하는 구조가 아닙니다.

향후 스키마 변경 시 기존 `V1`을 수정하지 말고 다음 버전 파일을 추가합니다.

```text
V2__add_ride_waitlist.sql
V3__add_driver_verification.sql
```

`V2`는 대기 예약 상태, 취소 상태, 동일 사용자·동일 일정의 활성 예약 중복 방지 인덱스를 추가합니다. 기존 운영 DB에 활성 중복 예약이 있으면 unique index 생성이 실패하므로 배포 전에 아래 쿼리가 0건인지 확인하십시오.

```sql
select schedule_id, user_id, count(*)
from app.ride_joins
where status in ('PENDING', 'WAITLISTED', 'APPROVED')
group by schedule_id, user_id
having count(*) > 1;
```

### 기존 MySQL 데이터가 있는 경우

다음 순서를 지키십시오.

1. MySQL 전체 백업
2. Supabase의 별도 staging 프로젝트에 스키마 적용
3. 데이터 타입과 enum 값을 변환해 import
4. 각 테이블 row count, FK orphan, identity sequence 검증
5. 읽기/쓰기 smoke test 수행
6. 검증 후에만 운영 Render 서비스 연결

현재 Flyway 파일은 **스키마를 생성할 뿐 MySQL 데이터를 이전하지 않습니다.**

## 7. 연결 확인

### 최소 연결·SSL 테스트 코드

`DatabaseConnectionSmokeTest`는 실제 PostgreSQL에 `SELECT 1`을 실행하고 현재 연결이 SSL인지 `pg_stat_ssl`로 확인합니다.

```bash
./gradlew dbConnectionTest
```

성공 조건:

- PostgreSQL 접속 성공
- `SELECT 1` 결과가 1
- 현재 세션의 `ssl=true`

이 테스트는 `integration` 태그로 분리되어 일반 `./gradlew test`에서는 실행되지 않습니다.

### 애플리케이션 실행

```bash
./gradlew bootRun
```

다른 터미널에서 확인합니다.

```bash
curl -fsS http://localhost:8080/actuator/health
```

정상 응답:

```json
{"status":"UP"}
```

DB가 연결되지 않으면 Health Check는 `DOWN`이 되고 Render가 잘못된 배포를 정상 상태로 표시하지 않습니다.

## 8. Docker 빌드와 실행

### Render에서 실제로 사용되는 명령

Render의 Java/JVM 배포는 Docker이므로 Dashboard의 Native Build/Start Command는 사용하지 않습니다.

| 단계 | 명령 |
|---|---|
| Build | Dockerfile 내부 `./gradlew clean bootJar -x test --no-daemon` |
| Start | Dockerfile `ENTRYPOINT ["java", "-jar", "/app/app.jar"]` |
| Port | Spring `server.port=${PORT:8080}`, Render 기본 `PORT=10000` |

로컬 이미지 검증:

```bash
docker build -t hamsafar-back:local .
docker run --rm --env-file .env -p 8080:8080 hamsafar-back:local
```

컨테이너는 root가 아닌 `spring` 사용자로 실행됩니다.

## 9. Render 배포

저장소는 다음 Git remote를 사용합니다.

```text
https://github.com/TazikGRP/Hamsafar_Back
```

### 9.1 Blueprint 검증

Render CLI 2.7 이상이 설치되어 있다면 다음 명령을 실행합니다.

```bash
render blueprints validate render.yaml
```

현재 Render CLI가 없다면 설치 후 검증하거나, Render Dashboard에서 Blueprint 생성 시 검증 결과를 확인합니다.

### 9.2 Git 반영

```bash
git add build.gradle src Dockerfile .dockerignore .env.example render.yaml README.md
git commit -m "Configure Supabase PostgreSQL and Render deployment"
git push origin main
```

실제 secret은 절대 commit하지 않습니다.

### 9.3 Blueprint 생성

`render.yaml`이 원격 저장소에 push된 것을 확인한 다음 아래 링크를 엽니다.

[Render Blueprint 생성](https://dashboard.render.com/blueprint/new?repo=https://github.com/TazikGRP/Hamsafar_Back)

설정값을 검토하고 `sync: false`로 표시된 다음 secret을 입력합니다.

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

`render.yaml`은 다음 값을 이미 설정합니다.

```text
SPRING_PROFILES_ACTIVE=prod
DB_SCHEMA=app
FLYWAY_ENABLED=true
DDL_AUTO=validate
DB_POOL_MAX_SIZE=5
DB_POOL_MIN_IDLE=0
TZ=Asia/Dushanbe
```

Render는 `PORT`를 자동 주입하므로 직접 고정하지 않습니다.
운행 날짜 검색과 출발 마감 판단이 Render의 기본 UTC가 아니라 타지키스탄 현지 시각을 사용하도록 JVM timezone도 `Asia/Dushanbe`로 고정되어 있습니다.
Free Plan의 512MB 메모리에서 JVM heap이 컨테이너 전체 메모리를 잠식하지 않도록 `MaxRAMPercentage=60`으로 제한합니다.

### 9.4 최초 배포 확인

1. Build 로그에서 Gradle `bootJar` 성공 확인
2. Runtime 로그에서 Hikari와 Flyway migration 성공 확인
3. 배포 상태가 `Live`인지 확인
4. 아래 URL 호출

```bash
curl -fsS https://<SERVICE_NAME>.onrender.com/actuator/health
```

5. Supabase SQL Editor에서 테이블 확인

```sql
select table_schema, table_name
from information_schema.tables
where table_schema = 'app'
order by table_name;
```

6. SSL 연결 확인

```sql
select ssl, usename, application_name, client_addr
from pg_stat_ssl
join pg_stat_activity using (pid)
where application_name = 'hamsafar';
```

## 10. MVP 운행 일정·예약 API

### 일정 조회와 검색

| 기능 | Method / Path | 주요 파라미터 |
|---|---|---|
| 일정 상세 | `GET /api/rides/schedules/{scheduleId}` | 경로 ID |
| 일정 검색 | `GET /api/rides/schedules` | `departure`, `destination`, `date`, `passengerCount` |
| 기사 일정 목록 | `GET /api/rides/drivers/{driverId}/schedules` | 페이징·정렬 파라미터 |

검색 예시:

```bash
curl "http://localhost:8080/api/rides/schedules?departure=Dushanbe&destination=Khujand&date=2026-07-25&passengerCount=2&page=0&size=20&sortBy=meetingAt&sortDirection=asc"
```

- 출발지·목적지는 주소와 장소명에 대한 대소문자 무시 부분 검색입니다.
- 날짜는 서버가 사용하는 현지 날짜의 00:00 이상, 다음 날 00:00 미만으로 조회합니다.
- `passengerCount`를 넣으면 `PENDING + APPROVED`가 점유한 좌석을 제외하고 충분한 일정만 반환합니다.
- 공개 검색은 미래의 `OPEN` 일정과 활성 차량만 반환합니다.
- 일정 정렬 필드: `meetingAt`, `fareAmount`, `createdAt`
- 예약 목록 정렬 필드: `createdAt`, `status`, `totalFare`
- `page`는 0부터 시작하고 `size`는 최대 100입니다.
- 목록 응답은 `content`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last`, `empty`를 포함합니다.

### 대기 예약과 자동 승급

| 기능 | Method / Path | 권한 식별 파라미터 |
|---|---|---|
| 예약 요청 | `POST /api/rides/schedules/{scheduleId}/joins` | `userId` |
| 승객 예약 목록 | `GET /api/rides/users/{userId}/joins` | 페이징·정렬 파라미터 |
| 일정 예약 목록 | `GET /api/rides/schedules/{scheduleId}/joins` | `driverId` + 페이징·정렬 |
| 기사 승인/거절 | `PATCH /api/rides/joins/{joinId}/approve`, `/reject` | `driverId` |
| 승객 취소 | `PATCH /api/rides/joins/{joinId}/cancel` | `userId` |

상태 전이 정책:

```text
좌석·수하물 여유 있음: PENDING -> APPROVED 또는 REJECTED/CANCELLED
좌석·수하물 초과:     WAITLISTED -> PENDING -> APPROVED
승객 직접 취소:       PENDING/WAITLISTED/APPROVED -> CANCELLED
```

- `PENDING`과 `APPROVED`가 좌석 및 수하물 용량을 점유합니다.
- 초과 요청은 거절하지 않고 `WAITLISTED`로 저장하며 응답에 `waitlistPosition`을 반환합니다.
- 일정의 총 좌석 또는 총 수하물 용량보다 큰 단일 요청은 영구 대기가 되므로 `400`으로 거절합니다.
- 점유 예약이 취소 또는 거절되면 `joinedAt`, `id` 순 FIFO로 승급합니다.
- 앞 순번이 남은 좌석 또는 수하물에 맞지 않으면 뒤 순번을 건너뛰지 않습니다.
- 자동 승급 상태는 `APPROVED`가 아니라 `PENDING`입니다. 기사 승인 절차를 우회하지 않습니다.
- 일정 행에 PostgreSQL 비관적 잠금을 적용해 동일 일정의 동시 요청이 용량을 초과하지 않도록 직렬화합니다.
- 같은 사용자는 동일 일정에 `PENDING`, `WAITLISTED`, `APPROVED` 예약을 하나만 가질 수 있습니다.

## 11. 자주 발생하는 오류

### `Connection refused` 또는 timeout

- Supabase 프로젝트가 pause/inactive 상태인지 확인
- Direct IPv6 URL이 아니라 Session pooler URL인지 확인
- Host와 region 문자열을 Dashboard에서 다시 복사
- Supabase Network Restrictions에 Render outbound IP 대역이 허용됐는지 확인

### `FATAL: password authentication failed`

- `DB_USERNAME`이 session pooler 형식인 `postgres.<PROJECT_REF>`인지 확인
- API key가 아니라 Database Password를 사용했는지 확인
- 비밀번호를 재설정했다면 Render secret도 갱신

### `Tenant or user not found`

- pooler host의 region과 프로젝트 region 불일치 여부 확인
- username에 프로젝트 ref가 빠졌는지 확인
- Direct URL의 username `postgres`와 Pooler username을 혼용하지 않았는지 확인

### `prepared statement ... does not exist`

- transaction pooler 6543을 사용 중인지 확인
- 기본 해결책은 session pooler 5432로 변경
- transaction mode를 유지해야 하면 `prepareThreshold=0` 추가 후 전체 회귀 테스트

### `too many connections`

총 최대 연결 수는 대략 다음과 같습니다.

```text
Render instance 수 × DB_POOL_MAX_SIZE
+ SQL Editor, Supabase 내부 서비스, migration 연결
```

- Free/소형 DB에서는 인스턴스당 5 이하로 시작
- `minimum-idle=0` 유지
- 무조건 pool size를 늘리지 말고 느린 쿼리와 connection leak부터 확인
- Supavisor와 별도 PgBouncer를 목적 없이 중첩하지 않기

### Render `No open ports detected`

- `PORT`를 임의로 8080으로 고정하지 않았는지 확인
- `server.address=0.0.0.0` 유지
- Dockerfile의 `ENTRYPOINT` 존재 여부 확인

### Health Check 503

- `/actuator/health`가 DB까지 검사하므로 DB 장애 시 503이 정상입니다.
- Runtime 로그에서 Flyway, Hikari, 인증, SSL 오류 중 첫 번째 원인을 먼저 해결하십시오.

### Flyway validation 실패

- 이미 적용된 migration 파일을 수정하지 않았는지 확인
- 운영 DB에서 `ddl-auto=update/create`를 사용하지 않기
- 수동 테이블 수정 대신 새 `V<n>__description.sql` 추가

## 12. 보안 체크리스트

- [ ] `.env`가 Git에 포함되지 않았는가
- [ ] Render secret에만 DB 자격증명이 저장됐는가
- [ ] 운영 `DDL_AUTO=validate`인가
- [ ] DB 연결이 최소 `sslmode=require`인가
- [ ] 가능하면 CA Secret File과 `verify-full`을 적용했는가
- [ ] `app` 스키마가 Supabase Data API Exposed Schemas에 포함되지 않았는가
- [ ] secret/service-role key를 클라이언트에 노출하지 않았는가
- [ ] Render와 Supabase region 간 지연시간을 확인했는가
- [ ] 인스턴스 수를 고려해 Hikari 최대 연결 수를 계산했는가
- [ ] 기존 MySQL 데이터의 백업·이관·검증이 완료됐는가

## 13. 공식 문서

- [Supabase: Connect to Postgres](https://supabase.com/docs/guides/database/connecting-to-postgres)
- [Supabase: SSL Enforcement](https://supabase.com/docs/guides/platform/ssl-enforcement)
- [Render: Docker](https://render.com/docs/docker)
- [Render: Web Service Port Binding](https://render.com/docs/web-services#port-binding)
- [Render: Blueprint YAML](https://render.com/docs/blueprint-spec)
