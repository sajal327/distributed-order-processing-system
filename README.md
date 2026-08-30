# Distributed Order Processing System

Production-style portfolio project based on the supplied master specification. The implementation is intentionally incremental: infrastructure and service skeleton first, then persistence, messaging, transactional outbox, consumers, security, testing, and optimization.

## Current stage
- Java 21 / Spring Boot 3.x Maven multi-module monorepo
- Docker Compose with MySQL 8.4 and RabbitMQ Management
- Separate service boundaries for order, inventory, payment, notification, and API gateway
- Actuator health/info/metrics baseline
- Environment-variable configuration via `.env`

## Run
```bash
cp .env.example .env
docker compose up --build
```

RabbitMQ Management: `http://localhost:15672`

## Architecture decision
The target architecture uses separate Spring Boot applications with RabbitMQ as the asynchronous backbone. Implementation will be validated stage-by-stage rather than generating an unverified full system in one pass.

## Roadmap
1. Project skeleton and Docker infrastructure
2. MySQL schema + Flyway
3. Product/category/order APIs
4. RabbitMQ topology
5. Transactional outbox
6. Inventory reservation + concurrency controls
7. Simulated payment
8. Notifications
9. JWT security
10. Retry/DLQ + idempotency
11. Pagination, indexes and SQL optimization
12. Unit/integration/Testcontainers tests
13. OpenAPI and complete documentation
14. Acceptance-flow verification
