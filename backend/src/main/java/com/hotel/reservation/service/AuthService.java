package com.hotel.reservation.service;

import com.hotel.reservation.dto.AuthDtos.*;
import com.hotel.reservation.entity.User;
import com.hotel.reservation.exception.ApiException;
import com.hotel.reservation.repository.UserRepository;
import com.hotel.reservation.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }
        User user = User.builder()
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .fullName(req.fullName())
                .phone(req.phone())
                .role(User.Role.CUSTOMER)
                .build();
        userRepository.save(user);
        return tokenFor(user);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return tokenFor(user);
    }

    private AuthResponse tokenFor(User user) {
        String token = jwtUtil.generate(user.getEmail(), user.getId(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getFullName(), user.getRole().name());
    }
}
