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
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommunityBarsIntegrationTest {

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
    private Community community;

    @BeforeEach
    void setUp() {
        user = createUser("Roberto");
        community = createCommunityWithAdmin("Los del barrio", "ABCD2345", user);
    }

    // ----- Create and list bars of a community -----

    @Test
    void createBar_whenMember_returnsCreatedInCommunity() throws Exception {
        createBar(community.getId(), user.getId(), "Anaikal")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Anaikal"))
                .andExpect(jsonPath("$.communityId").value(community.getId()));
    }

    @Test
    void createBar_whenNotMember_returnsForbidden() throws Exception {
        User outsider = createUser("Intruso");

        createBar(community.getId(), outsider.getId(), "Anaikal")
                .andExpect(status().isForbidden());
    }

    @Test
    void createBar_whenCommunityDoesNotExist_returnsNotFound() throws Exception {
        createBar(999_999L, user.getId(), "Anaikal")
                .andExpect(status().isNotFound());
    }

    @Test
    void createBar_whenNameBlank_returnsBadRequest() throws Exception {
        createBar(community.getId(), user.getId(), "   ")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void getBars_whenMember_returnsOnlyBarsOfThatCommunity() throws Exception {
        Community other = createCommunityWithAdmin("Los del curro", "WXYZ6789", createUser("Alberto"));
        saveBar(community, "Anaikal");
        saveBar(community, "La Competencia");
        saveBar(other, "El Rebote");

        getBars(community.getId(), user.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Anaikal", "La Competencia")));
    }

    // The same physical bar can exist in several communities: search never crosses them
    @Test
    void getBars_whenQuery_searchesOnlyWithinCommunity() throws Exception {
        Community other = createCommunityWithAdmin("Los del curro", "WXYZ6789", createUser("Alberto"));
        saveBar(community, "Anaikal");
        saveBar(community, "La Competencia");
        saveBar(other, "Anaikal");

        mockMvc.perform(get("/api/communities/{communityId}/bars", community.getId())
                        .param("query", "Anaikal")
                        .with(authenticatedAs(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].communityId").value(community.getId()));
    }

    @Test
    void getBars_whenNotMember_returnsForbidden() throws Exception {
        User outsider = createUser("Intruso");

        getBars(community.getId(), outsider.getId())
                .andExpect(status().isForbidden());
    }

    // ----- Access to a bar and its resources -----

    @Test
    void getBar_whenMember_returnsOk() throws Exception {
        Bar bar = saveBar(community, "Anaikal");

        getBar(bar.getId(), user.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Anaikal"))
                .andExpect(jsonPath("$.communityId").value(community.getId()));
    }

    @Test
    void getBar_whenNotMember_returnsForbidden() throws Exception {
        Bar bar = saveBar(community, "Anaikal");
        User outsider = createUser("Intruso");

        getBar(bar.getId(), outsider.getId())
                .andExpect(status().isForbidden());
    }

    @Test
    void getBar_whenDoesNotExist_returnsNotFound() throws Exception {
        getBar(999_999L, user.getId())
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBar_whenNotMember_returnsForbidden() throws Exception {
        Bar bar = saveBar(community, "Anaikal");
        User outsider = createUser("Intruso");

        mockMvc.perform(put("/api/bars/{barId}", bar.getId())
                        .with(authenticatedAs(outsider.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(barBody("Otro nombre")))
                .andExpect(status().isForbidden());
    }

    // Bars are not admin-only: any member can add, edit or delete them
    @Test
    void deleteBar_whenAnyMember_returnsNoContent() throws Exception {
        Bar bar = saveBar(community, "Anaikal");
        User alberto = createUser("Alberto");
        addMember(community, alberto);

        deleteBar(bar.getId(), alberto.getId())
                .andExpect(status().isNoContent());

        assertThat(barRepository.existsById(bar.getId())).isFalse();
    }

    @Test
    void deleteBar_whenNotMember_returnsForbiddenAndKeepsBar() throws Exception {
        Bar bar = saveBar(community, "Anaikal");
        User outsider = createUser("Intruso");

        deleteBar(bar.getId(), outsider.getId())
                .andExpect(status().isForbidden());

        assertThat(barRepository.existsById(bar.getId())).isTrue();
    }

    @Test
    void getBarTapas_whenNotMember_returnsForbidden() throws Exception {
        Bar bar = saveBar(community, "Anaikal");
        User outsider = createUser("Intruso");

        mockMvc.perform(get("/api/bars/{barId}/tapas", bar.getId())
                        .with(authenticatedAs(outsider.getId())))
                .andExpect(status().isForbidden());
    }

    @Test
    void saveBarReview_whenNotMember_returnsForbidden() throws Exception {
        Bar bar = saveBar(community, "Anaikal");
        User outsider = createUser("Intruso");

        mockMvc.perform(put("/api/bars/{barId}/review", bar.getId())
                        .with(authenticatedAs(outsider.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "rating": 8 }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void saveTapaReview_whenNotMember_returnsForbidden() throws Exception {
        Bar bar = saveBar(community, "Anaikal");
        Tapa tapa = tapaRepository.save(new Tapa("alitas"));
        barTapaRepository.save(new BarTapa(bar, tapa));
        User outsider = createUser("Intruso");

        mockMvc.perform(put("/api/bars/{barId}/tapas/{tapaId}/review", bar.getId(), tapa.getId())
                        .with(authenticatedAs(outsider.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "rating": 4 }
                                """))
                .andExpect(status().isForbidden());
    }

    // ----- Request helpers -----

    private ResultActions createBar(Long communityId, Long userId, String name) throws Exception {
        return mockMvc.perform(post("/api/communities/{communityId}/bars", communityId)
                .with(authenticatedAs(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(barBody(name)));
    }

    private ResultActions getBars(Long communityId, Long userId) throws Exception {
        return mockMvc.perform(get("/api/communities/{communityId}/bars", communityId)
                .with(authenticatedAs(userId)));
    }

    private ResultActions getBar(Long barId, Long userId) throws Exception {
        return mockMvc.perform(get("/api/bars/{barId}", barId)
                .with(authenticatedAs(userId)));
    }

    private ResultActions deleteBar(Long barId, Long userId) throws Exception {
        return mockMvc.perform(delete("/api/bars/{barId}", barId)
                .with(authenticatedAs(userId)));
    }

    private String barBody(String name) {
        return """
                {
                  "name": "%s",
                  "city": "León",
                  "address": "Calle Jesús Rubio",
                  "zone": "Colegio San Claudio"
                }
                """.formatted(name);
    }

    // ----- Data helpers -----

    private User createUser(String username) {
        return userRepository.save(new User(username, "password"));
    }

    private Community createCommunityWithAdmin(String name, String inviteCode, User admin) {
        Community created = communityRepository.save(new Community(name, inviteCode));
        membershipRepository.save(new Membership(created, admin, CommunityRole.ADMIN));
        return created;
    }

    private void addMember(Community target, User member) {
        membershipRepository.save(new Membership(target, member, CommunityRole.MEMBER));
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