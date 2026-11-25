package com.grabaseat.config;

import com.grabaseat.dto.MovieRequest;
import com.grabaseat.dto.MovieResponse;
import com.grabaseat.dto.ShowRequest;
import com.grabaseat.dto.TheatreRequest;
import com.grabaseat.dto.TheatreResponse;
import com.grabaseat.model.Role;
import com.grabaseat.model.User;
import com.grabaseat.repository.MovieRepository;
import com.grabaseat.repository.TheatreRepository;
import com.grabaseat.repository.UserRepository;
import com.grabaseat.service.MovieService;
import com.grabaseat.service.ShowService;
import com.grabaseat.service.TheatreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final Environment environment;
    private final MovieRepository movieRepository;
    private final TheatreRepository theatreRepository;
    private final UserRepository userRepository;
    private final MovieService movieService;
    private final TheatreService theatreService;
    private final ShowService showService;
    private final BCryptPasswordEncoder passwordEncoder;

    public DemoDataSeeder(Environment environment,
                          MovieRepository movieRepository,
                          TheatreRepository theatreRepository,
                          UserRepository userRepository,
                          MovieService movieService,
                          TheatreService theatreService,
                          ShowService showService,
                          BCryptPasswordEncoder passwordEncoder) {
        this.environment = environment;
        this.movieRepository = movieRepository;
        this.theatreRepository = theatreRepository;
        this.userRepository = userRepository;
        this.movieService = movieService;
        this.theatreService = theatreService;
        this.showService = showService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        String seedFlag = environment.getProperty("SEED_DEMO_DATA");
        if (seedFlag == null || seedFlag.isBlank()) {
            seedFlag = System.getenv("SEED_DEMO_DATA");
        }
        if (!"true".equals(seedFlag)) {
            return;
        }
        if (movieRepository.count() > 0 || theatreRepository.count() > 0) {
            return;
        }

        List<MovieResponse> movies = List.of(
                movieService.createMovie(new MovieRequest("RRR", "Telugu", "Action", 187)),
                movieService.createMovie(new MovieRequest("KGF: Chapter 2", "Kannada", "Action", 168)),
                movieService.createMovie(new MovieRequest("Kantara", "Kannada", "Drama", 148)),
                movieService.createMovie(new MovieRequest("Jawan", "Hindi", "Action", 169)),
                movieService.createMovie(new MovieRequest("3 Idiots", "Hindi", "Comedy", 170)),
                movieService.createMovie(new MovieRequest("Dangal", "Hindi", "Drama", 161))
        );

        List<TheatreResponse> theatres = List.of(
                theatreService.createTheatre(new TheatreRequest("Sunset Talkies", "Mumbai")),
                theatreService.createTheatre(new TheatreRequest("Lotus Multiplex", "Pune")),
                theatreService.createTheatre(new TheatreRequest("Skyline Cinemas", "Bengaluru"))
        );

        List<BigDecimal> theatrePrices = List.of(
                new BigDecimal("200"),
                new BigDecimal("280"),
                new BigDecimal("350")
        );

        List<LocalTime> showTimes = List.of(
                LocalTime.of(10, 0),
                LocalTime.of(14, 0),
                LocalTime.of(18, 0)
        );

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        int showCount = 0;

        for (int day = 0; day < 3; day++) {
            LocalDate date = tomorrow.plusDays(day);
            for (int t = 0; t < theatres.size(); t++) {
                TheatreResponse theatre = theatres.get(t);
                BigDecimal price = theatrePrices.get(t);
                for (int slot = 0; slot < showTimes.size(); slot++) {
                    LocalDateTime startTime = LocalDateTime.of(date, showTimes.get(slot));
                    int screen1MovieIndex = (day + t + slot * 2) % movies.size();
                    int screen2MovieIndex = (screen1MovieIndex + 1) % movies.size();

                    showService.createShow(new ShowRequest(movies.get(screen1MovieIndex).getId(), theatre.getId(), 1, startTime, price));
                    showService.createShow(new ShowRequest(movies.get(screen2MovieIndex).getId(), theatre.getId(), 2, startTime, price));
                    showCount += 2;
                }
            }
        }

        // Public demo data for quick testing
        String demoEmail = "demo@example.com";
        if (!userRepository.existsByEmail(demoEmail)) {
            User demoUser = new User("Demo User", demoEmail, passwordEncoder.encode("Demo@1234"), Role.USER);
            userRepository.save(demoUser);
        }

        log.info("Demo data added: {} movies, {} theatres, {} shows.", movies.size(), theatres.size(), showCount);
    }
}
