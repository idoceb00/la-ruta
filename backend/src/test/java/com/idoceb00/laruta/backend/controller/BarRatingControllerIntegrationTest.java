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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
        user = createUser("Roberto");
    }

    @Test
    void rateBar_whenNoPreviousRating_createsItAndReturnsStats() throws Exception {
        rate(bar.getId(), user.getId(), 8)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userRating").value(8))
                .andExpect(jsonPath("$.averageRating").value(8.0))
                .andExpect(jsonPath("$.ratingCount").value(1));
    }

    @Test
    void rateBar_whenSameUserRatesTwice_updatesInsteadOfDuplicating() throws Exception {
        rate(bar.getId(), user.getId(), 8);

        rate(bar.getId(), user.getId(), 6)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userRating").value(6))
                .andExpect(jsonPath("$.averageRating").value(6.0))
                .andExpect(jsonPath("$.ratingCount").value(1));
    }

    @Test
    void rateBar_whenTwoUsersRate_returnsAverage() throws Exception {
        User user2 = createUser("Alberto");
        rate(bar.getId(), user.getId(), 10);

        rate(bar.getId(), user2.getId(), 5)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userRating").value(5))
                .andExpect(jsonPath("$.averageRating").value(7.5))
                .andExpect(jsonPath("$.ratingCount").value(2));
    }

    @Test
    void rateBar_whenAverageHasManyDecimals_roundsToOneDecimal() throws Exception {
        User user2 = createUser("Alberto");
        User user3 = createUser("Jorge");
        rate(bar.getId(), user.getId(), 8);
        rate(bar.getId(), user2.getId(), 7);

        rate(bar.getId(), user3.getId(), 8)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(7.7))
                .andExpect(jsonPath("$.ratingCount").value(3));
    }

    @Test
    void rateBar_whenRatingOutOfRange_returnsBadRequest() throws Exception {
        rate(bar.getId(), user.getId(), 11)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        rate(bar.getId(), user.getId(), -1)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void rateBar_whenRatingIsZeroOrTen_isAccepted() throws Exception {
        rate(bar.getId(), user.getId(), 0)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userRating").value(0));

        rate(bar.getId(), user.getId(), 10)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userRating").value(10));
    }

    @Test
    void rateBar_whenRatingMissing_returnsBadRequest() throws Exception {
        String body = """
                { "userId": %d }
                """.formatted(user.getId());

        rateWithBody(bar.getId(), body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void rateBar_whenUserIdMissing_returnsBadRequest() throws Exception {
        String body = """
                { "rating": 5 }
                """;

        rateWithBody(bar.getId(), body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void rateBar_whenBarDoesNotExist_returnsNotFound() throws Exception {
        Long nonExistentBarId = bar.getId() + 1000;

        rate(nonExistentBarId, user.getId(), 8)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Bar not found with id: " + nonExistentBarId));
    }

    @Test
    void rateBar_whenUserDoesNotExist_returnsNotFound() throws Exception {
        Long nonExistentUserId = user.getId() + 1000;

        rate(bar.getId(), nonExistentUserId, 8)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("User not found with id: " + nonExistentUserId));
    }

    @Test
    void deleteRating_whenRatingExists_returnsNoContent() throws Exception {
        rate(bar.getId(), user.getId(), 8);

        deleteRating(bar.getId(), user.getId())
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteRating_whenRatingDoesNotExist_returnsNotFound() throws Exception {
        deleteRating(bar.getId(), user.getId())
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRating_whenDeleted_isExcludedFromStats() throws Exception {
        rate(bar.getId(), user.getId(), 8);
        deleteRating(bar.getId(), user.getId());

        rate(bar.getId(), createUser("Alberto").getId(), 10)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userRating").value(10))
                .andExpect(jsonPath("$.averageRating").value(10.0))
                .andExpect(jsonPath("$.ratingCount").value(1));
    }

    @Test
    void deleteRating_whenUserIdMissing_returnsBadRequest() throws Exception {
        mockMvc.perform(delete("/api/bars/{barId}/rating", bar.getId()))
                .andExpect(status().isBadRequest());
    }

    private ResultActions rate(Long barId, Long userId, Integer rating) throws Exception {
        String body = """
                { "rating": %d, "userId": %d }
                """.formatted(rating, userId);
        return rateWithBody(barId, body);
    }

    private ResultActions rateWithBody(Long barId, String body) throws Exception {
        return mockMvc.perform(put("/api/bars/{barId}/rating", barId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private ResultActions deleteRating(Long barId, Long userId) throws Exception {
        return mockMvc.perform(delete("/api/bars/{barId}/rating", barId)
                .param("userId", userId.toString()));
    }

    private User createUser(String username) {
        return userRepository.save(new User(username, "password"));
    }
}