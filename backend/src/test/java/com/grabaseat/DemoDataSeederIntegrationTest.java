package com.grabaseat;

import com.grabaseat.config.DemoDataSeeder;
import com.grabaseat.repository.MovieRepository;
import com.grabaseat.repository.ShowRepository;
import com.grabaseat.repository.ShowSeatRepository;
import com.grabaseat.repository.TheatreRepository;
import com.grabaseat.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:seeder_testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "JWT_SECRET=supersecretkeyformyseatbookingapp12345",
        "SEED_DEMO_DATA=true"
})
class DemoDataSeederIntegrationTest {

    @Autowired
    private DemoDataSeeder demoDataSeeder;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TheatreRepository theatreRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void seedsDemoDataOnceWhenDatabaseIsEmpty() {
        assertEquals(6, movieRepository.count());
        assertEquals(3, theatreRepository.count());
        assertEquals(54, showRepository.count());
        assertEquals(4320, showSeatRepository.count());
        assertTrue(userRepository.existsByEmail("demo@example.com"));

        demoDataSeeder.run(new DefaultApplicationArguments());

        assertEquals(6, movieRepository.count());
        assertEquals(3, theatreRepository.count());
        assertEquals(54, showRepository.count());
        assertEquals(4320, showSeatRepository.count());
    }
}
