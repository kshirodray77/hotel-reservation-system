package com.hotel.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class HotelReservationApplication {
    public static void main(String[] args) {
        SpringApplication.run(HotelReservationApplication.class, args);
    }
}
