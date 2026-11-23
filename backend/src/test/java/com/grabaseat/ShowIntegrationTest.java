package com.grabaseat;

import com.grabaseat.model.Movie;
import com.grabaseat.model.Role;
import com.grabaseat.model.Theatre;
import com.grabaseat.repository.MovieRepository;
import com.grabaseat.repository.TheatreRepository;
import com.grabaseat.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "JWT_SECRET=supersecretkeyformyseatbookingapp12345"
})
class ShowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TheatreRepository theatreRepository;

    private Movie movie;
    private Theatre theatre;
    private String adminToken;

    @BeforeEach
    void setUp() {
        movie = movieRepository.save(new Movie("Interstellar", "English", "Sci-Fi", 169));
        theatre = theatreRepository.save(new Theatre("PVR Cinemas", "Mumbai"));
        adminToken = jwtUtil.createToken("admin@example.com", Role.ADMIN);
    }

    @Test
    void showOverlapRuleEnforced() throws Exception {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0).withSecond(0).withNano(0);

        String firstShowJson = String.format("""
                {
                    "movieId": %d,
                    "theatreId": %d,
                    "screenNumber": 1,
                    "startTime": "%s",
                    "price": 250.0
                }
                """, movie.getId(), theatre.getId(), startTime);

        mockMvc.perform(post("/api/admin/shows")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(firstShowJson))
                .andExpect(status().isCreated());

        LocalDateTime overlappingTime = startTime.plusMinutes(30);
        String overlappingShowJson = String.format("""
                {
                    "movieId": %d,
                    "theatreId": %d,
                    "screenNumber": 1,
                    "startTime": "%s",
                    "price": 250.0
                }
                """, movie.getId(), theatre.getId(), overlappingTime);

        mockMvc.perform(post("/api/admin/shows")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(overlappingShowJson))
                .andExpect(status().isConflict());

        String differentScreenShowJson = String.format("""
                {
                    "movieId": %d,
                    "theatreId": %d,
                    "screenNumber": 2,
                    "startTime": "%s",
                    "price": 250.0
                }
                """, movie.getId(), theatre.getId(), startTime);

        mockMvc.perform(post("/api/admin/shows")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(differentScreenShowJson))
                .andExpect(status().isCreated());
    }
}
