package com.idoceb00.laruta.backend.controller;

import com.idoceb00.laruta.backend.model.Bar;
import com.idoceb00.laruta.backend.model.BarTapa;
import com.idoceb00.laruta.backend.model.Community;
import com.idoceb00.laruta.backend.model.CommunityRole;
import com.idoceb00.laruta.backend.model.Membership;
import com.idoceb00.laruta.backend.model.Tapa;
import com.idoceb00.laruta.backend.model.User;
import com.idoceb00.laruta.backend.repository.BarRepository;
import com.idoceb00.laruta.backend.repository.BarTapaRepository;
import com.idoceb00.laruta.backend.repository.CommunityRepository;
import com.idoceb00.laruta.backend.repository.MembershipRepository;
import com.idoceb00.laruta.backend.repository.TapaRepository;
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
class TapaReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BarRepository barRepository;

    @Autowired
    private TapaRepository tapaRepository;

    @Autowired
    private BarTapaRepository barTapaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    private Bar bar;
    private Tapa tapa;
    private User user;

    @BeforeEach
    void setUp() {
        user = createUser("Roberto");
        Community community = createCommunityWithAdmin("Los del barrio", "ABCD2345", user);
        bar = saveBar(community, "Anaikal");
        tapa = tapaRepository.save(new Tapa("alitas"));
        barTapaRepository.save(new BarTapa(bar, tapa));
    }

    // ----- Rating -----

    @Test
    void saveReview_whenNoPreviousReview_createsItAndReturnsStats() throws Exception {
        rate(user.getId(), 4)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.barId").value(bar.getId()))
                .andExpect(jsonPath("$.tapaId").value(tapa.getId()))
                .andExpect(jsonPath("$.userRating").value(4))
                .andExpect(jsonPath("$.averageRating").value(4.0))
                .andExpect(jsonPath("$.ratingCount").value(1));
    }

    @Test
    void saveReview_whenSameUserRatesTwice_updatesInsteadOfDuplicating() throws Exception {
        rate(user.getId(), 4);

        rate(user.getId(), 2)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userRating").value(2))
                .andExpect(jsonPath("$.averageRating").value(2.0))
                .andExpect(jsonPath("$.ratingCount").value(1));
    }

    @Test
    void saveReview_whenTwoUsersRate_returnsAverage() throws Exception {
        rate(user.getId(), 5);

        rate(createUser("Alberto").getId(), 2)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(3.5))
                .andExpect(jsonPath("$.ratingCount").value(2));
    }

    @Test
    void saveReview_whenRatingOutOfRange_returnsBadRequest() throws Exception {
        rate(user.getId(), 6)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        rate(user.getId(), -1)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void saveReview_whenRatingIsZeroOrFive_isAccepted() throws Exception {
        rate(user.getId(), 0)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userRating").value(0));

        rate(user.getId(), 5)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userRating").value(5));
    }

    // ----- Fav, notes and optional rating -----

    @Test
    void saveReview_whenOnlyFav_savesReviewWithoutAffectingStats() throws Exception {
        saveReview(user.getId(), """
                { "fav": true }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fav").value(true))
                .andExpect(jsonPath("$.userRating").value(nullValue()))
                .andExpect(jsonPath("$.averageRating").value(nullValue()))
                .andExpect(jsonPath("$.ratingCount").value(0));
    }

    @Test
    void saveReview_whenOnlyNotes_savesReviewWithoutAffectingStats() throws Exception {
        saveReview(user.getId(), """
                { "notes": "Ask for extra sauce" }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Ask for extra sauce"))
                .andExpect(jsonPath("$.fav").value(false))
                .andExpect(jsonPath("$.ratingCount").value(0));
    }

    @Test
    void saveReview_whenAddingRatingToFavOnlyReview_countsIt() throws Exception {
        saveReview(user.getId(), """
                { "fav": true }
                """);

        saveReview(user.getId(), """
                { "fav": true, "rating": 5 }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fav").value(true))
                .andExpect(jsonPath("$.userRating").value(5))
                .andExpect(jsonPath("$.averageRating").value(5.0))
                .andExpect(jsonPath("$.ratingCount").value(1));
    }

    @Test
    void saveReview_whenRemovingRating_keepsFavAndUncountsIt() throws Exception {
        saveReview(user.getId(), """
                { "fav": true, "rating": 5 }
                """);

        saveReview(user.getId(), """
                { "fav": true }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fav").value(true))
                .andExpect(jsonPath("$.userRating").value(nullValue()))
                .andExpect(jsonPath("$.averageRating").value(nullValue()))
                .andExpect(jsonPath("$.ratingCount").value(0));
    }

    @Test
    void saveReview_whenUnmarkingFav_keepsRating() throws Exception {
        saveReview(user.getId(), """
                { "fav": true, "rating": 4 }
                """);

        saveReview(user.getId(), """
                { "fav": false, "rating": 4 }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fav").value(false))
                .andExpect(jsonPath("$.userRating").value(4))
                .andExpect(jsonPath("$.ratingCount").value(1));
    }

    @Test
    void saveReview_whenNoRatingNoNotesAndNoFav_returnsBadRequest() throws Exception {
        saveReview(user.getId(), """
                { }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        saveReview(user.getId(), """
                { "fav": false, "notes": "   " }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    // ----- Errors and auth -----

    @Test
    void saveReview_whenTapaNotInBar_returnsNotFound() throws Exception {
        Tapa otherTapa = tapaRepository.save(new Tapa("croquetas"));

        saveReview(bar.getId(), otherTapa.getId(), user.getId(), """
                { "rating": 4 }
                """)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Tapa with id: " + otherTapa.getId() + " not found in bar with id: " + bar.getId()));
    }

    @Test
    void saveReview_whenBarDoesNotExist_returnsNotFound() throws Exception {
        Long nonExistentBarId = bar.getId() + 1000;

        saveReview(nonExistentBarId, tapa.getId(), user.getId(), """
                { "rating": 4 }
                """)
                .andExpect(status().isNotFound());
    }

    // A valid token can outlive its user (e.g. the account was deleted)
    @Test
    void saveReview_whenUserDoesNotExist_returnsNotFound() throws Exception {
        Long nonExistentUserId = user.getId() + 1000;

        rate(nonExistentUserId, 4)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("User not found with id: " + nonExistentUserId));
    }

    @Test
    void saveReview_whenNotAuthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(put("/api/bars/{barId}/tapas/{tapaId}/review", bar.getId(), tapa.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "rating": 4 }
                                """))
                .andExpect(status().isUnauthorized());
    }

    // ----- Delete -----

    @Test
    void deleteReview_whenReviewExists_returnsNoContent() throws Exception {
        rate(user.getId(), 4);

        deleteReview(user.getId())
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteReview_whenReviewDoesNotExist_returnsNotFound() throws Exception {
        deleteReview(user.getId())
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReview_whenDeleted_isExcludedFromStats() throws Exception {
        rate(user.getId(), 1);
        deleteReview(user.getId());

        rate(createUser("Alberto").getId(), 5)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(5.0))
                .andExpect(jsonPath("$.ratingCount").value(1));
    }

    @Test
    void deleteReview_whenFavOnly_keepsOtherRatings() throws Exception {
        rate(createUser("Alberto").getId(), 3);
        saveReview(user.getId(), """
                { "fav": true }
                """);

        deleteReview(user.getId())
                .andExpect(status().isNoContent());

        rate(createUser("Jorge").getId(), 5)
                .andExpect(jsonPath("$.averageRating").value(4.0))
                .andExpect(jsonPath("$.ratingCount").value(2));
    }

    @Test
    void deleteReview_whenTapaNotInBar_returnsNotFound() throws Exception {
        Tapa otherTapa = tapaRepository.save(new Tapa("croquetas"));

        mockMvc.perform(delete("/api/bars/{barId}/tapas/{tapaId}/review", bar.getId(), otherTapa.getId())
                        .with(authenticatedAs(user.getId())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReview_whenNotAuthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/bars/{barId}/tapas/{tapaId}/review", bar.getId(), tapa.getId()))
                .andExpect(status().isUnauthorized());
    }

    // Helpers target the bar and tapa from setUp unless explicit ids are given

    private ResultActions rate(Long userId, Integer rating) throws Exception {
        String body = """
                { "rating": %d }
                """.formatted(rating);
        return saveReview(userId, body);
    }

    private ResultActions saveReview(Long userId, String body) throws Exception {
        return saveReview(bar.getId(), tapa.getId(), userId, body);
    }

    private ResultActions saveReview(Long barId, Long tapaId, Long userId, String body) throws Exception {
        return mockMvc.perform(put("/api/bars/{barId}/tapas/{tapaId}/review", barId, tapaId)
                .with(authenticatedAs(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private ResultActions deleteReview(Long userId) throws Exception {
        return mockMvc.perform(delete("/api/bars/{barId}/tapas/{tapaId}/review", bar.getId(), tapa.getId())
                .with(authenticatedAs(userId)));
    }

    private User createUser(String username) {
        return userRepository.save(new User(username, "password"));
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