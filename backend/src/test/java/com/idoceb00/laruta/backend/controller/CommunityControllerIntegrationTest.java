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

import java.util.List;

import static com.idoceb00.laruta.backend.testutil.TestAuth.authenticatedAs;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommunityControllerIntegrationTest {

    private static final String INVITE_CODE = "ABCD2345";
    private static final String OTHER_INVITE_CODE = "WXYZ6789";
    // 8 characters, uppercase letters and digits without the ambiguous I, O, 0 and 1
    private static final String INVITE_CODE_PATTERN = "^[A-HJ-NP-Z2-9]{8}$";

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

    // ----- Create -----

    @Test
    void createCommunity_whenValidRequest_returnsCreatedWithCreatorAsAdmin() throws Exception {
        createCommunity(user.getId(), "Los del barrio")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Los del barrio"))
                .andExpect(jsonPath("$.inviteCode").value(matchesPattern(INVITE_CODE_PATTERN)))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.memberCount").value(1));
    }

    @Test
    void createCommunity_whenCreatedTwice_generatesDifferentInviteCodes() throws Exception {
        createCommunity(user.getId(), "Los del barrio");
        createCommunity(user.getId(), "Los del curro");

        List<String> codes = communityRepository.findAll().stream()
                .map(Community::getInviteCode)
                .toList();
        assertThat(codes).hasSize(2).doesNotHaveDuplicates();
    }

    @Test
    void createCommunity_whenNameBlank_returnsBadRequest() throws Exception {
        createCommunity(user.getId(), "   ")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void createCommunity_whenNotAuthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/communities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "Los del barrio" }
                                """))
                .andExpect(status().isUnauthorized());
    }

    // ----- List and detail -----

    @Test
    void getMyCommunities_whenMemberOfSome_returnsOnlyThose() throws Exception {
        Community mine = createCommunityWithAdmin("Los del barrio", INVITE_CODE, user);
        createCommunityWithAdmin("Los del curro", OTHER_INVITE_CODE, createUser("Alberto"));

        getMyCommunities(user.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(mine.getId()))
                .andExpect(jsonPath("$[0].name").value("Los del barrio"))
                .andExpect(jsonPath("$[0].role").value("ADMIN"));
    }

    @Test
    void getMyCommunities_whenMemberOfNone_returnsEmptyList() throws Exception {
        getMyCommunities(user.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getCommunity_whenMember_returnsDetailWithRoleAndMemberCount() throws Exception {
        Community community = createCommunityWithAdmin("Los del barrio", INVITE_CODE, createUser("Alberto"));
        addMember(community, user);

        getCommunity(community.getId(), user.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(community.getId()))
                .andExpect(jsonPath("$.name").value("Los del barrio"))
                .andExpect(jsonPath("$.inviteCode").value(INVITE_CODE))
                .andExpect(jsonPath("$.role").value("MEMBER"))
                .andExpect(jsonPath("$.memberCount").value(2));
    }

    @Test
    void getCommunity_whenNotMember_returnsForbidden() throws Exception {
        Community community = createCommunityWithAdmin("Los del barrio", INVITE_CODE, createUser("Alberto"));

        getCommunity(community.getId(), user.getId())
                .andExpect(status().isForbidden());
    }

    @Test
    void getCommunity_whenDoesNotExist_returnsNotFound() throws Exception {
        getCommunity(999_999L, user.getId())
                .andExpect(status().isNotFound());
    }

    // ----- Join -----

    @Test
    void join_whenValidCode_addsUserAsMember() throws Exception {
        Community community = createCommunityWithAdmin("Los del barrio", INVITE_CODE, createUser("Alberto"));

        join(user.getId(), INVITE_CODE)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(community.getId()))
                .andExpect(jsonPath("$.role").value("MEMBER"))
                .andExpect(jsonPath("$.memberCount").value(2));

        assertThat(roleOf(community, user)).isEqualTo(CommunityRole.MEMBER);
    }

    // Codes are typed by hand on a phone: case and surrounding spaces are ignored
    @Test
    void join_whenCodeHasLowercaseOrSpaces_isAccepted() throws Exception {
        Community community = createCommunityWithAdmin("Los del barrio", INVITE_CODE, createUser("Alberto"));

        join(user.getId(), "  abcd2345  ")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(community.getId()));
    }

    @Test
    void join_whenCodeDoesNotExist_returnsNotFound() throws Exception {
        join(user.getId(), INVITE_CODE)
                .andExpect(status().isNotFound());
    }

    @Test
    void join_whenAlreadyMember_returnsConflict() throws Exception {
        createCommunityWithAdmin("Los del barrio", INVITE_CODE, user);

        join(user.getId(), INVITE_CODE)
                .andExpect(status().isConflict());
    }

    @Test
    void join_whenCodeBlank_returnsBadRequest() throws Exception {
        join(user.getId(), "   ")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void join_whenNotAuthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/communities/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "inviteCode": "%s" }
                                """.formatted(INVITE_CODE)))
                .andExpect(status().isUnauthorized());
    }

    // ----- Leave -----

    @Test
    void leave_whenMemberLeaves_removesMembershipAndKeepsCommunityAndBars() throws Exception {
        Community community = createCommunityWithAdmin("Los del barrio", INVITE_CODE, createUser("Alberto"));
        addMember(community, user);
        Bar bar = saveBar(community, "Anaikal");

        leave(community.getId(), user.getId())
                .andExpect(status().isNoContent());

        assertThat(membershipRepository.findByCommunityIdAndUserId(community.getId(), user.getId())).isEmpty();
        assertThat(communityRepository.existsById(community.getId())).isTrue();
        assertThat(barRepository.existsById(bar.getId())).isTrue();
    }

    // The member who joined first (lowest membership id) becomes the new admin
    @Test
    void leave_whenAdminLeaves_promotesOldestMember() throws Exception {
        Community community = createCommunityWithAdmin("Los del barrio", INVITE_CODE, user);
        User alberto = createUser("Alberto");
        addMember(community, alberto);
        User jorge = createUser("Jorge");
        addMember(community, jorge);

        leave(community.getId(), user.getId())
                .andExpect(status().isNoContent());

        assertThat(roleOf(community, alberto)).isEqualTo(CommunityRole.ADMIN);
        assertThat(roleOf(community, jorge)).isEqualTo(CommunityRole.MEMBER);
    }

    @Test
    void leave_whenLastMember_deletesCommunityAndItsBars() throws Exception {
        Community community = createCommunityWithAdmin("Los del barrio", INVITE_CODE, user);
        Bar bar = saveBar(community, "Anaikal");

        leave(community.getId(), user.getId())
                .andExpect(status().isNoContent());

        assertThat(communityRepository.existsById(community.getId())).isFalse();
        assertThat(barRepository.existsById(bar.getId())).isFalse();
    }

    @Test
    void leave_whenUserHadBarReviews_removesThemFromStats() throws Exception {
        User alberto = createUser("Alberto");
        Community community = createCommunityWithAdmin("Los del barrio", INVITE_CODE, alberto);
        addMember(community, user);
        Bar bar = saveBar(community, "Anaikal");
        rateBar(bar.getId(), user.getId(), 2);
        rateBar(bar.getId(), alberto.getId(), 8);

        leave(community.getId(), user.getId());

        rateBar(bar.getId(), alberto.getId(), 8)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(8.0))
                .andExpect(jsonPath("$.ratingCount").value(1));
    }

    @Test
    void leave_whenUserHadTapaReviews_removesThemFromStats() throws Exception {
        User alberto = createUser("Alberto");
        Community community = createCommunityWithAdmin("Los del barrio", INVITE_CODE, alberto);
        addMember(community, user);
        Bar bar = saveBar(community, "Anaikal");
        Tapa tapa = saveTapaInBar(bar, "alitas");
        rateTapa(bar.getId(), tapa.getId(), user.getId(), 1);
        rateTapa(bar.getId(), tapa.getId(), alberto.getId(), 5);

        leave(community.getId(), user.getId());

        rateTapa(bar.getId(), tapa.getId(), alberto.getId(), 5)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(5.0))
                .andExpect(jsonPath("$.ratingCount").value(1));
    }

    @Test
    void leave_whenUserHasReviewsInAnotherCommunity_keepsThem() throws Exception {
        User alberto = createUser("Alberto");
        Community leaving = createCommunityWithAdmin("Los del barrio", INVITE_CODE, alberto);
        Community staying = createCommunityWithAdmin("Los del curro", OTHER_INVITE_CODE, alberto);
        addMember(leaving, user);
        addMember(staying, user);
        Bar bar = saveBar(staying, "La Competencia");
        rateBar(bar.getId(), user.getId(), 2);

        leave(leaving.getId(), user.getId());

        rateBar(bar.getId(), alberto.getId(), 8)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(5.0))
                .andExpect(jsonPath("$.ratingCount").value(2));
    }

    @Test
    void leave_whenNotMember_returnsForbidden() throws Exception {
        Community community = createCommunityWithAdmin("Los del barrio", INVITE_CODE, createUser("Alberto"));

        leave(community.getId(), user.getId())
                .andExpect(status().isForbidden());
    }

    @Test
    void leave_whenNotAuthenticated_returnsUnauthorized() throws Exception {
        Community community = createCommunityWithAdmin("Los del barrio", INVITE_CODE, user);

        mockMvc.perform(delete("/api/communities/{communityId}/members/me", community.getId()))
                .andExpect(status().isUnauthorized());
    }

    // ----- Request helpers -----

    private ResultActions createCommunity(Long userId, String name) throws Exception {
        return mockMvc.perform(post("/api/communities")
                .with(authenticatedAs(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "name": "%s" }
                        """.formatted(name)));
    }

    private ResultActions getMyCommunities(Long userId) throws Exception {
        return mockMvc.perform(get("/api/communities")
                .with(authenticatedAs(userId)));
    }

    private ResultActions getCommunity(Long communityId, Long userId) throws Exception {
        return mockMvc.perform(get("/api/communities/{communityId}", communityId)
                .with(authenticatedAs(userId)));
    }

    private ResultActions join(Long userId, String inviteCode) throws Exception {
        return mockMvc.perform(post("/api/communities/join")
                .with(authenticatedAs(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "inviteCode": "%s" }
                        """.formatted(inviteCode)));
    }

    private ResultActions leave(Long communityId, Long userId) throws Exception {
        return mockMvc.perform(delete("/api/communities/{communityId}/members/me", communityId)
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

    private Tapa saveTapaInBar(Bar bar, String name) {
        Tapa tapa = tapaRepository.save(new Tapa(name));
        barTapaRepository.save(new BarTapa(bar, tapa));
        return tapa;
    }

    private CommunityRole roleOf(Community community, User member) {
        return membershipRepository.findByCommunityIdAndUserId(community.getId(), member.getId())
                .map(Membership::getRole)
                .orElseThrow();
    }
}