package com.grabaseat.controller;

import com.grabaseat.dto.BookingRequest;
import com.grabaseat.dto.BookingResponse;
import com.grabaseat.dto.LockSeatsRequest;
import com.grabaseat.dto.LockSeatsResponse;
import com.grabaseat.dto.SeatResponse;
import com.grabaseat.dto.ShowResponse;
import com.grabaseat.service.BookingService;
import com.grabaseat.service.ShowService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Shows", description = "Shows, seats, locking, and booking")
@RestController
@RequestMapping("/api/shows")
public class ShowController {

    private final ShowService showService;
    private final BookingService bookingService;

    public ShowController(ShowService showService, BookingService bookingService) {
        this.showService = showService;
        this.bookingService = bookingService;
    }

    @GetMapping("/{showId}")
    public ResponseEntity<ShowResponse> getShowById(@PathVariable Long showId) {
        return ResponseEntity.ok(showService.getShowById(showId));
    }

    @GetMapping("/{showId}/seats")
    public ResponseEntity<List<SeatResponse>> getSeatsForShow(@PathVariable Long showId) {
        return ResponseEntity.ok(showService.getSeatsForShow(showId));
    }

    @PostMapping("/{showId}/lock")
    public ResponseEntity<LockSeatsResponse> lockSeats(
            @PathVariable Long showId,
            @Valid @RequestBody LockSeatsRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(showService.lockSeats(showId, request, authentication.getName()));
    }

    @PostMapping("/{showId}/book")
    public ResponseEntity<BookingResponse> bookSeats(
            @PathVariable Long showId,
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.bookSeats(showId, request, authentication.getName()));
    }
}
