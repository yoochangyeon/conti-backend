# CLAUDE.md

## Project: Conti Backend
예배팀 콘티(세트리스트) + 찬양 데이터베이스 협업 플랫폼 - Backend
Tech: Spring Boot 3.5 (Java 21), JPA + QueryDSL, MySQL, Flyway, OAuth2 (Kakao/Google) + JWT

## Database
- Host: localhost:3306 | DB: conti | User: root | Pass: (none)
- DB 스키마 관리: **Flyway** (직접 DDL 실행 금지, 마이그레이션 스크립트로 관리)
- 마이그레이션 위치: `src/main/resources/db/migration/`
- JPA ddl-auto: validate (Flyway가 스키마 생성, JPA는 검증만)
- 환경변수로 DB 설정 오버라이드 가능 (`.env.example` 참고)

## Commands
```bash
./gradlew build        # 빌드
./gradlew bootRun      # 실행 (localhost:8080)
./gradlew test         # 테스트
./gradlew clean build  # 클린 빌드
```

## Design Document
`docs/DESIGN.md`를 반드시 읽고 구현할 것 (ERD, API 스펙, 인증 플로우, 아키텍처)

## Package Structure
```
src/main/java/com/conti/
├── ContiApplication.java
├── global/           # 공통 인프라
│   ├── common/       # BaseEntity, ApiResponse
│   ├── config/       # SecurityConfig, JpaConfig, QueryDslConfig, WebConfig
│   ├── auth/jwt/     # JwtTokenProvider, JwtAuthenticationFilter
│   └── error/        # ErrorCode, BusinessException, GlobalExceptionHandler
├── domain/           # 비즈니스 도메인 (user, team, song, setlist)
│   └── {domain}/     # entity/ repository/ service/ controller/ dto/
└── infra/            # 외부 서비스 (s3/, oauth/)
```

## Coding Conventions
1. 엔티티: BaseEntity 상속 (id, createdAt, updatedAt). Lombok @Getter, @NoArgsConstructor(PROTECTED), @Builder
2. DTO: Java record. Request/Response 분리
3. API 응답: ApiResponse<T> 래핑. ApiResponse.ok(data), throw BusinessException(ErrorCode)
4. 인증: @LoginUser Long userId, @TeamAuth(roles={...})
5. 트랜잭션: 쓰기 서비스 메서드에 @Transactional
6. FK: DB 레벨 FK 없음, JPA 레벨에서만 관리
7. TDD: 테스트 먼저 작성 → 구현 → 리팩토링
8. DB 변경: 반드시 Flyway 마이그레이션 스크립트로 (V2__xxx.sql, V3__xxx.sql ...)

## Roles
ADMIN: 팀/곡/콘티/멤버 전체 CRUD | VIEWER: 조회만 | GUEST: 조회만 (팀 외부)
