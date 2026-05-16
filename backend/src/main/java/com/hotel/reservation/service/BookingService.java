package com.hotel.reservation.service;

import com.hotel.reservation.dto.BookingDtos.*;
import com.hotel.reservation.entity.Booking;
import com.hotel.reservation.entity.Room;
import com.hotel.reservation.entity.User;
import com.hotel.reservation.exception.ApiException;
import com.hotel.reservation.kafka.BookingEvent;
import com.hotel.reservation.kafka.EventProducer;
import com.hotel.reservation.repository.BookingRepository;
import com.hotel.reservation.repository.RoomRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final EventProducer producer;

    public BookingService(BookingRepository bookingRepository,
                          RoomRepository roomRepository,
                          EventProducer producer) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.producer = producer;
    }

    @Transactional
    public BookingResponse create(User user, BookingRequest req) {
        if (!req.checkOut().isAfter(req.checkIn())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Check-out must be after check-in");
        }
        Room room = roomRepository.findById(req.roomId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Room not found"));
        if (!room.getIsActive()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Room not available");
        }
        if (req.guests() > room.getCapacity()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Room capacity is " + room.getCapacity() + " guests");
        }
        if (bookingRepository.existsOverlap(room.getId(), req.checkIn(), req.checkOut())) {
            throw new ApiException(HttpStatus.CONFLICT, "Room is already booked for those dates");
        }

        long nights = ChronoUnit.DAYS.between(req.checkIn(), req.checkOut());
        BigDecimal total = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        Booking booking = Booking.builder()
                .user(user)
                .room(room)
                .checkIn(req.checkIn())
                .checkOut(req.checkOut())
                .guests(req.guests())
                .totalPrice(total)
                .status(Booking.Status.PENDING)
                .specialRequest(req.specialRequest())
                .build();
        bookingRepository.save(booking);

        producer.publishBooking(new BookingEvent(
                "CREATED", booking.getId(), user.getId(), user.getEmail(),
                room.getId(), room.getRoomNumber(),
                booking.getCheckIn(), booking.getCheckOut(),
                booking.getTotalPrice(), LocalDateTime.now()));

        return BookingResponse.from(booking);
    }

    public List<BookingResponse> myBookings(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(BookingResponse::from).toList();
    }

    public List<BookingResponse> all() {
        return bookingRepository.findAll(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))
                .stream().map(BookingResponse::from).toList();
    }

    @Transactional
    public BookingResponse cancel(User user, Long bookingId) {
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (user.getRole() != User.Role.ADMIN && !b.getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not your booking");
        }
        if (b.getStatus() == Booking.Status.CANCELLED || b.getStatus() == Booking.Status.COMPLETED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot cancel a " + b.getStatus() + " booking");
        }
        if (b.getCheckIn().isBefore(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot cancel past bookings");
        }
        b.setStatus(Booking.Status.CANCELLED);
        bookingRepository.save(b);

        producer.publishBooking(new BookingEvent(
                "CANCELLED", b.getId(), b.getUser().getId(), b.getUser().getEmail(),
                b.getRoom().getId(), b.getRoom().getRoomNumber(),
                b.getCheckIn(), b.getCheckOut(),
                b.getTotalPrice(), LocalDateTime.now()));

        return BookingResponse.from(b);
    }

    public Booking findById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));
    }
}
