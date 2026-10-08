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
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private BarRepository barRepository;

    @Autowired
    private TapaRepository tapaRepository;

    @Autowired
    private BarTapaRepository barTapaRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = createUser("Roberto");
    }

    // ----- Get current user -----

    @Test
    void getCurrentUser_whenAuthenticated_returnsOwnUser() throws Exception {
        getMe(user.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.username").value("Roberto"));
    }

    @Test
    void getCurrentUser_whenNotAuthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    // A valid token can outlive its user (e.g. the account was deleted)
    @Test
    void getCurrentUser_whenUserDoesNotExist_returnsNotFound() throws Exception {
        Long nonExistentUserId = user.getId() + 1000;

        getMe(nonExistentUserId)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("User not found with id: " + nonExistentUserId));
    }

    // ----- Delete current user -----

    @Test
    void deleteCurrentUser_whenAuthenticated_deletesOnlyOwnUser() throws Exception {
        User otherUser = createUser("Alberto");

        deleteMe(user.getId())
                .andExpect(status().isNoContent());

        getMe(user.getId())
                .andExpect(status().isNotFound());
        getMe(otherUser.getId())
                .andExpect(status().isOk());
    }

    @Test
    void deleteCurrentUser_whenNotAuthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteCurrentUser_whenUserDoesNotExist_returnsNotFound() throws Exception {
        Long nonExistentUserId = user.getId() + 1000;

        deleteMe(nonExistentUserId)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: " + nonExistentUserId));
    }

    // ----- Delete current user: communities -----
    // Deleting an account is equivalent to leaving every community first,
    // so rating aggregates are recalculated in Java instead of relying on DB cascades

    @Test
    void deleteCurrentUser_whenOnlyMemberOfCommunity_deletesCommunityAndItsBars() throws Exception {
        Community community = createCommunityWithAdmin("Los del barrio", "ABCD2345", user);
        Bar bar = saveBar(community, "Anaikal");

        deleteMe(user.getId())
                .andExpect(status().isNoContent());

        assertThat(communityRepository.existsById(community.getId())).isFalse();
        assertThat(barRepository.existsById(bar.getId())).isFalse();
    }

    // The member who joined first (lowest membership id) becomes the new admin
    @Test
    void deleteCurrentUser_whenAdminOfCommunity_promotesOldestMember() throws Exception {
        Community community = createCommunityWithAdmin("Los del barrio", "ABCD2345", user);
        User alberto = createUser("Alberto");
        addMember(community, alberto);
        addMember(community, createUser("Jorge"));

        deleteMe(user.getId())
                .andExpect(status().isNoContent());

        assertThat(communityRepository.existsById(community.getId())).isTrue();
        assertThat(membershipRepository.findByCommunityIdAndUserId(community.getId(), alberto.getId()))
                .get()
                .extracting(Membership::getRole)
                .isEqualTo(CommunityRole.ADMIN);
    }

    @Test
    void deleteCurrentUser_whenUserHadBarReviews_removesThemFromStats() throws Exception {
        User alberto = createUser("Alberto");
        Community community = createCommunityWithAdmin("Los del barrio", "ABCD2345", alberto);
        addMember(community, user);
        Bar bar = saveBar(community, "Anaikal");
        rateBar(bar.getId(), user.getId(), 2);
        rateBar(bar.getId(), alberto.getId(), 8);

        deleteMe(user.getId())
                .andExpect(status().isNoContent());

        rateBar(bar.getId(), alberto.getId(), 8)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(8.0))
                .andExpect(jsonPath("$.ratingCount").value(1));
    }

    @Test
    void deleteCurrentUser_whenUserHadTapaReviews_removesThemFromStats() throws Exception {
        User alberto = createUser("Alberto");
        Community community = createCommunityWithAdmin("Los del barrio", "ABCD2345", alberto);
        addMember(community, user);
        Bar bar = saveBar(community, "Anaikal");
        Tapa tapa = tapaRepository.save(new Tapa("alitas"));
        barTapaRepository.save(new BarTapa(bar, tapa));
        rateTapa(bar.getId(), tapa.getId(), user.getId(), 1);
        rateTapa(bar.getId(), tapa.getId(), alberto.getId(), 5);

        deleteMe(user.getId())
                .andExpect(status().isNoContent());

        rateTapa(bar.getId(), tapa.getId(), alberto.getId(), 5)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(5.0))
                .andExpect(jsonPath("$.ratingCount").value(1));
    }

    // ----- Request helpers -----

    private ResultActions getMe(Long userId) throws Exception {
        return mockMvc.perform(get("/api/users/me")
                .with(authenticatedAs(userId)));
    }

    private ResultActions deleteMe(Long userId) throws Exception {
        return mockMvc.perform(delete("/api/users/me")
                .with(authenticatedAs(userId)));
    }

    private ResultActions rateBar(Long barId, Long userId, Integer rating) throws Exception {
        return mockMvc.perform(put("/api/bars/{barId}/review", barId)
                .with(authenticatedAs(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "rating": %d }
                        """.formatted(rating)));
    }

    private ResultActions rateTapa(Long barId, Long tapaId, Long userId, Integer rating) throws Exception {
        return mockMvc.perform(put("/api/bars/{barId}/tapas/{tapaId}/review", barId, tapaId)
                .with(authenticatedAs(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "rating": %d }
                        """.formatted(rating)));
    }

    // ----- Data helpers -----

    private User createUser(String username) {
        return userRepository.save(new User(username, "password"));
    }

    private Community createCommunityWithAdmin(String name, String inviteCode, User admin) {
        Community community = communityRepository.save(new Community(name, inviteCode));
        membershipRepository.save(new Membership(community, admin, CommunityRole.ADMIN));
        return community;
    }

    private void addMember(Community community, User member) {
        membershipRepository.save(new Membership(community, member, CommunityRole.MEMBER));
    }

    private Bar saveBar(Community community, String name) {
        return barRepository.save(new Bar(
                community,
                name,
                "León",
                "Calle Jesús Rubio",
                "Colegio San Claudio")
        );
    }
}