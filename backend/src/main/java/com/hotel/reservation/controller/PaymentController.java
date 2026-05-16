package com.hotel.reservation.controller;

import com.hotel.reservation.dto.PaymentDtos.*;
import com.hotel.reservation.entity.User;
import com.hotel.reservation.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/checkout")
    public PaymentResponse checkout(@AuthenticationPrincipal User user,
                                    @Valid @RequestBody CheckoutRequest req) {
        return paymentService.checkout(user, req);
    }
}
