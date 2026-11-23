package com.grabaseat;

import com.grabaseat.model.Role;
import com.grabaseat.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class MovieIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void movieEndpointsEnforceAccessRules() throws Exception {
        String userToken = jwtUtil.createToken("user@example.com", Role.USER);
        String movieJson = """
                {
                    "title": "Inception",
                    "language": "English",
                    "genre": "Sci-Fi",
                    "durationMinutes": 148
                }
                """;

        mockMvc.perform(post("/api/admin/movies")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(movieJson))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk());
    }
}
