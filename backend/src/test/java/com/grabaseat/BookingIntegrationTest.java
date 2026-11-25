package com.grabaseat;

import com.grabaseat.dto.BookingRequest;
import com.grabaseat.dto.LockSeatsRequest;
import com.grabaseat.dto.ShowRequest;
import com.grabaseat.dto.ShowResponse;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:booking_testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "JWT_SECRET=supersecretkeyformyseatbookingapp12345"
})
class BookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShowService showService;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TheatreRepository theatreRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private Movie movie;
    private Theatre theatre;
    private Long showId;
    private User userA;
    private User userB;
    private String tokenA;
    private String tokenB;

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
        ShowRequest request = new ShowRequest(movie.getId(), theatre.getId(), 1, startTime, BigDecimal.valueOf(200.0));
        ShowResponse showResponse = showService.createShow(request);
        showId = showResponse.getId();

        userA = userRepository.save(new User("User A", "userA@example.com", "passA123", Role.USER));
        userB = userRepository.save(new User("User B", "userB@example.com", "passB123", Role.USER));

        tokenA = jwtUtil.createToken(userA.getEmail(), userA.getRole());
        tokenB = jwtUtil.createToken(userB.getEmail(), userB.getRole());
    }

    @Test
    void bookingSeatsILockedWorks() throws Exception {
        // User A locks A1 and A2
        showService.lockSeats(showId, new LockSeatsRequest(List.of("A1", "A2")), userA.getEmail());

        // User A books A1 and A2 with UPI
        mockMvc.perform(post("/api/shows/" + showId + "/book")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatNumbers\":[\"A1\",\"A2\"],\"paymentMethod\":\"UPI\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.movieTitle", is("Inception")))
                .andExpect(jsonPath("$.totalAmount", is(400.0)))
                .andExpect(jsonPath("$.paymentMethod", is("UPI")))
                .andExpect(jsonPath("$.status", is("CONFIRMED")));

        // Verify seats are BOOKED and linked to booking
        ShowSeat seatA1 = showSeatRepository.findByShowIdOrderBySeatNumberAsc(showId).stream()
                .filter(s -> s.getSeatNumber().equals("A1"))
                .findFirst()
                .orElseThrow();
        assertEquals(SeatStatus.BOOKED, seatA1.getStatus());
        assertNotNull(seatA1.getBooking());
    }

    @Test
    void bookingSeatsINeverLockedReturns409() throws Exception {
        // Seat A3 was never locked
        mockMvc.perform(post("/api/shows/" + showId + "/book")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatNumbers\":[\"A3\"],\"paymentMethod\":\"CARD\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Seat A3 is not locked by you or the lock expired")));

        // Seat A3 is still AVAILABLE
        ShowSeat seatA3 = showSeatRepository.findByShowIdOrderBySeatNumberAsc(showId).stream()
                .filter(s -> s.getSeatNumber().equals("A3"))
                .findFirst()
                .orElseThrow();
        assertEquals(SeatStatus.AVAILABLE, seatA3.getStatus());
    }

    @Test
    void bookingSeatsWithExpiredLockReturns409() throws Exception {
        // Lock A4 with past timestamp
        ShowSeat seatA4 = showSeatRepository.findByShowIdOrderBySeatNumberAsc(showId).stream()
                .filter(s -> s.getSeatNumber().equals("A4"))
                .findFirst()
                .orElseThrow();
        seatA4.setStatus(SeatStatus.LOCKED);
        seatA4.setLockedByUserId(userA.getId());
        seatA4.setLockedUntil(LocalDateTime.now().minusMinutes(5));
        showSeatRepository.save(seatA4);

        mockMvc.perform(post("/api/shows/" + showId + "/book")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatNumbers\":[\"A4\"],\"paymentMethod\":\"UPI\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Seat A4 is not locked by you or the lock expired")));

        ShowSeat updatedSeatA4 = showSeatRepository.findByShowIdOrderBySeatNumberAsc(showId).stream()
                .filter(s -> s.getSeatNumber().equals("A4"))
                .findFirst()
                .orElseThrow();
        assertEquals(SeatStatus.LOCKED, updatedSeatA4.getStatus());
    }

    @Test
    void secondUserCannotBookSeatsFirstUserLocked() throws Exception {
        // User A locks A5
        showService.lockSeats(showId, new LockSeatsRequest(List.of("A5")), userA.getEmail());

        // User B tries to book A5 -> 409
        mockMvc.perform(post("/api/shows/" + showId + "/book")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatNumbers\":[\"A5\"],\"paymentMethod\":\"UPI\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Seat A5 is not locked by you or the lock expired")));
    }

    @Test
    void lockingBookedSeatReturns409() throws Exception {
        // User A locks and books B1
        showService.lockSeats(showId, new LockSeatsRequest(List.of("B1")), userA.getEmail());
        mockMvc.perform(post("/api/shows/" + showId + "/book")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatNumbers\":[\"B1\"],\"paymentMethod\":\"UPI\"}"))
                .andExpect(status().isCreated());

        // User B tries to lock B1 -> 409
        mockMvc.perform(post("/api/shows/" + showId + "/lock")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatNumbers\":[\"B1\"]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Seat B1 is not available")));
    }

    @Test
    void myBookingsReturnsOnlyMyOwnBookings() throws Exception {
        // User A locks and books B2
        showService.lockSeats(showId, new LockSeatsRequest(List.of("B2")), userA.getEmail());
        mockMvc.perform(post("/api/shows/" + showId + "/book")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatNumbers\":[\"B2\"],\"paymentMethod\":\"CARD\"}"))
                .andExpect(status().isCreated());

        // User B locks and books B3
        showService.lockSeats(showId, new LockSeatsRequest(List.of("B3")), userB.getEmail());
        mockMvc.perform(post("/api/shows/" + showId + "/book")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatNumbers\":[\"B3\"],\"paymentMethod\":\"UPI\"}"))
                .andExpect(status().isCreated());

        // User A calls GET /api/bookings/my
        mockMvc.perform(get("/api/bookings/my")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].seatNumbers[0]", is("B2")));
    }

    @Test
    void lockingAndBookingSeatsForPastShowReturns409() throws Exception {
        // Create past show directly through repository
        LocalDateTime pastStart = LocalDateTime.now().minusHours(2);
        LocalDateTime pastEnd = pastStart.plusMinutes(120);
        Show pastShow = showRepository.save(new Show(movie, theatre, 5, pastStart, pastEnd, BigDecimal.valueOf(200.0)));
        showSeatRepository.save(new ShowSeat(pastShow, "A1", SeatStatus.AVAILABLE));

        // Lock seats on past show -> 409
        mockMvc.perform(post("/api/shows/" + pastShow.getId() + "/lock")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatNumbers\":[\"A1\"]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Show has already started")));

        // Book seats on past show -> 409
        mockMvc.perform(post("/api/shows/" + pastShow.getId() + "/book")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatNumbers\":[\"A1\"],\"paymentMethod\":\"UPI\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Show has already started")));
    }
}
