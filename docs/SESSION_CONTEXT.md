# Conti Backend - 세션 컨텍스트

> 마지막 업데이트: 2026-02-16

## 프로젝트 상태

```
Phase: Phase 2 핵심 확장 거의 완료 🔶
Next:  FCM 실 연동 (Step 5) → 배포 인프라 (Step 8)
```

## 현재 구현 현황

### 백엔드 (Spring Boot) — 핵심 기능 구현 완료

| 영역 | 구현 현황 |
|------|----------|
| **컨트롤러** | 17개 |
| **API 엔드포인트** | 82개 |
| **JPA 엔티티** | 29개 |
| **서비스 클래스** | 15개 |
| **리포지토리** | 23개 (JPA 20 + QueryDSL 3) |
| **DTO** | 60개 |
| **DB 마이그레이션** | V1~V7 |
| **테스트 클래스** | 23개 (Unit 10 + E2E 13) |
| **테스트 케이스** | 186+ |

### 구현된 도메인

- **User**: 인증 (OAuth2 카카오/구글 + JWT), 프로필, 팀 목록
- **Team**: CRUD, 멤버 관리, 초대코드, 포지션, 공지사항, 세분화 권한 (GUEST/VIEWER/SCHEDULER/EDITOR/ADMIN)
- **Song**: CRUD, 태그(자동완성 API), 파일(S3+URL), 섹션, 사용이력, 고급 검색, 다중 편곡(CRUD), 사용 통계
- **Setlist**: CRUD, 아이템 관리, 순서변경, 비곡 항목, 복사, 템플릿(CRUD), 색상, 서비스구간, 노트
- **Schedule**: 벌크 배정, 수락/거절, 충돌감지, 매트릭스뷰, 셀프사인업, 캘린더 구독(iCal)
- **Blockout**: CRUD, 기간 조회
- **Notification**: 알림 목록/읽음처리/디바이스토큰 (FCM은 Mock)

### 미구현 사항

| 항목 | 상태 |
|------|------|
| FCM 실제 푸시 발송 | MockPushNotificationService → 실 FCM 교체 필요 |
| 배포 인프라 | AWS EC2/ECS + RDS 미구성 |
| CI/CD | GitHub Actions 미구성 |

## DB 마이그레이션 현황

| 버전 | 내용 | 상태 |
|------|------|:----:|
| V1 | 초기 스키마 (users, teams, team_members, songs, song_files, setlists, setlist_items) | ✅ |
| V2 | 곡 섹션 (song_sections, section_type) | ✅ |
| V3 | 스케줄링 + 포지션 + 비곡 항목 | ✅ |
| V4 | 세트리스트 강화 (color, service_phase, templates, notes) | ✅ |
| V5 | 곡 편곡 (song_arrangements) | ✅ |
| V6 | 알림 시스템 (notifications, device_tokens) | ✅ |
| V7 | 팀 강화 (team_notices) | ✅ |

## 주요 기술 결정 사항

| 결정 | 선택 | 이유 |
|------|------|------|
| 백엔드 | Spring Boot 3.5 (Java 25) | 취업 목표 + 엔터프라이즈 생태계 |
| ORM | JPA + QueryDSL | 동적 검색 쿼리 |
| DB | MySQL 8.0+ | Flyway 마이그레이션 |
| FK | JPA 레벨만 | 유연한 데이터 관리 |
| 인증 | OAuth2 + JWT | Access 1h, Refresh 14d |
| 파일 | AWS S3 | 악보 파일 |
| 권한 | 계층형 Enum | GUEST(0) < VIEWER(1) < SCHEDULER(2) < EDITOR(3) < ADMIN(4) |

---

*이 파일은 세션 간 컨텍스트 유지를 위해 작성되었습니다.*
