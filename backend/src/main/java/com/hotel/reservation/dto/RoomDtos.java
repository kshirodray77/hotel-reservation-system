package com.hotel.reservation.dto;

import com.hotel.reservation.entity.Room;

import java.math.BigDecimal;

public class RoomDtos {

    public record RoomResponse(
            Long id,
            String roomNumber,
            String type,
            String description,
            Integer capacity,
            BigDecimal pricePerNight,
            String imageUrl,
            String amenities,
            Boolean isActive,
            Double averageRating
    ) {
        public static RoomResponse from(Room r, Double avgRating) {
            return new RoomResponse(
                    r.getId(),
                    r.getRoomNumber(),
                    r.getType().name(),
                    r.getDescription(),
                    r.getCapacity(),
                    r.getPricePerNight(),
                    r.getImageUrl(),
                    r.getAmenities(),
                    r.getIsActive(),
                    avgRating
            );
        }
    }

    public record RoomCreateRequest(
            String roomNumber,
            String type,
            String description,
            Integer capacity,
            BigDecimal pricePerNight,
            String imageUrl,
            String amenities
    ) {}
}
