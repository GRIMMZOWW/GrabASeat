package com.grabaseat.service;

import com.grabaseat.dto.AdminBookingResponse;
import com.grabaseat.dto.AdminSummaryResponse;
import com.grabaseat.dto.BookingRequest;
import com.grabaseat.dto.BookingResponse;
import com.grabaseat.exception.SeatUnavailableException;
import com.grabaseat.exception.ShowClosedException;
import com.grabaseat.exception.ShowNotFoundException;
import com.grabaseat.model.Booking;
import com.grabaseat.model.BookingStatus;
import com.grabaseat.model.SeatStatus;
import com.grabaseat.model.Show;
import com.grabaseat.model.ShowSeat;
import com.grabaseat.model.User;
import com.grabaseat.repository.BookingRepository;
import com.grabaseat.repository.ShowRepository;
import com.grabaseat.repository.ShowSeatRepository;
import com.grabaseat.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                          ShowRepository showRepository,
                          ShowSeatRepository showSeatRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.showRepository = showRepository;
        this.showSeatRepository = showSeatRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BookingResponse bookSeats(Long showId, BookingRequest request, String userEmail) {
        if (request.getSeatNumbers() == null || request.getSeatNumbers().isEmpty() || request.getSeatNumbers().size() > 6) {
            throw new IllegalArgumentException("Seat numbers must contain between 1 and 6 items");
        }
        for (String seatNumber : request.getSeatNumbers()) {
            if (seatNumber == null || seatNumber.trim().isEmpty()) {
                throw new IllegalArgumentException("Seat numbers cannot contain blanks");
            }
        }
        if (request.getSeatNumbers().stream().distinct().count() != request.getSeatNumbers().size()) {
            throw new IllegalArgumentException("Duplicate seat numbers are not allowed");
        }
        if (request.getPaymentMethod() == null) {
            throw new IllegalArgumentException("Payment method is required");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Long userId = user.getId();

        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException("Show not found"));

        if (!show.getStartTime().isAfter(LocalDateTime.now())) {
            throw new ShowClosedException("Show has already started");
        }

        List<ShowSeat> seats = showSeatRepository.findByShowIdAndSeatNumberIn(showId, request.getSeatNumbers());
        Set<String> foundSeatNumbers = seats.stream().map(ShowSeat::getSeatNumber).collect(Collectors.toSet());
        for (String seatNumber : request.getSeatNumbers()) {
            if (!foundSeatNumbers.contains(seatNumber)) {
                throw new IllegalArgumentException("Seat " + seatNumber + " does not exist for this show");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        for (ShowSeat seat : seats) {
            boolean isLockedByUser = seat.getStatus() == SeatStatus.LOCKED
                    && userId.equals(seat.getLockedByUserId());
            boolean isLockNotExpired = seat.getLockedUntil() != null
                    && seat.getLockedUntil().isAfter(now);

            if (!isLockedByUser || !isLockNotExpired) {
                throw new SeatUnavailableException("Seat " + seat.getSeatNumber() + " is not locked by you or the lock expired");
            }
        }

        BigDecimal totalAmount = show.getPrice().multiply(BigDecimal.valueOf(seats.size()));
        LocalDateTime createdAt = LocalDateTime.now();
        Booking booking = new Booking(user, show, totalAmount, request.getPaymentMethod(), BookingStatus.CONFIRMED, createdAt);
        Booking savedBooking = bookingRepository.save(booking);

        for (ShowSeat seat : seats) {
            seat.setStatus(SeatStatus.BOOKED);
            seat.setLockedByUserId(null);
            seat.setLockedUntil(null);
            seat.setBooking(savedBooking);
        }
        showSeatRepository.saveAll(seats);

        return new BookingResponse(
                savedBooking.getId(),
                show.getMovie().getTitle(),
                show.getTheatre().getName(),
                show.getTheatre().getCity(),
                show.getScreenNumber(),
                show.getStartTime(),
                request.getSeatNumbers(),
                totalAmount,
                savedBooking.getPaymentMethod(),
                savedBooking.getStatus(),
                createdAt
        );
    }

    public List<BookingResponse> getMyBookings(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return bookings.stream()
                .map(b -> {
                    List<String> seatNumbers = showSeatRepository.findByBookingId(b.getId()).stream()
                            .sorted(Comparator.comparing((ShowSeat s) -> s.getSeatNumber().substring(0, 1))
                                    .thenComparingInt(s -> Integer.parseInt(s.getSeatNumber().substring(1))))
                            .map(ShowSeat::getSeatNumber)
                            .toList();

                    return new BookingResponse(
                            b.getId(),
                            b.getShow().getMovie().getTitle(),
                            b.getShow().getTheatre().getName(),
                            b.getShow().getTheatre().getCity(),
                            b.getShow().getScreenNumber(),
                            b.getShow().getStartTime(),
                            seatNumbers,
                            b.getTotalAmount(),
                            b.getPaymentMethod(),
                            b.getStatus(),
                            b.getCreatedAt()
                    );
                })
                .toList();
    }

    public List<AdminBookingResponse> getAllBookingsAdmin() {
        List<Booking> bookings = bookingRepository.findAllByOrderByCreatedAtDesc();

        return bookings.stream()
                .map(b -> {
                    List<String> seatNumbers = showSeatRepository.findByBookingId(b.getId()).stream()
                            .sorted(Comparator.comparing((ShowSeat s) -> s.getSeatNumber().substring(0, 1))
                                    .thenComparingInt(s -> Integer.parseInt(s.getSeatNumber().substring(1))))
                            .map(ShowSeat::getSeatNumber)
                            .toList();

                    return new AdminBookingResponse(
                            b.getId(),
                            b.getUser().getEmail(),
                            b.getShow().getMovie().getTitle(),
                            b.getShow().getTheatre().getName(),
                            b.getShow().getTheatre().getCity(),
                            b.getShow().getScreenNumber(),
                            b.getShow().getStartTime(),
                            seatNumbers,
                            b.getTotalAmount(),
                            b.getPaymentMethod(),
                            b.getStatus(),
                            b.getCreatedAt()
                    );
                })
                .toList();
    }

    public AdminSummaryResponse getAdminSummary() {
        List<Booking> allBookings = bookingRepository.findAll();

        long confirmedBookings = 0;
        long cancelledBookings = 0;
        long seatsSold = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;

        for (Booking booking : allBookings) {
            if (booking.getStatus() == BookingStatus.CONFIRMED) {
                confirmedBookings++;
                seatsSold += showSeatRepository.findByBookingId(booking.getId()).size();
                totalRevenue = totalRevenue.add(booking.getTotalAmount());
            } else if (booking.getStatus() == BookingStatus.CANCELLED) {
                cancelledBookings++;
            }
        }

        return new AdminSummaryResponse(confirmedBookings, cancelledBookings, seatsSold, totalRevenue);
    }
}
