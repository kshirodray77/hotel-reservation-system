package com.hotel.reservation.repository;

import com.hotel.reservation.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * Find rooms that are active, fit the requested capacity & price range, and
     * have NO overlapping booking in (PENDING, CONFIRMED) for the given dates.
     */
    @Query("""
        SELECT r FROM Room r
        WHERE r.isActive = true
          AND r.capacity >= :guests
          AND (:minPrice IS NULL OR r.pricePerNight >= :minPrice)
          AND (:maxPrice IS NULL OR r.pricePerNight <= :maxPrice)
          AND (:type IS NULL OR r.type = :type)
          AND r.id NOT IN (
              SELECT b.room.id FROM Booking b
              WHERE b.status IN ('PENDING','CONFIRMED')
                AND b.checkIn  < :checkOut
                AND b.checkOut > :checkIn
          )
        ORDER BY r.pricePerNight ASC
    """)
    List<Room> searchAvailable(
            @Param("checkIn")  LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("guests")   Integer guests,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("type")     Room.Type type
    );
}
