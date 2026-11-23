package com.grabaseat;

import com.grabaseat.dto.ShowRequest;
import com.grabaseat.dto.ShowResponse;
import com.grabaseat.model.Movie;
import com.grabaseat.model.SeatStatus;
import com.grabaseat.model.ShowSeat;
import com.grabaseat.model.Theatre;
import com.grabaseat.repository.MovieRepository;
import com.grabaseat.repository.ShowSeatRepository;
import com.grabaseat.repository.TheatreRepository;
import com.grabaseat.service.ShowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:seat_testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "JWT_SECRET=supersecretkeyformyseatbookingapp12345"
})
class SeatIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShowService showService;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TheatreRepository theatreRepository;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    private Movie movie;
    private Theatre theatre;

    @BeforeEach
    void setUp() {
        movie = movieRepository.save(new Movie("The Dark Knight", "English", "Action", 152));
        theatre = theatreRepository.save(new Theatre("PVR Forum", "Bangalore"));
    }

    @Test
    void showSeatsCreationAndStatusMappingRules() throws Exception {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0).withSecond(0).withNano(0);
        ShowRequest request = new ShowRequest(movie.getId(), theatre.getId(), 1, startTime, 200.0);
        ShowResponse showResponse = showService.createShow(request);
        Long showId = showResponse.getId();

        // 1. Verify 80 seats created and seat map returns 80 AVAILABLE seats
        mockMvc.perform(get("/api/shows/" + showId + "/seats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(80)))
                .andExpect(jsonPath("$[0].status", is("AVAILABLE")));

        // 2. Lock a seat with lockedUntil in the past, and lock a seat with lockedUntil in the future
        List<ShowSeat> seats = showSeatRepository.findByShowIdOrderBySeatNumberAsc(showId);
        ShowSeat pastLockedSeat = seats.get(0);
        pastLockedSeat.setStatus(SeatStatus.LOCKED);
        pastLockedSeat.setLockedUntil(LocalDateTime.now().minusMinutes(5));
        pastLockedSeat.setLockedByUserId(100L);
        showSeatRepository.save(pastLockedSeat);

        ShowSeat futureLockedSeat = seats.get(1);
        futureLockedSeat.setStatus(SeatStatus.LOCKED);
        futureLockedSeat.setLockedUntil(LocalDateTime.now().plusMinutes(10));
        futureLockedSeat.setLockedByUserId(101L);
        showSeatRepository.save(futureLockedSeat);

        // 3. Verify past locked seat is returned as AVAILABLE, future locked seat is returned as LOCKED, and no lockedByUserId is returned
        mockMvc.perform(get("/api/shows/" + showId + "/seats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(80)))
                .andExpect(jsonPath("$[?(@.seatNumber == '" + pastLockedSeat.getSeatNumber() + "')].status").value("AVAILABLE"))
                .andExpect(jsonPath("$[?(@.seatNumber == '" + futureLockedSeat.getSeatNumber() + "')].status").value("LOCKED"))
                .andExpect(jsonPath("$[0].lockedByUserId").doesNotExist());
    }
}
