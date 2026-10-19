
package com.fitstud.backend.service;

import com.fitstud.backend.dto.RegisterRequest;
import com.fitstud.backend.entity.User;
import com.fitstud.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private AuthService authService;

    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, passwordEncoder);
    }

    @Test
    void registerShouldHashPasswordAndSaveUser() {
        RegisterRequest request = new RegisterRequest(
                "ASHU@example.com",
                "Ashu_123",
                "StrongPass123",
                "Ashutosh"
        );

        when(userRepository.existsByEmail("ashu@example.com"))
                .thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase("ashu_123"))
                .thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    return user;
                });

        User savedUser = authService.register(request);

        assertEquals("ashu@example.com", savedUser.getEmail());
        assertEquals("ashu_123", savedUser.getUsername());
        assertTrue(passwordEncoder.matches(
                "StrongPass123",
                savedUser.getPasswordHash()
        ));
        assertNotEquals("StrongPass123", savedUser.getPasswordHash());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerShouldRejectDuplicateEmail() {
        RegisterRequest request = new RegisterRequest(
                "ashu@example.com",
                "ashu_123",
                "StrongPass123",
                "Ashutosh"
        );

        when(userRepository.existsByEmail("ashu@example.com"))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerShouldRejectDuplicateUsername() {
        RegisterRequest request = new RegisterRequest(
                "new@example.com",
                "Ashu_123",
                "StrongPass123",
                "Ashutosh"
        );

        when(userRepository.existsByEmail("new@example.com"))
                .thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase("ashu_123"))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );

        verify(userRepository, never()).save(any(User.class));
    }
}
