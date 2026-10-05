package com.idoceb00.laruta.backend.service;

import com.idoceb00.laruta.backend.config.JwtProperties;
import com.idoceb00.laruta.backend.dto.LoginRequest;
import com.idoceb00.laruta.backend.dto.LoginResponse;
import com.idoceb00.laruta.backend.exception.InvalidCredentialsException;
import com.idoceb00.laruta.backend.model.User;
import com.idoceb00.laruta.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final JwtProperties jwtProperties;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() ->new InvalidCredentialsException("User or password is wrong"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Wrong password");
        }

        String token = tokenService.generateToken(user);
        return new LoginResponse(token, "Bearer", jwtProperties.expiration().toSeconds());
    }

}
