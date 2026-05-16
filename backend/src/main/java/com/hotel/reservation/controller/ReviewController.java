package com.hotel.reservation.controller;

import com.hotel.reservation.dto.ReviewDtos.*;
import com.hotel.reservation.entity.User;
import com.hotel.reservation.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ReviewResponse create(@AuthenticationPrincipal User user,
                                 @Valid @RequestBody ReviewRequest req) {
        return reviewService.create(user, req);
    }

    @GetMapping("/room/{roomId}")
    public List<ReviewResponse> forRoom(@PathVariable Long roomId) {
        return reviewService.forRoom(roomId);
    }
}
