package com.grabaseat;

import com.grabaseat.dto.BookingRequest;
import com.grabaseat.dto.LockSeatsRequest;
import com.grabaseat.dto.ShowRequest;
import com.grabaseat.dto.ShowResponse;
import com.grabaseat.model.Booking;
import com.grabaseat.model.BookingStatus;
import com.grabaseat.model.Movie;
import com.grabaseat.model.PaymentMethod;
import com.grabaseat.model.Role;
import com.grabaseat.model.Show;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:admin_booking_testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "JWT_SECRET=supersecretkeyformyseatbookingapp12345"
})
public class AdminBookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

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
    private ShowService showService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private JwtUtil jwtUtil;

    private String userToken;
    private String adminToken;
    private User testUser;
    private Long showId;

    @BeforeEach
    void setUp() {
        showSeatRepository.deleteAll();
        bookingRepository.deleteAll();
        showRepository.deleteAll();
        movieRepository.deleteAll();
        theatreRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(new User("Normal User", "user@test.com", "pass123", Role.USER));
        User adminUser = userRepository.save(new User("Admin User", "admin@test.com", "admin123", Role.ADMIN));

        userToken = jwtUtil.createToken(testUser.getEmail(), testUser.getRole());
        adminToken = jwtUtil.createToken(adminUser.getEmail(), adminUser.getRole());

        Movie movie = movieRepository.save(new Movie("Test Movie", "English", "Drama", 120));
        Theatre theatre = theatreRepository.save(new Theatre("Test Cinema", "City"));

        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0).withSecond(0).withNano(0);
        ShowRequest request = new ShowRequest(movie.getId(), theatre.getId(), 1, startTime, new BigDecimal("250.50"));
        ShowResponse showResponse = showService.createShow(request);
        showId = showResponse.getId();
    }

    @Test
    void userTokenOnAdminEndpointsReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/bookings")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/summary")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void summaryNumbersAreCorrectAfterOneConfirmedAndOneCancelledBooking() throws Exception {
        Show show = showRepository.findById(showId).orElseThrow();

        // One CONFIRMED booking (2 seats @ 250.50 = 501.00)
        showService.lockSeats(showId, new LockSeatsRequest(List.of("A1", "A2")), testUser.getEmail());
        bookingService.bookSeats(showId, new BookingRequest(List.of("A1", "A2"), PaymentMethod.UPI), testUser.getEmail());

        // One CANCELLED booking
        Booking cancelledBooking = new Booking(
                testUser,
                show,
                new BigDecimal("300.00"),
                PaymentMethod.CARD,
                BookingStatus.CANCELLED,
                LocalDateTime.now()
        );
        bookingRepository.save(cancelledBooking);

        mockMvc.perform(get("/api/admin/summary")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedBookings", is(1)))
                .andExpect(jsonPath("$.cancelledBookings", is(1)))
                .andExpect(jsonPath("$.seatsSold", is(2)))
                .andExpect(jsonPath("$.totalRevenue", is(501.00)));
    }

    @Test
    void bookingTotalIsExactForMultipleSeats() throws Exception {
        // 3 seats at 250.50 gives exact 751.50
        showService.lockSeats(showId, new LockSeatsRequest(List.of("A1", "A2", "A3")), testUser.getEmail());

        mockMvc.perform(post("/api/shows/" + showId + "/book")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatNumbers\":[\"A1\",\"A2\",\"A3\"],\"paymentMethod\":\"UPI\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount", is(751.50)));
    }
}
