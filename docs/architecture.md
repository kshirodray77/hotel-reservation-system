# Architecture

This document describes the high-level architecture of the hotel reservation system.

## Components

### Frontend (React + Vite + Tailwind)
A single-page application that consumes the REST API. It stores the JWT in `localStorage` and attaches it via an Axios interceptor. Routing is handled by React Router 6, with `<Protected>` route guards for authenticated and admin-only routes.

### Backend (Spring Boot 3)
A monolith composed of clearly separated layers:
- `controller/` REST endpoints (Spring MVC)
- `service/` business logic, transactional boundaries
- `repository/` Spring Data JPA
- `entity/` JPA entities mapped 1:1 to PostgreSQL tables
- `security/` JWT filter and utilities
- `kafka/` event producers and consumers
- `dto/` request and response payloads (Java records)

### Persistence (PostgreSQL 16)
Schema is defined in `db/init.sql` and matches the JPA entities. JPA's `ddl-auto=update` mode is enabled but the SQL file is the source of truth in production.

### Messaging (Apache Kafka)
Three topics:
- `booking-events`: produced by `BookingService` on lifecycle changes (CREATED, CONFIRMED, CANCELLED, COMPLETED)
- `payment-events`: produced by `PaymentService` after a checkout attempt (SUCCEEDED / FAILED / REFUNDED)
- `notification-events`: produced by the booking consumer; consumed by the email dispatcher

### Mail (Spring Mail)
`EmailService` is the final hop. If no SMTP is configured it logs the message instead, so the stack still works in dev.

## Data Flow: Successful Booking

```
[Customer] --POST /api/bookings--> BookingController
                                       |
                                       v
                                  BookingService
                                       |
                                       +--> save Booking (PENDING)
                                       +--> kafka:booking-events {CREATED}
                                                       |
                              +------------------------+------------------------+
                              v                                                 v
                  notification-service                                  analytics-service
                              |                                                 |
                              v                                                 v
                  kafka:notification-events --> EmailService          (counters / dashboards)

[Customer] --POST /api/payments/checkout--> PaymentController
                                                |
                                                v
                                          PaymentService
                                                |
                                                +--> save Payment (SUCCESS)
                                                +--> kafka:payment-events {SUCCEEDED}
                                                                |
                                                                v
                                                       PaymentEventConsumer
                                                                |
                                                                +--> Booking.status = CONFIRMED
                                                                +--> kafka:booking-events {CONFIRMED}
                                                                                |
                                                                                v
                                                                         (notification)
```

## Sequence: Cancellation

1. Customer calls `DELETE /api/bookings/{id}`.
2. `BookingService.cancel` flips status to `CANCELLED`, persists, and emits `BookingEvent{CANCELLED}`.
3. `BookingEventConsumer (notification-service)` translates that into a `NotificationEvent` and publishes to `notification-events`.
4. `NotificationEventConsumer (email-dispatcher)` invokes `EmailService.send`.

## Security model

- Public: room search, room detail, reviews list, login/register
- Customer (`ROLE_CUSTOMER`): create booking, list/cancel own bookings, pay, leave review
- Admin (`ROLE_ADMIN`): full access plus `/api/admin/**`

JWT signing uses HS256 with a configurable secret (`JWT_SECRET` env var). Tokens carry the user id and role as custom claims.

## Concurrency / consistency

Overlapping booking attempts on the same room are prevented by a JPQL existence check inside `BookingService.create`, executed within the transaction. For higher contention environments this should be backed by a database constraint (e.g. an `EXCLUDE` constraint on the date range column) or a distributed lock keyed by `roomId`.

Payments are idempotent at the booking level: at most one payment row per booking (enforced via `findByBookingId` check + DB unique transaction id).

## Extending the system

| Goal                    | Where to add it                                                                  |
| ----------------------- | -------------------------------------------------------------------------------- |
| Real card processing    | Replace mock logic in `PaymentService.checkout` with a Stripe / Adyen client      |
| SMS notifications       | Add a `SmsService` and a `case "SMS" -> ...` branch in `NotificationEventConsumer` |
| Multi-property          | Add `Hotel` entity, foreign key from `Room`, scope queries by hotel id            |
| Real-time availability  | Push booking events to a WebSocket hub; subscribe from the React frontend         |
| CDC / data warehouse    | Tap `booking-events` with a Kafka Connect sink (e.g. BigQuery, ClickHouse)        |
