package com.hotel.reservation.repository;

import com.hotel.reservation.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByRoomIdOrderByCreatedAtDesc(Long roomId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.room.id = :roomId")
    Double averageRatingForRoom(@Param("roomId") Long roomId);
}
