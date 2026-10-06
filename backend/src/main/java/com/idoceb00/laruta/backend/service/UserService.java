package com.idoceb00.laruta.backend.service;

import com.idoceb00.laruta.backend.dto.CreateUserRequest;
import com.idoceb00.laruta.backend.dto.UserResponse;
import com.idoceb00.laruta.backend.exception.UserNotFoundException;
import com.idoceb00.laruta.backend.exception.UsernameAlreadyExistsException;
import com.idoceb00.laruta.backend.model.User;
import com.idoceb00.laruta.backend.repository.UserRepository;
import com.idoceb00.laruta.backend.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException("Username already exists: " + request.username());
        }

        User user = new User(request.username(), passwordEncoder.encode(request.password()));

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Long userId = currentUserProvider.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        return  UserResponse.from(user);
    }

    @Transactional
    public void deleteCurrentUser() {
        Long userId = currentUserProvider.getCurrentUserId();
        if (!userRepository.existsById(userId)){
            throw new UserNotFoundException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);

    }

}