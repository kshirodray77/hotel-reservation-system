package com.hotel.reservation.service;

import com.hotel.reservation.dto.RoomDtos.*;
import com.hotel.reservation.entity.Room;
import com.hotel.reservation.exception.ApiException;
import com.hotel.reservation.repository.RoomRepository;
import com.hotel.reservation.repository.ReviewRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final ReviewRepository reviewRepository;

    public RoomService(RoomRepository roomRepository, ReviewRepository reviewRepository) {
        this.roomRepository = roomRepository;
        this.reviewRepository = reviewRepository;
    }

    public List<RoomResponse> search(LocalDate checkIn, LocalDate checkOut,
                                     Integer guests, BigDecimal minPrice, BigDecimal maxPrice,
                                     String type) {
        if (checkIn == null) checkIn = LocalDate.now();
        if (checkOut == null) checkOut = checkIn.plusDays(1);
        if (guests == null) guests = 1;

        Room.Type t = type != null ? Room.Type.valueOf(type) : null;
        return roomRepository.searchAvailable(checkIn, checkOut, guests, minPrice, maxPrice, t)
                .stream()
                .map(r -> RoomResponse.from(r, reviewRepository.averageRatingForRoom(r.getId())))
                .toList();
    }

    public RoomResponse getById(Long id) {
        Room r = roomRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Room not found"));
        return RoomResponse.from(r, reviewRepository.averageRatingForRoom(id));
    }

    public RoomResponse create(RoomCreateRequest req) {
        Room r = Room.builder()
                .roomNumber(req.roomNumber())
                .type(Room.Type.valueOf(req.type()))
                .description(req.description())
                .capacity(req.capacity())
                .pricePerNight(req.pricePerNight())
                .imageUrl(req.imageUrl())
                .amenities(req.amenities())
                .isActive(true)
                .build();
        roomRepository.save(r);
        return RoomResponse.from(r, 0d);
    }

    public RoomResponse update(Long id, RoomCreateRequest req) {
        Room r = roomRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Room not found"));
        r.setRoomNumber(req.roomNumber());
        r.setType(Room.Type.valueOf(req.type()));
        r.setDescription(req.description());
        r.setCapacity(req.capacity());
        r.setPricePerNight(req.pricePerNight());
        r.setImageUrl(req.imageUrl());
        r.setAmenities(req.amenities());
        roomRepository.save(r);
        return RoomResponse.from(r, reviewRepository.averageRatingForRoom(id));
    }

    public void delete(Long id) {
        Room r = roomRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Room not found"));
        r.setIsActive(false);
        roomRepository.save(r);
    }
}
