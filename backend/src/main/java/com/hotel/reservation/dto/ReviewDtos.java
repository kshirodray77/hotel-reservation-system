package com.hotel.reservation.dto;

import com.hotel.reservation.entity.Review;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public class ReviewDtos {

    public record ReviewRequest(
            @NotNull Long bookingId,
            @Min(1) @Max(5) Integer rating,
            @Size(max = 2000) String comment
    ) {}

    public record ReviewResponse(
            Long id,
            Long roomId,
            Long userId,
            String userFullName,
            Integer rating,
            String comment,
            LocalDateTime createdAt
    ) {
        public static ReviewResponse from(Review r) {
            return new ReviewResponse(
                    r.getId(),
                    r.getRoom().getId(),
                    r.getUser().getId(),
                    r.getUser().getFullName(),
                    r.getRating(),
                    r.getComment(),
                    r.getCreatedAt()
            );
        }
    }
}
