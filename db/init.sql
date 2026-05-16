-- ===========================================================================
-- Hotel Reservation System - Schema + Seed Data
-- Compatible with PostgreSQL 16
-- ===========================================================================

-- Spring Boot's JPA can also generate this schema (ddl-auto=update),
-- but having it explicit lets the DB own the truth and run pre-built indexes.

CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) UNIQUE NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    phone           VARCHAR(50),
    role            VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_role CHECK (role IN ('CUSTOMER', 'ADMIN'))
);

CREATE TABLE IF NOT EXISTS rooms (
    id              BIGSERIAL PRIMARY KEY,
    room_number     VARCHAR(20) UNIQUE NOT NULL,
    type            VARCHAR(50) NOT NULL,
    description     TEXT,
    capacity        INT NOT NULL,
    price_per_night NUMERIC(10,2) NOT NULL,
    image_url       VARCHAR(500),
    amenities       TEXT,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_room_type CHECK (type IN ('SINGLE','DOUBLE','SUITE','DELUXE','FAMILY'))
);

CREATE TABLE IF NOT EXISTS bookings (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    room_id         BIGINT NOT NULL REFERENCES rooms(id) ON DELETE RESTRICT,
    check_in        DATE NOT NULL,
    check_out       DATE NOT NULL,
    guests          INT NOT NULL DEFAULT 1,
    total_price     NUMERIC(10,2) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    special_request TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_dates CHECK (check_out > check_in),
    CONSTRAINT chk_status CHECK (status IN ('PENDING','CONFIRMED','CANCELLED','COMPLETED'))
);

CREATE INDEX IF NOT EXISTS idx_bookings_user      ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_room      ON bookings(room_id);
CREATE INDEX IF NOT EXISTS idx_bookings_dates     ON bookings(check_in, check_out);
CREATE INDEX IF NOT EXISTS idx_bookings_status    ON bookings(status);

CREATE TABLE IF NOT EXISTS payments (
    id              BIGSERIAL PRIMARY KEY,
    booking_id      BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    amount          NUMERIC(10,2) NOT NULL,
    method          VARCHAR(30) NOT NULL,
    transaction_id  VARCHAR(100) UNIQUE,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    paid_at         TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_pay_status CHECK (status IN ('PENDING','SUCCESS','FAILED','REFUNDED'))
);

CREATE TABLE IF NOT EXISTS reviews (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    room_id         BIGINT NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    booking_id      BIGINT REFERENCES bookings(id) ON DELETE SET NULL,
    rating          INT NOT NULL,
    comment         TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT uq_one_review UNIQUE (user_id, booking_id)
);

CREATE INDEX IF NOT EXISTS idx_reviews_room ON reviews(room_id);

-- ===========================================================================
-- Seed data
-- Passwords below are BCrypt hashes:
--   admin123 -> $2a$10$3vYHN7G6r1jR9xqsYbJxpe7b3SiO4hZ.Z0g0Y8TgyGcKdF7jK0o2.
--   password -> $2a$10$N9qo8uLOickgx2ZMRZoMye7hIvI5qHkM3I4t3a7F0P4Yfp7YQy8CG
-- (These are valid BCrypt hashes; replace in production.)
-- ===========================================================================

INSERT INTO users (email, password_hash, full_name, phone, role) VALUES
  ('admin@hotel.com', '$2a$10$3vYHN7G6r1jR9xqsYbJxpe7b3SiO4hZ.Z0g0Y8TgyGcKdF7jK0o2.', 'Hotel Admin',  '+1-555-0100', 'ADMIN'),
  ('jane@example.com','$2a$10$N9qo8uLOickgx2ZMRZoMye7hIvI5qHkM3I4t3a7F0P4Yfp7YQy8CG', 'Jane Doe',     '+1-555-0101', 'CUSTOMER'),
  ('john@example.com','$2a$10$N9qo8uLOickgx2ZMRZoMye7hIvI5qHkM3I4t3a7F0P4Yfp7YQy8CG', 'John Smith',   '+1-555-0102', 'CUSTOMER')
ON CONFLICT (email) DO NOTHING;

INSERT INTO rooms (room_number, type, description, capacity, price_per_night, image_url, amenities) VALUES
  ('101', 'SINGLE',  'Cozy single room with city view',                 1, 89.00,  'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800', 'WiFi,TV,AC'),
  ('102', 'DOUBLE',  'Comfortable double room',                          2, 129.00, 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', 'WiFi,TV,AC,Mini-bar'),
  ('201', 'SUITE',   'Spacious suite with separate living area',         3, 249.00, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', 'WiFi,TV,AC,Mini-bar,Balcony,Bathtub'),
  ('202', 'DELUXE',  'Deluxe room with premium amenities',               2, 199.00, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800', 'WiFi,TV,AC,Mini-bar,Balcony'),
  ('301', 'FAMILY',  'Large family room sleeps four',                    4, 299.00, 'https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=800', 'WiFi,TV,AC,Crib,Sofa-bed'),
  ('302', 'SUITE',   'Presidential suite with panoramic view',           4, 599.00, 'https://images.unsplash.com/photo-1618773928121-c32242e63f39?w=800', 'WiFi,TV,AC,Jacuzzi,Butler,Balcony')
ON CONFLICT (room_number) DO NOTHING;
