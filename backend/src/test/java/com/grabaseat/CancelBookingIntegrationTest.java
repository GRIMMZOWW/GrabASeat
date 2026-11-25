package com.grabaseat;

import com.grabaseat.dto.BookingRequest;
import com.grabaseat.dto.BookingResponse;
import com.grabaseat.dto.LockSeatsRequest;
import com.grabaseat.dto.ShowRequest;
import com.grabaseat.dto.ShowResponse;
import com.grabaseat.model.Booking;
import com.grabaseat.model.BookingStatus;
import com.grabaseat.model.Movie;
import com.grabaseat.model.PaymentMethod;
import com.grabaseat.model.Role;
import com.grabaseat.model.SeatStatus;
import com.grabaseat.model.Show;
import com.grabaseat.model.ShowSeat;
import com.grabaseat.model.Theatre;
import com.grabaseat.model.User;
import com.grabaseat.repository.BookingRepository;
import com.grabaseat.repository.MovieRepository;
import com.grabaseat.repository.ShowRepository;
import com.grabaseat.repository.ShowSeatRepository;
import com.grabaseat.repository.TheatreRepository;
import com.grabaseat.repository.UserRepository;
import com.grabaseat.service.BookingService;
import com.grabaseat.service.ShowService;
import com.grabaseat.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:cancel_booking_testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "JWT_SECRET=supersecretkeyformyseatbookingapp12345"
})
class CancelBookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TheatreRepository theatreRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShowService showService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private JwtUtil jwtUtil;

    private Movie movie;
    private Theatre theatre;
    private Long showId;
    private User userA;
    private User userB;
    private String tokenA;
    private String tokenB;
    private String adminToken;

    @BeforeEach
    void setUp() {
        showSeatRepository.deleteAll();
        bookingRepository.deleteAll();
        showRepository.deleteAll();
        movieRepository.deleteAll();
        theatreRepository.deleteAll();
        userRepository.deleteAll();

        movie = movieRepository.save(new Movie("Inception", "English", "Sci-Fi", 148));
        theatre = theatreRepository.save(new Theatre("PVR Grand", "Bangalore"));

        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0).withSecond(0).withNano(0);
        ShowRequest request = new ShowRequest(movie.getId(), theatre.getId(), 1, startTime, new BigDecimal("250.00"));
        ShowResponse showResponse = showService.createShow(request);
        showId = showResponse.getId();

        userA = userRepository.save(new User("User A", "userA@example.com", "passA123", Role.USER));
        userB = userRepository.save(new User("User B", "userB@example.com", "passB123", Role.USER));
        User admin = userRepository.save(new User("Admin", "admin@example.com", "admin123", Role.ADMIN));

        tokenA = jwtUtil.createToken(userA.getEmail(), userA.getRole());
        tokenB = jwtUtil.createToken(userB.getEmail(), userB.getRole());
        adminToken = jwtUtil.createToken(admin.getEmail(), admin.getRole());
    }

    private BookingResponse createConfirmedBooking(User user, List<String> seats) {
        showService.lockSeats(showId, new LockSeatsRequest(seats), user.getEmail());
        return bookingService.bookSeats(showId, new BookingRequest(seats, PaymentMethod.UPI), user.getEmail());
    }

    @Test
    void cancellingOwnBookingSetsCancelledAndFreesSeats() throws Exception {
        BookingResponse booking = createConfirmedBooking(userA, List.of("A1", "A2"));

        mockMvc.perform(post("/api/bookings/" + booking.getBookingId() + "/cancel")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId", is(booking.getBookingId().intValue())))
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        Booking updated = bookingRepository.findById(booking.getBookingId()).orElseThrow();
        assertEquals(BookingStatus.CANCELLED, updated.getStatus());

        List<ShowSeat> seats = showSeatRepository.findByShowIdOrderBySeatNumberAsc(showId).stream()
                .filter(s -> s.getSeatNumber().equals("A1") || s.getSeatNumber().equals("A2"))
                .toList();
        for (ShowSeat seat : seats) {
            assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
            assertNull(seat.getBooking());
            assertNull(seat.getLockedByUserId());
            assertNull(seat.getLockedUntil());
        }
    }

    @Test
    void anotherUserCancellingBookingGets404AndNothingChanges() throws Exception {
        BookingResponse booking = createConfirmedBooking(userA, List.of("A3"));

        mockMvc.perform(post("/api/bookings/" + booking.getBookingId() + "/cancel")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Booking not found")));

        Booking stillConfirmed = bookingRepository.findById(booking.getBookingId()).orElseThrow();
        assertEquals(BookingStatus.CONFIRMED, stillConfirmed.getStatus());

        ShowSeat seatA3 = showSeatRepository.findByShowIdOrderBySeatNumberAsc(showId).stream()
                .filter(s -> s.getSeatNumber().equals("A3"))
                .findFirst()
                .orElseThrow();
        assertEquals(SeatStatus.BOOKED, seatA3.getStatus());
    }

    @Test
    void cancellingTwiceReturns409SecondTime() throws Exception {
        BookingResponse booking = createConfirmedBooking(userA, List.of("A4"));

        mockMvc.perform(post("/api/bookings/" + booking.getBookingId() + "/cancel")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/bookings/" + booking.getBookingId() + "/cancel")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Booking is already cancelled")));
    }

    @Test
    void cannotCancelBookingForShowThatAlreadyStarted() throws Exception {
        LocalDateTime pastStart = LocalDateTime.now().minusHours(1);
        LocalDateTime pastEnd = pastStart.plusMinutes(148);
        Show pastShow = showRepository.save(new Show(movie, theatre, 2, pastStart, pastEnd, new BigDecimal("250.00")));
        Booking pastBooking = bookingRepository.save(new Booking(
                userA,
                pastShow,
                new BigDecimal("250.00"),
                PaymentMethod.UPI,
                BookingStatus.CONFIRMED,
                LocalDateTime.now().minusHours(3)
        ));
        ShowSeat seat = new ShowSeat(pastShow, "A1", SeatStatus.BOOKED);
        seat.setBooking(pastBooking);
        showSeatRepository.save(seat);

        mockMvc.perform(post("/api/bookings/" + pastBooking.getId() + "/cancel")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Cannot cancel after the show has started")));
    }

    @Test
    void afterCancelAnotherUserCanLockFreedSeats() throws Exception {
        BookingResponse booking = createConfirmedBooking(userA, List.of("B1", "B2"));

        mockMvc.perform(post("/api/bookings/" + booking.getBookingId() + "/cancel")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/shows/" + showId + "/lock")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatNumbers\":[\"B1\",\"B2\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.showId", is(showId.intValue())));
    }

    @Test
    void adminSummaryExcludesCancelledBookingsFromSeatsSoldAndRevenue() throws Exception {
        createConfirmedBooking(userA, List.of("C1", "C2"));
        BookingResponse toCancel = createConfirmedBooking(userB, List.of("C3", "C4"));

        mockMvc.perform(post("/api/bookings/" + toCancel.getBookingId() + "/cancel")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/summary")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedBookings", is(1)))
                .andExpect(jsonPath("$.cancelledBookings", is(1)))
                .andExpect(jsonPath("$.seatsSold", is(2)))
                .andExpect(jsonPath("$.totalRevenue", is(500.0)));
    }
}
