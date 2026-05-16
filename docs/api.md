# API Reference

Base URL: `http://localhost:8080/api`

All authenticated endpoints expect a JWT in the `Authorization` header:

```
Authorization: Bearer <token>
```

A live, interactive version is available at `/swagger-ui.html` while the backend is running.

## Auth

### POST /auth/register

```json
{
  "email": "jane@example.com",
  "password": "supersecret",
  "fullName": "Jane Doe",
  "phone": "+1-555-1234"
}
```

Response:

```json
{
  "token": "eyJhbGciOi...",
  "userId": 4,
  "email": "jane@example.com",
  "fullName": "Jane Doe",
  "role": "CUSTOMER"
}
```

### POST /auth/login

```json
{ "email": "jane@example.com", "password": "supersecret" }
```

Same response shape as `/auth/register`.

## Rooms

### GET /rooms

Query params (all optional):

| param      | type     | description                       |
| ---------- | -------- | --------------------------------- |
| `checkIn`  | ISO date | defaults to today                 |
| `checkOut` | ISO date | defaults to tomorrow              |
| `guests`   | int      | minimum capacity, defaults to 1   |
| `minPrice` | decimal  |                                   |
| `maxPrice` | decimal  |                                   |
| `type`     | enum     | SINGLE / DOUBLE / SUITE / DELUXE / FAMILY |

Returns rooms that are active, fit the capacity & price filters, and have no overlapping booking in `PENDING` or `CONFIRMED`.

### GET /rooms/{id}

Single room detail including `averageRating`.

### POST /rooms (admin)

```json
{
  "roomNumber": "401",
  "type": "DELUXE",
  "description": "Modern deluxe room",
  "capacity": 2,
  "pricePerNight": 219.00,
  "imageUrl": "https://...",
  "amenities": "WiFi,TV,AC,Mini-bar"
}
```

### PUT /rooms/{id} (admin)

Same body as POST.

### DELETE /rooms/{id} (admin)

Soft-disables the room (`is_active=false`).

## Bookings

### POST /bookings (customer)

```json
{
  "roomId": 3,
  "checkIn": "2026-06-01",
  "checkOut": "2026-06-04",
  "guests": 2,
  "specialRequest": "Quiet room, please"
}
```

Errors:
- `404` room not found
- `400` invalid dates / capacity exceeded
- `409` room already booked for those dates

### GET /bookings/me (customer)

Returns the caller's bookings, newest first.

### DELETE /bookings/{id} (customer or admin)

Cancels the booking. Only `PENDING` and `CONFIRMED` bookings can be cancelled, and only before the check-in date.

## Payments

### POST /payments/checkout (customer)

```json
{
  "bookingId": 12,
  "method": "CARD",
  "cardNumber": "4242424242424242",
  "cardHolder": "Jane Doe",
  "expiry": "12/29",
  "cvv": "123"
}
```

The mock processor succeeds for any card number that does not end with `0000`. On success the booking automatically transitions to `CONFIRMED` (via Kafka).

## Reviews

### POST /reviews (customer)

```json
{
  "bookingId": 12,
  "rating": 5,
  "comment": "Loved the view"
}
```

Only one review per booking.

### GET /reviews/room/{roomId}

Public list of reviews for a room.

## Admin

### GET /admin/bookings

Returns every booking in the system.

### GET /admin/reports/revenue

Query params: `from`, `to` (ISO dates). Defaults to month-to-date.

```json
{
  "from": "2026-05-01",
  "to":   "2026-05-31",
  "totalRevenue": 12450.00,
  "totalRooms":   6,
  "totalBookings": 42
}
```

## Error format

All errors follow this shape:

```json
{
  "timestamp": "2026-05-05T13:45:00",
  "status": 409,
  "error": "Conflict",
  "message": "Room is already booked for those dates"
}
```

Validation errors include a `details` map of field -> message.
