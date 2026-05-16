# Hotel Reservation System

A full-stack hotel reservation system built with **Java 17 + Spring Boot**, **Apache Kafka**, **PostgreSQL**, **React 18**, and **Tailwind CSS**.

[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen)](https://spring.io/projects/spring-boot)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-3.6-black)](https://kafka.apache.org/)
[![React](https://img.shields.io/badge/React-18-blue)](https://reactjs.org/)
[![Tailwind](https://img.shields.io/badge/Tailwind-3.4-38B2AC)](https://tailwindcss.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

## Features

- **Room search & booking** — search by date range, capacity, and price; real-time availability
- **JWT authentication** — secure login/register with role-based access (CUSTOMER / ADMIN)
- **Booking lifecycle** — create, confirm, cancel; status updates broadcast over Kafka
- **Payments** — checkout flow with mock payment processor (extensible to Stripe)
- **Email notifications** — booking confirmation, cancellation, and reminders via Kafka consumers
- **Reviews & ratings** — guests can review stays; aggregate ratings shown on rooms
- **Admin dashboard** — manage rooms, view bookings, revenue reports, occupancy stats
- **Event-driven architecture** — Kafka topics for `booking-events`, `payment-events`, `notification-events`
- **Modern UI** — responsive React SPA with Tailwind CSS

## Architecture

```
┌──────────────┐      ┌──────────────────┐      ┌──────────────┐
│  React + TW  │ ───► │  Spring Boot API │ ───► │  PostgreSQL  │
│  (Frontend)  │ ◄─── │     (Backend)    │ ◄─── │              │
└──────────────┘      └──────┬───────────┘      └──────────────┘
                             │
                             ▼
                      ┌──────────────┐
                      │ Apache Kafka │
                      └──────┬───────┘
                             │
                  ┌──────────┴──────────┐
                  ▼                     ▼
          ┌──────────────┐      ┌──────────────┐
          │ Notification │      │   Analytics  │
          │  Consumer    │      │   Consumer   │
          └──────────────┘      └──────────────┘
```

See [docs/architecture.md](docs/architecture.md) for a deeper dive.

## Tech Stack

| Layer            | Technology                              |
| ---------------- | --------------------------------------- |
| Backend          | Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA |
| Messaging        | Apache Kafka 3.6 (Spring Kafka)         |
| Database         | PostgreSQL 16                           |
| Auth             | JWT (jjwt 0.12)                         |
| Mail             | Spring Mail (Jakarta Mail)              |
| Frontend         | React 18, Vite, React Router 6          |
| Styling          | Tailwind CSS 3.4                        |
| HTTP client      | Axios                                   |
| Build / Deploy   | Maven, Docker, Docker Compose           |

## Project Structure

```
hotel-reservation-system/
├── backend/                  # Spring Boot application
│   ├── src/main/java/com/hotel/reservation/
│   │   ├── config/           # Kafka, Security, CORS, OpenAPI configs
│   │   ├── controller/       # REST controllers
│   │   ├── dto/              # Request/response DTOs
│   │   ├── entity/           # JPA entities
│   │   ├── exception/        # Global exception handler
│   │   ├── kafka/            # Producers, consumers, event models
│   │   ├── repository/       # Spring Data JPA repositories
│   │   ├── security/         # JWT filter & utilities
│   │   └── service/          # Business logic
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── pom.xml
│   └── Dockerfile
├── frontend/                 # React + Vite + Tailwind
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── context/
│   │   ├── services/
│   │   └── hooks/
│   ├── package.json
│   ├── tailwind.config.js
│   ├── vite.config.js
│   └── Dockerfile
├── db/
│   └── init.sql              # Schema + seed data
├── docs/
│   ├── architecture.md
│   └── api.md
├── docker-compose.yml
└── README.md
```

## Quick Start (Docker Compose)

```bash
git clone https://github.com/kshirodray77/hotel-reservation-system.git
cd hotel-reservation-system
docker compose up --build
```

Brings up PostgreSQL (5432), Zookeeper (2181), Kafka (9092), Backend (8080), Frontend (5173).

Open <http://localhost:5173>.

### Default credentials (seeded)

| Role     | Email                  | Password   |
| -------- | ---------------------- | ---------- |
| Admin    | admin@hotel.com        | admin123   |
| Customer | jane@example.com       | password   |

## Running Locally (without Docker)

### Prerequisites
- Java 17+, Maven 3.9+
- Node 20+, npm 10+
- PostgreSQL 16 on `localhost:5432`
- Kafka 3.6 on `localhost:9092`

### Backend

```bash
cd backend
mvn spring-boot:run
```

API at <http://localhost:8080/api>. Swagger UI at <http://localhost:8080/swagger-ui.html>.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend at <http://localhost:5173>.

## Kafka Topics

| Topic                   | Producer            | Consumers                              | Payload             |
| ----------------------- | ------------------- | -------------------------------------- | ------------------- |
| `booking-events`        | BookingService      | NotificationConsumer, AnalyticsConsumer | `BookingEvent`      |
| `payment-events`        | PaymentService      | BookingService, NotificationConsumer    | `PaymentEvent`      |
| `notification-events`   | NotificationService | EmailDispatcher                        | `NotificationEvent` |

## API Highlights

| Method | Path                          | Auth      | Description                |
| ------ | ----------------------------- | --------- | -------------------------- |
| POST   | `/api/auth/register`          | Public    | Register new user          |
| POST   | `/api/auth/login`             | Public    | Authenticate, return JWT   |
| GET    | `/api/rooms`                  | Public    | Search/list available rooms|
| GET    | `/api/rooms/{id}`             | Public    | Room details + reviews     |
| POST   | `/api/bookings`               | Customer  | Create booking             |
| GET    | `/api/bookings/me`            | Customer  | List my bookings           |
| DELETE | `/api/bookings/{id}`          | Customer  | Cancel booking             |
| POST   | `/api/payments/checkout`      | Customer  | Pay for booking            |
| POST   | `/api/reviews`                | Customer  | Leave a review             |
| GET    | `/api/admin/bookings`         | Admin     | All bookings               |
| GET    | `/api/admin/reports/revenue`  | Admin     | Revenue report             |

Full API docs in [docs/api.md](docs/api.md).

## Testing

```bash
cd backend && mvn test
cd ../frontend && npm test
```

## Roadmap

- Stripe integration for real payments
- Multi-property support
- Loyalty points & promo codes
- WebSocket live availability updates
- CI/CD pipeline (GitHub Actions)
- Kubernetes manifests

## License

[MIT](LICENSE) (c) 2026
