package com.grabaseat.controller;

import com.grabaseat.dto.AdminBookingResponse;
import com.grabaseat.dto.AdminSummaryResponse;
import com.grabaseat.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Admin Bookings", description = "Admin booking management and summary analytics")
@RestController
@RequestMapping("/api/admin")
public class AdminBookingController {

    private final BookingService bookingService;

    public AdminBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Operation(summary = "Get all bookings (Admin only)")
    @GetMapping("/bookings")
    public ResponseEntity<List<AdminBookingResponse>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookingsAdmin());
    }

    @Operation(summary = "Get booking summary and revenue statistics (Admin only)")
    @GetMapping("/summary")
    public ResponseEntity<AdminSummaryResponse> getSummary() {
        return ResponseEntity.ok(bookingService.getAdminSummary());
    }
}
