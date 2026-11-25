package com.grabaseat;

import com.grabaseat.dto.ShowRequest;
import com.grabaseat.dto.ShowResponse;
import com.grabaseat.model.Movie;
import com.grabaseat.model.Role;
import com.grabaseat.model.SeatStatus;
import com.grabaseat.model.ShowSeat;
import com.grabaseat.model.Theatre;
import com.grabaseat.model.User;
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

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:seat_lock_testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "JWT_SECRET=supersecretkeyformyseatbookingapp12345"
})
class SeatLockIntegrationTest {

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
    private JwtUtil jwtUtil;

    private Long showId;
    private User userA;
    private User userB;
    private String tokenA;
    private String tokenB;

    @BeforeEach
    void setUp() {
        showSeatRepository.deleteAll();
        showRepository.deleteAll();
        movieRepository.deleteAll();
        theatreRepository.deleteAll();
        userRepository.deleteAll();

        Movie movie = movieRepository.save(new Movie("Inception", "English", "Sci-Fi", 148));
        Theatre theatre = theatreRepository.save(new Theatre("PVR Forum", "Bangalore"));

        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(16).withMinute(0).withSecond(0).withNano(0);
        ShowRequest request = new ShowRequest(movie.getId(), theatre.getId(), 1, startTime, BigDecimal.valueOf(250.0));
        ShowResponse showResponse = showService.createShow(request);
        showId = showResponse.getId();

        userA = userRepository.save(new User("User A", "userA@example.com", "passA123", Role.USER));
        userB = userRepository.save(new User("User B", "userB@example.com", "passB123", Role.USER));

        tokenA = jwtUtil.createToken(userA.getEmail(), userA.getRole());
        tokenB = jwtUtil.createToken(userB.getEmail(), userB.getRole());
    }

    @Test
    void userBCannotLockSeatLockedByUserA() throws Exception {
        // User A locks A1
        mockMvc.perform(post("/api/shows/" + showId + "/lock")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatNumbers\":[\"A1\"]}"))
                .andExpect(status().isOk());

        // User B tries to lock A1 -> 409
        mockMvc.perform(post("/api/shows/" + showId + "/lock")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatNumbers\":[\"A1\"]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Seat A1 is not available")));

        // Verify A's lock stays unchanged
        ShowSeat seatA1 = showSeatRepository.findByShowIdOrderBySeatNumberAsc(showId).stream()
                .filter(s -> s.getSeatNumber().equals("A1"))
                .findFirst()
                .orElseThrow();
        assertEquals(SeatStatus.LOCKED, seatA1.getStatus());
        assertEquals(userA.getId(), seatA1.getLockedByUserId());
    }

    @Test
    void ifOneSeatIsTakenNoneIsLocked() throws Exception {
        // User A locks A1
        mockMvc.perform(post("/api/shows/" + showId + "/lock")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatNumbers\":[\"A1\"]}"))
                .andExpect(status().isOk());

        // User B tries to lock A2 and A1 -> 409
        mockMvc.perform(post("/api/shows/" + showId + "/lock")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatNumbers\":[\"A2\",\"A1\"]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Seat A1 is not available")));

        // Verify A2 was NOT locked
        ShowSeat seatA2 = showSeatRepository.findByShowIdOrderBySeatNumberAsc(showId).stream()
                .filter(s -> s.getSeatNumber().equals("A2"))
                .findFirst()
                .orElseThrow();
        assertEquals(SeatStatus.AVAILABLE, seatA2.getStatus());
    }

    @Test
    void expiredLockCanBeLockedByAnotherUser() throws Exception {
        // Seat A4 has an expired lock by User A
        ShowSeat seatA4 = showSeatRepository.findByShowIdOrderBySeatNumberAsc(showId).stream()
                .filter(s -> s.getSeatNumber().equals("A4"))
                .findFirst()
                .orElseThrow();
        seatA4.setStatus(SeatStatus.LOCKED);
        seatA4.setLockedByUserId(userA.getId());
        seatA4.setLockedUntil(LocalDateTime.now().minusMinutes(10));
        showSeatRepository.save(seatA4);

        // User B locks A4 -> 200
        mockMvc.perform(post("/api/shows/" + showId + "/lock")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatNumbers\":[\"A4\"]}"))
                .andExpect(status().isOk());

        // Verify A4 is now locked by User B
        ShowSeat updatedSeatA4 = showSeatRepository.findByShowIdOrderBySeatNumberAsc(showId).stream()
                .filter(s -> s.getSeatNumber().equals("A4"))
                .findFirst()
                .orElseThrow();
        assertEquals(SeatStatus.LOCKED, updatedSeatA4.getStatus());
        assertEquals(userB.getId(), updatedSeatA4.getLockedByUserId());
    }

    @Test
    void lockWithNoTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/shows/" + showId + "/lock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatNumbers\":[\"A1\"]}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Please log in")));
    }
}
