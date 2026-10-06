package com.idoceb00.laruta.backend.controller;

import com.idoceb00.laruta.backend.model.User;
import com.idoceb00.laruta.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static com.idoceb00.laruta.backend.testutil.TestAuth.authenticatedAs;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    private User user;

    @BeforeEach
    void setUp() {
        user = createUser("Roberto");
    }

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

    private ResultActions getMe(Long userId) throws Exception {
        return mockMvc.perform(get("/api/users/me")
                .with(authenticatedAs(userId)));
    }

    private ResultActions deleteMe(Long userId) throws Exception {
        return mockMvc.perform(delete("/api/users/me")
                .with(authenticatedAs(userId)));
    }

    private User createUser(String username) {
        return userRepository.save(new User(username, "password"));
    }
}