package com.idoceb00.laruta.backend.dto;

import com.idoceb00.laruta.backend.model.User;

public record UserResponse(Long id, String username) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername());
    }

}
