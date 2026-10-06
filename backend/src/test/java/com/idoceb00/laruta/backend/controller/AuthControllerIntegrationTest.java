package com.idoceb00.laruta.backend.controller;

import com.idoceb00.laruta.backend.model.User;
import com.idoceb00.laruta.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

    private static final String PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;

    @BeforeEach
    void setUp() {
        user = createUser("Roberto", PASSWORD);
    }

    @Test
    void register_whenValidRequest_returnsCreated() throws Exception {
        String body = """
                { "username": "Alberto", "password": "%s" }
                """.formatted(PASSWORD);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("Alberto"));
    }

    @Test
    void login_whenValidCredentials_returnsToken() throws Exception {
        login(user.getUsername(), PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber());
    }

    @Test
    void login_whenWrongPassword_returnsUnauthorized() throws Exception {
        login(user.getUsername(), "wrongPassword")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    // Same message as a wrong password: the API must not reveal which usernames exist
    @Test
    void login_whenUserDoesNotExist_returnsUnauthorized() throws Exception {
        login("Nobody", PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void login_whenPasswordMissing_returnsBadRequest() throws Exception {
        String body = """
                { "username": "%s" }
                """.formatted(user.getUsername());

        loginWithBody(body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    private ResultActions login(String username, String password) throws Exception {
        String body = """
                { "username": "%s", "password": "%s" }
                """.formatted(username, password);
        return loginWithBody(body);
    }

    private ResultActions loginWithBody(String body) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    // Unlike other tests, the password must be a real BCrypt hash so login can verify it
    private User createUser(String username, String rawPassword) {
        return userRepository.save(new User(username, passwordEncoder.encode(rawPassword)));
    }
}