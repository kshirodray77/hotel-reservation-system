package com.hotel.reservation.repository;

import com.hotel.reservation.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b
        WHERE b.room.id = :roomId
          AND b.status IN ('PENDING','CONFIRMED')
          AND b.checkIn  < :checkOut
          AND b.checkOut > :checkIn
    """)
    boolean existsOverlap(@Param("roomId")   Long roomId,
                          @Param("checkIn")  LocalDate checkIn,
                          @Param("checkOut") LocalDate checkOut);

    @Query("""
        SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b
        WHERE b.status IN ('CONFIRMED','COMPLETED')
          AND b.checkIn  >= :from
          AND b.checkOut <= :to
    """)
    java.math.BigDecimal totalRevenueBetween(@Param("from") LocalDate from,
                                              @Param("to")   LocalDate to);
}
