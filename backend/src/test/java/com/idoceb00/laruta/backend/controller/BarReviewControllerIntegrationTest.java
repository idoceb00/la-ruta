package com.idoceb00.laruta.backend.controller;

import com.idoceb00.laruta.backend.model.Bar;
import com.idoceb00.laruta.backend.model.Community;
import com.idoceb00.laruta.backend.model.CommunityRole;
import com.idoceb00.laruta.backend.model.Membership;
import com.idoceb00.laruta.backend.model.User;
import com.idoceb00.laruta.backend.repository.BarRepository;
import com.idoceb00.laruta.backend.repository.CommunityRepository;
import com.idoceb00.laruta.backend.repository.MembershipRepository;
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

import static com.idoceb00.laruta.backend.testutil.TestAuth.authenticatedAs;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BarReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BarRepository barRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    private Bar bar;
    private User user;
    private Community community;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("Roberto", "password"));
        community = createCommunityWithAdmin("Los del barrio", "ABCD2345", user);
        bar = saveBar(community, "Anaikal");
    }

    // ----- Rating -----

    @Test
    void saveReview_whenNoPreviousReview_createsItAndReturnsStats() throws Exception {
        rate(bar.getId(), user.getId(), 8)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userRating").value(8))
                .andExpect(jsonPath("$.averageRating").value(8.0))
                .andExpect(jsonPath("$.ratingCount").value(1));
    }

    @Test
    void saveReview_whenSameUserRatesTwice_updatesInsteadOfDuplicating() throws Exception {
        rate(bar.getId(), user.getId(), 8);

        rate(bar.getId(), user.getId(), 6)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userRating").value(6))
                .andExpect(jsonPath("$.averageRating").value(6.0))
                .andExpect(jsonPath("$.ratingCount").value(1));
    }

    @Test
    void saveReview_whenTwoUsersRate_returnsAverage() throws Exception {
        User user2 = createUser("Alberto");
        rate(bar.getId(), user.getId(), 10);

        rate(bar.getId(), user2.getId(), 5)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userRating").value(5))
                .andExpect(jsonPath("$.averageRating").value(7.5))
                .andExpect(jsonPath("$.ratingCount").value(2));
    }

    @Test
    void saveReview_whenAverageHasManyDecimals_roundsToOneDecimal() throws Exception {
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
    void saveReview_whenRatingOutOfRange_returnsBadRequest() throws Exception {
        rate(bar.getId(), user.getId(), 11)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        rate(bar.getId(), user.getId(), -1)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void saveReview_whenRatingIsZeroOrTen_isAccepted() throws Exception {
        rate(bar.getId(), user.getId(), 0)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userRating").value(0));

        rate(bar.getId(), user.getId(), 10)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userRating").value(10));
    }

    // ----- Notes and optional rating -----

    @Test
    void saveReview_whenOnlyNotes_savesReviewWithoutAffectingStats() throws Exception {
        String body = """
                { "notes": "Great croquettes" }
                """;

        saveReview(bar.getId(), user.getId(), body)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Great croquettes"))
                .andExpect(jsonPath("$.userRating").value(nullValue()))
                .andExpect(jsonPath("$.averageRating").value(nullValue()))
                .andExpect(jsonPath("$.ratingCount").value(0));
    }

    @Test
    void saveReview_whenAddingRatingToNotesOnlyReview_countsIt() throws Exception {
        saveReview(bar.getId(), user.getId(), """
                { "notes": "Great croquettes" }
                """);

        saveReview(bar.getId(), user.getId(), """
                { "notes": "Great croquettes", "rating": 9 }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userRating").value(9))
                .andExpect(jsonPath("$.averageRating").value(9.0))
                .andExpect(jsonPath("$.ratingCount").value(1));
    }

    @Test
    void saveReview_whenRemovingRating_keepsNotesAndUncountsIt() throws Exception {
        saveReview(bar.getId(), user.getId(), """
                { "notes": "Great croquettes", "rating": 9 }
                """);

        saveReview(bar.getId(), user.getId(), """
                { "notes": "Great croquettes" }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Great croquettes"))
                .andExpect(jsonPath("$.userRating").value(nullValue()))
                .andExpect(jsonPath("$.averageRating").value(nullValue()))
                .andExpect(jsonPath("$.ratingCount").value(0));
    }

    @Test
    void saveReview_whenNotesChange_updatesThem() throws Exception {
        saveReview(bar.getId(), user.getId(), """
                { "notes": "Too crowded", "rating": 6 }
                """);

        saveReview(bar.getId(), user.getId(), """
                { "notes": "Quieter on weekdays", "rating": 6 }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Quieter on weekdays"))
                .andExpect(jsonPath("$.ratingCount").value(1));
    }

    @Test
    void saveReview_whenNoRatingAndNoNotes_returnsBadRequest() throws Exception {
        saveReview(bar.getId(), user.getId(), """
                { }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        saveReview(bar.getId(), user.getId(), """
                { "notes": "   " }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    // ----- Errors and auth -----

    @Test
    void saveReview_whenNotAuthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(put("/api/bars/{barId}/review", bar.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "rating": 8 }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void saveReview_whenBarDoesNotExist_returnsNotFound() throws Exception {
        Long nonExistentBarId = bar.getId() + 1000;

        rate(nonExistentBarId, user.getId(), 8)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Bar not found with id: " + nonExistentBarId));
    }

    // A valid token can outlive its user (e.g. the account was deleted)
    @Test
    void saveReview_whenUserDoesNotExist_returnsNotFound() throws Exception {
        Long nonExistentUserId = user.getId() + 1000;

        rate(bar.getId(), nonExistentUserId, 8)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("User not found with id: " + nonExistentUserId));
    }

    // ----- Delete -----

    @Test
    void deleteReview_whenReviewExists_returnsNoContent() throws Exception {
        rate(bar.getId(), user.getId(), 8);

        deleteReview(bar.getId(), user.getId())
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteReview_whenReviewDoesNotExist_returnsNotFound() throws Exception {
        deleteReview(bar.getId(), user.getId())
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReview_whenDeleted_isExcludedFromStats() throws Exception {
        rate(bar.getId(), user.getId(), 8);
        deleteReview(bar.getId(), user.getId());

        rate(bar.getId(), createUser("Alberto").getId(), 10)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userRating").value(10))
                .andExpect(jsonPath("$.averageRating").value(10.0))
                .andExpect(jsonPath("$.ratingCount").value(1));
    }

    @Test
    void deleteReview_whenNotesOnly_keepsOtherRatings() throws Exception {
        rate(bar.getId(), createUser("Alberto").getId(), 7);
        saveReview(bar.getId(), user.getId(), """
                { "notes": "Great croquettes" }
                """);

        deleteReview(bar.getId(), user.getId())
                .andExpect(status().isNoContent());

        rate(bar.getId(), createUser("Jorge").getId(), 9)
                .andExpect(jsonPath("$.averageRating").value(8.0))
                .andExpect(jsonPath("$.ratingCount").value(2));
    }

    @Test
    void deleteReview_whenNotAuthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/bars/{barId}/review", bar.getId()))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions rate(Long barId, Long userId, Integer rating) throws Exception {
        String body = """
                { "rating": %d }
                """.formatted(rating);
        return saveReview(barId, userId, body);
    }

    private ResultActions saveReview(Long barId, Long userId, String body) throws Exception {
        return mockMvc.perform(put("/api/bars/{barId}/review", barId)
                .with(authenticatedAs(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private ResultActions deleteReview(Long barId, Long userId) throws Exception {
        return mockMvc.perform(delete("/api/bars/{barId}/review", barId)
                .with(authenticatedAs(userId)));
    }

    private User createUser(String username) {
        User user = userRepository.save(new User(username, "password"));
        // Rating a bar requires being a member of its community
        membershipRepository.save(new Membership(community, user, CommunityRole.MEMBER));
        return user;
    }

    private Community createCommunityWithAdmin(String name, String inviteCode, User admin) {
        Community community = communityRepository.save(new Community(name, inviteCode));
        membershipRepository.save(new Membership(community, admin, CommunityRole.ADMIN));
        return community;
    }

    private Bar saveBar(Community owner, String name) {
        return barRepository.save(new Bar(
                owner,
                name,
                "León",
                "Calle Jesús Rubio",
                "Colegio San Claudio")
        );
    }
}