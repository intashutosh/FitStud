
package com.fitstud.backend.controller;

import com.fitstud.backend.dto.UserProfileResponse;
import com.fitstud.backend.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitstud.backend.dto.UpdateProfileRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fitstud.backend.dto.PublicProfileResponse;
import org.springframework.web.bind.annotation.PathVariable;

import com.fitstud.backend.dto.ChangePasswordRequest;
import org.springframework.web.bind.annotation.PatchMapping;

import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserProfileResponse getMyProfile(
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        return userService.getProfile(userId);
    }


    @PutMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public UserProfileResponse updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {

        UUID userId = UUID.fromString(authentication.getName());

        return userService.updateProfile(userId, request);
    }

    @GetMapping("/username-availability")
    public Map<String, Boolean> checkUsernameAvailability(
            @RequestParam String username) {

        boolean available = userService.isUsernameAvailable(username);

        return Map.of("available", available);
    }


    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {

        UUID userId = UUID.fromString(authentication.getName());

        userService.changePassword(userId, request);
    }

    @GetMapping("/{username}")
    public PublicProfileResponse getPublicProfile(
            @PathVariable String username) {
        return userService.getPublicProfile(username);
    }
}
