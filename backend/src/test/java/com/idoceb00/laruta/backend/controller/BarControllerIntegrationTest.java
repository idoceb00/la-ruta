package com.idoceb00.laruta.backend.controller;

import com.idoceb00.laruta.backend.TestcontainersConfiguration;
import com.idoceb00.laruta.backend.model.Bar;
import com.idoceb00.laruta.backend.model.Community;
import com.idoceb00.laruta.backend.model.CommunityRole;
import com.idoceb00.laruta.backend.model.Membership;
import com.idoceb00.laruta.backend.model.User;
import com.idoceb00.laruta.backend.repository.BarRepository;
import com.idoceb00.laruta.backend.repository.CommunityRepository;
import com.idoceb00.laruta.backend.repository.MembershipRepository;
import com.idoceb00.laruta.backend.repository.UserRepository;
import com.idoceb00.laruta.backend.model.BarTapa;
import com.idoceb00.laruta.backend.model.Tapa;
import com.idoceb00.laruta.backend.model.TapaReview;
import com.idoceb00.laruta.backend.repository.BarTapaRepository;
import com.idoceb00.laruta.backend.repository.TapaRepository;
import com.idoceb00.laruta.backend.repository.TapaReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
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
@Import(TestcontainersConfiguration.class)
class BarControllerIntegrationTest {

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

    @Autowired
    private TapaRepository tapaRepository;

    @Autowired
    private BarTapaRepository barTapaRepository;

    @Autowired
    private TapaReviewRepository tapaReviewRepository;

    private Bar bar;
    private User user;
    private Community community;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("Roberto", "password"));
        community = createCommunityWithAdmin("Los del barrio", "ABCD2345", user);
        bar = saveBar(community, "Anaikal");
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
        Bar otherBar = saveBar(community, "El Rebote");
        rate(bar.getId(), user.getId(), 8);

        mockMvc.perform(get("/api/communities/{communityId}/bars", community.getId())
                        .with(authenticatedAs(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == %d)].averageRating", bar.getId()).value(contains(8.0)))
                .andExpect(jsonPath("$[?(@.id == %d)].ratingCount", bar.getId()).value(contains(1)))
                .andExpect(jsonPath("$[?(@.id == %d)].averageRating", otherBar.getId()).value(contains(nullValue())))
                .andExpect(jsonPath("$[?(@.id == %d)].ratingCount", otherBar.getId()).value(contains(0)));
    }

    @Test
    void getBar_whenNoReviewNorTapas_returnsNullReviewAndEmptyTapas() throws Exception {
        getBar(bar.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.myReview").value(nullValue()))
                .andExpect(jsonPath("$.tapas").isEmpty());
    }

    @Test
    void getBar_whenUserReviewed_includesOwnReview() throws Exception {
        review(bar.getId(), user.getId(), 8, "Buenas bravas");

        getBar(bar.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.myReview.rating").value(8))
                .andExpect(jsonPath("$.myReview.notes").value("Buenas bravas"));
    }

    @Test
    void getBar_whenOnlyOtherMemberReviewed_doesNotExposeTheirReview() throws Exception {
        rate(bar.getId(), createUser("Alberto").getId(), 9);

        getBar(bar.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.myReview").value(nullValue()))
                .andExpect(jsonPath("$.ratingCount").value(1));
    }

    @Test
    void getBar_withTapas_returnsThemSortedWithOwnTapaReview() throws Exception {
        BarTapa tortilla = addTapa(bar, "Tortilla de prueba");
        addTapa(bar, "Bravas de prueba");
        tapaReviewRepository.save(new TapaReview(tortilla, user, null, 4, true));

        getBar(bar.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tapas.length()").value(2))
                .andExpect(jsonPath("$.tapas[0].tapaName").value("Bravas de prueba"))
                .andExpect(jsonPath("$.tapas[0].myReview").value(nullValue()))
                .andExpect(jsonPath("$.tapas[1].tapaName").value("Tortilla de prueba"))
                .andExpect(jsonPath("$.tapas[1].myReview.rating").value(4))
                .andExpect(jsonPath("$.tapas[1].myReview.fav").value(true));
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

    private void review(Long barId, Long userId, Integer rating, String notes) throws Exception {
        String body = """
            { "rating": %d, "notes": "%s" }
            """.formatted(rating, notes);
        mockMvc.perform(put("/api/bars/{barId}/review", barId)
                        .with(authenticatedAs(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    private BarTapa addTapa(Bar owner, String tapaName) {
        Tapa tapa = tapaRepository.save(new Tapa(tapaName));
        return barTapaRepository.save(new BarTapa(owner, tapa));
    }
}