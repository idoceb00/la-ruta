package com.idoceb00.laruta.backend.controller;

import com.idoceb00.laruta.backend.model.Bar;
import com.idoceb00.laruta.backend.model.User;
import com.idoceb00.laruta.backend.repository.BarRepository;
import com.idoceb00.laruta.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BarRatingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BarRepository barRepository;

    @Autowired
    private UserRepository userRepository;

    private Bar bar;
    private User user;

    @BeforeEach
    void setUp() {
        bar = barRepository.save(new Bar(
                "Anaikal",
                "León",
                "Calle Jesús Rubio",
                "Colegio San Claudio",
                "El arroz picante está realmente bueno.")
        );
        user = userRepository.save(new User("Roberto", "12345"));

    }

    @Test
    void rateBar_whenNoPreviousRating_createsItAndReturnsStats() throws Exception {
        String body = """
                { "rating": 8, "userId": %d }
                """.formatted(user.getId());

        mockMvc.perform(put("/api/bars/{barId}/rating", bar.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userRating").value(8))
                .andExpect(jsonPath("$.averageRating").value(8.0))
                .andExpect(jsonPath("$.ratingCount").value(1));
    }
}
