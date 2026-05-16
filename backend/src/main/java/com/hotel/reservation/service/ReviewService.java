package com.hotel.reservation.service;

import com.hotel.reservation.dto.ReviewDtos.*;
import com.hotel.reservation.entity.Booking;
import com.hotel.reservation.entity.Review;
import com.hotel.reservation.entity.User;
import com.hotel.reservation.exception.ApiException;
import com.hotel.reservation.repository.BookingRepository;
import com.hotel.reservation.repository.ReviewRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    public ReviewService(ReviewRepository reviewRepository, BookingRepository bookingRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
    }

    public ReviewResponse create(User user, ReviewRequest req) {
        Booking b = bookingRepository.findById(req.bookingId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (!b.getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not your booking");
        }
        if (b.getStatus() != Booking.Status.COMPLETED && b.getStatus() != Booking.Status.CONFIRMED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You can only review bookings you've taken");
        }
        Review r = Review.builder()
                .user(user)
                .room(b.getRoom())
                .booking(b)
                .rating(req.rating())
                .comment(req.comment())
                .build();
        reviewRepository.save(r);
        return ReviewResponse.from(r);
    }

    public List<ReviewResponse> forRoom(Long roomId) {
        return reviewRepository.findByRoomIdOrderByCreatedAtDesc(roomId)
                .stream().map(ReviewResponse::from).toList();
    }
}
