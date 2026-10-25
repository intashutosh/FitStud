package com.fitstud.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitstud.backend.dto.RegisterRequest;
import com.fitstud.backend.service.AuthService;
import com.fitstud.backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
// 1. Replace the old MockBean import with MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(com.fitstud.backend.config.SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 2. Change @MockBean to @MockitoBean
    @MockitoBean
    private AuthService authService;

    // 3. Change @MockBean to @MockitoBean
    @MockitoBean
    private JwtService jwtService;

    @Test
    void registrationRejectsInvalidEmail() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "not-an-email",
                "ashu123",
                "password123",
                "Ashutosh"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}