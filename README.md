# Conti Backend

> 예배팀 콘티(세트리스트) + 찬양 데이터베이스 협업 플랫폼 - Backend API

## 기술 스택

| 구분 | 기술 |
|------|------|
| Framework | Spring Boot 3.5 (Java 21) |
| ORM | Spring Data JPA + QueryDSL |
| Database | MySQL |
| Migration | Flyway |
| Auth | OAuth2 (Kakao, Google) + JWT |
| Infra | AWS (EC2, RDS, S3) |

## 시작하기

### 사전 준비
- Java 21+
- MySQL 8.0+
- DB 생성: `CREATE DATABASE conti;`

### 환경변수 설정
`.env.example`을 복사하여 `.env` 파일을 생성하고 값을 설정하세요:
```bash
cp .env.example .env
```

### 실행
```bash
./gradlew bootRun
```
- 서버: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html

## 문서
- [시스템 설계 문서](./docs/DESIGN.md)
- [세션 컨텍스트](./docs/SESSION_CONTEXT.md)

## 라이선스
MIT License
