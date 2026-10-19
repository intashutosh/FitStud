
package com.fitstud.backend.service;

import com.fitstud.backend.dto.RegisterRequest;
import com.fitstud.backend.entity.User;
import com.fitstud.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.fitstud.backend.dto.LoginRequest;
import com.fitstud.backend.entity.User;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String username = request.username().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Username is already taken");
        }

        User user = User.create(
                email,
                username,
                passwordEncoder.encode(request.password()),
                request.displayName().trim()
        );

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User login(LoginRequest request) {
        String email = request.email()
                .trim()
                .toLowerCase(java.util.Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Invalid email or password"
                        )
                );

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash())) {
            throw new BadCredentialsException(
                    "Invalid email or password"
            );
        }

        return user;
    }
}
