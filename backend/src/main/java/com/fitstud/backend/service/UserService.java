
package com.fitstud.backend.service;

import com.fitstud.backend.dto.PublicProfileResponse;
import com.fitstud.backend.dto.UserProfileResponse;
import com.fitstud.backend.entity.User;
import com.fitstud.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fitstud.backend.dto.UpdateProfileRequest;
import com.fitstud.backend.dto.ChangePasswordRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getDisplayName(),
                user.getBio(),
                user.getProfilePhotoUrl(),
                user.getCreatedAt()
        );
    }

    @Transactional
    public UserProfileResponse updateProfile(
            UUID userId,
            UpdateProfileRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        user.setDisplayName(request.displayName().trim());
        user.setBio(request.bio() == null
                ? null : request.bio().trim());
        user.setProfilePhotoUrl(request.profilePhotoUrl() == null
                ? null : request.profilePhotoUrl().trim());

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getDisplayName(),
                user.getBio(),
                user.getProfilePhotoUrl(),
                user.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        if (username == null
                || !username.matches("^[A-Za-z0-9_]{3,30}$")) {
            throw new IllegalArgumentException(
                    "Username must be 3-30 characters using letters, numbers, or underscores"
            );
        }

        String normalizedUsername = username.toLowerCase(
                java.util.Locale.ROOT
        );

        return !userRepository.existsByUsernameIgnoreCase(
                normalizedUsername
        );
    }


    @Transactional
    public void changePassword(
            UUID userId,
            ChangePasswordRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(
                request.currentPassword(),
                user.getPasswordHash())) {
            throw new BadCredentialsException(
                    "Current password is incorrect"
            );
        }

        if (passwordEncoder.matches(
                request.newPassword(),
                user.getPasswordHash())) {
            throw new IllegalArgumentException(
                    "New password must differ from current password"
            );
        }

        user.setPasswordHash(
                passwordEncoder.encode(request.newPassword())
        );
        user.incrementTokenVersion();
    }


    @Transactional(readOnly = true)
    public PublicProfileResponse getPublicProfile(String username) {
        String normalizedUsername = username.trim()
                .toLowerCase(java.util.Locale.ROOT);

        User user = userRepository.findByUsernameIgnoreCase(
                        normalizedUsername)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        return new PublicProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getBio(),
                user.getProfilePhotoUrl(),
                user.getCreatedAt()
        );
    }




}
