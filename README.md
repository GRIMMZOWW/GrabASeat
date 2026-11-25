# GrabASeat

[![CI](https://github.com/GRIMMZOWW/GrabASeat/actions/workflows/ci.yml/badge.svg)](https://github.com/GRIMMZOWW/GrabASeat/actions/workflows/ci.yml)

## What GrabASeat is
GrabASeat is a movie ticket booking web application inspired by platforms like BookMyShow. I am building it to practice Spring Boot, MySQL and React in one project. The main challenge is stopping two people from booking the same seat.

## Tech Stack
- Java 21
- Spring Boot
- MySQL
- JPA / Hibernate
- JWT
- React

## Features Planned
- [ ] Login with two roles: Admin and User
- [ ] Movies and shows
- [ ] Seat map
- [ ] Seat lock and booking
- [ ] Mock payment
- [ ] Booking history
- [ ] Admin panel

## How to Run

### Run tests
```bash
cd backend
./mvnw test
```

### Run with Docker Compose
1. Copy `.env.example` to `.env` and fill in the values:
   - `DB_USER`
   - `DB_PASSWORD`
   - `JWT_SECRET`
   - `ADMIN_EMAIL`
   - `ADMIN_PASSWORD`
2. Start the services:
```bash
docker compose up --build
```

API documentation is available at http://localhost:8080/swagger-ui/index.html

## Progress Log

| Day | What I built |
| --- | ------------ |
| 1   | Spring Boot backend setup, MySQL config, GET /api/health endpoint |
| 2   | User table and register API |
| 3   | Login with JWT |
| 4   | JWT filter and admin account |
| 5   | Movie CRUD |
| 6   | Theatres and shows |
| 7   | Seats for each show and the seat map |
| 8   | Seat locking |
| 9   | Booking and mock payment |
| 11  | Admin view, BigDecimal and Swagger |
| 12  | Docker and CI |

