
package com.fitstud.backend.service;

import com.fitstud.backend.dto.UserProfileResponse;
import com.fitstud.backend.entity.User;
import com.fitstud.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fitstud.backend.dto.UpdateProfileRequest;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

}
