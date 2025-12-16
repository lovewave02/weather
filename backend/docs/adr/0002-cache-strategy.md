# ADR-0002: 조회 캐시는 Spring Cache + Redis TTL로 간다

## Context

현재 날씨 조회는 “핫키(서울/부산 등)”가 생기기 쉽고, 외부 API/DB 부하를 줄여야 한다.

## Decision

- Cache-Aside 패턴으로 구현한다(서비스 메서드에 `@Cacheable` 적용).
- 캐시 TTL은 짧게(예: 60초) 가져가고, 수집 작업이 최신 스냅샷을 적재하면 해당 지역 키를 evict한다.
- 테스트 환경에서는 `spring.cache.type=simple`로 Redis 없이도 검증 가능하게 한다.

## Consequences

- 장점: 구현 단순, 체감 성능 개선, Redis 장애 시에도 DB로 폴백 가능.
- 단점: TTL/evict 타이밍에 따라 약간의 stale 데이터 가능(요구사항에 따라 조정).

