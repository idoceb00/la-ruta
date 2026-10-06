package com.idoceb00.laruta.backend.controller;

import com.idoceb00.laruta.backend.model.Bar;
import com.idoceb00.laruta.backend.model.User;
import com.idoceb00.laruta.backend.repository.BarRepository;
import com.idoceb00.laruta.backend.repository.UserRepository;
import com.idoceb00.laruta.backend.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BarRepository barRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private JwtEncoder jwtEncoder;

    private User user;
    private String token;

    // Tokens are generated directly with TokenService: the login flow has its own tests
    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("Roberto", "password"));
        token = tokenService.generateToken(user);
    }

    @Test
    void getBars_whenNoToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/bars"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getBars_whenValidToken_returnsOk() throws Exception {
        getBars(token)
                .andExpect(status().isOk());
    }

    @Test
    void getBars_whenTokenTampered_returnsUnauthorized() throws Exception {
        getBars(tamper(token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getBars_whenTokenExpired_returnsUnauthorized() throws Exception {
        getBars(expiredToken(user))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createBar_whenAuthenticated_setsCreatedByToUserId() throws Exception {
        String body = """
                {
                  "name": "Anaikal",
                  "city": "León",
                  "address": "Calle Jesús Rubio",
                  "zone": "Colegio San Claudio",
                  "notes": "El arroz picante está realmente bueno."
                }
                """;

        mockMvc.perform(post("/api/bars")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        List<Bar> bars = barRepository.findAll();
        assertThat(bars).singleElement()
                .extracting(Bar::getCreatedBy)
                .isEqualTo(user.getId());
    }

    private ResultActions getBars(String token) throws Exception {
        return mockMvc.perform(get("/api/bars")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    // Changes one character of the payload: the signature no longer matches
    private String tamper(String token) {
        int index = token.indexOf('.') + 5;
        char replacement = token.charAt(index) == 'A' ? 'B' : 'A';
        return token.substring(0, index) + replacement + token.substring(index + 1);
    }

    // Expired well beyond the decoder's default 60-second clock skew
    private String expiredToken(User user) {
        Instant issuedAt = Instant.now().minus(Duration.ofHours(2));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("la-ruta")
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(Duration.ofHours(1)))
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}