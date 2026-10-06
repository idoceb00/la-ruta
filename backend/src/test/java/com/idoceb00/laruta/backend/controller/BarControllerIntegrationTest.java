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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.idoceb00.laruta.backend.testutil.TestAuth.authenticatedAs;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BarControllerIntegrationTest {

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
                "Colegio San Claudio")
        );
        user = createUser("Roberto");
    }

    @Test
    void getBar_whenHasRatings_includesStats() throws Exception {
        rate(bar.getId(), user.getId(), 6);
        rate(bar.getId(), createUser("Alberto").getId(), 9);

        getBar(bar.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Anaikal"))
                .andExpect(jsonPath("$.averageRating").value(7.5))
                .andExpect(jsonPath("$.ratingCount").value(2));
    }

    @Test
    void getBar_whenNoRatings_returnsNullAverageAndZeroCount() throws Exception {
        getBar(bar.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(nullValue()))
                .andExpect(jsonPath("$.ratingCount").value(0));
    }

    @Test
    void getBars_returnsStatsForEachBar() throws Exception {
        Bar otherBar = barRepository.save(new Bar(
                "El Rebote",
                "León",
                "Plaza San Martín",
                "Barrio Húmedo")
        );
        rate(bar.getId(), user.getId(), 8);

        mockMvc.perform(get("/api/bars").with(authenticatedAs(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == %d)].averageRating", bar.getId()).value(contains(8.0)))
                .andExpect(jsonPath("$[?(@.id == %d)].ratingCount", bar.getId()).value(contains(1)))
                .andExpect(jsonPath("$[?(@.id == %d)].averageRating", otherBar.getId()).value(contains(nullValue())))
                .andExpect(jsonPath("$[?(@.id == %d)].ratingCount", otherBar.getId()).value(contains(0)));
    }

    private ResultActions getBar(Long barId) throws Exception {
        return mockMvc.perform(get("/api/bars/{id}", barId)
                .with(authenticatedAs(user.getId())));
    }

    private void rate(Long barId, Long userId, Integer rating) throws Exception {
        String body = """
                { "rating": %d }
                """.formatted(rating);
        mockMvc.perform(put("/api/bars/{barId}/review", barId)
                .with(authenticatedAs(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk());
    }

    private User createUser(String username) {
        return userRepository.save(new User(username, "password"));
    }
}