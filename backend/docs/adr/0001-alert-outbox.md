# ADR-0001: 알림 처리 방식은 DB Outbox + Dispatcher로 간다

## Context

조건을 만족했을 때 “알림을 보낸다”는 동작은 비동기/재시도/중복방지가 필요하다. 하지만 포트폴리오 MVP에서 Kafka/RabbitMQ를 도입하면 운영 부담이 커진다.

## Decision

- 알림을 즉시 외부로 보내지 않고, 먼저 `alert_events` 테이블에 **PENDING**으로 적재한다(Outbox).
- 별도 스케줄러(Dispatcher)가 PENDING을 가져가 “발송 처리” 후 **SENT**로 변경한다(데모는 로그).
- `(subscription_id, snapshot_id)` 유니크로 중복 이벤트를 막는다.

## Consequences

- 장점: 인프라 의존도가 낮고, 장애/재시도/재처리 로직을 DB로 단순화할 수 있다.
- 단점: 고트래픽/대규모 확장에는 메시지 브로커 대비 한계가 있다(후속 개선 포인트).

