package com.example.maidmanager;

import com.example.maidmanager.auth.dto.AuthResponse;
import com.example.maidmanager.auth.dto.LoginRequest;
import com.example.maidmanager.auth.dto.RefreshTokenRequest;
import com.example.maidmanager.auth.dto.RegisterRequest;
import com.example.maidmanager.owner.dto.UpdateOwnerRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Verify successful owner registration returns tokens and profile")
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Pooja Sharma", "pooja@example.com", "Password@123", "+919876543210", "Asia/Kolkata", "INR"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.owner.email").value("pooja@example.com"))
                .andExpect(jsonPath("$.owner.name").value("Pooja Sharma"));
    }

    @Test
    @DisplayName("Verify registration with duplicate email returns 409 Conflict")
    void testRegisterDuplicateEmailFails() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Duplicate User", "dup@example.com", "Password@123", null, null, null
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_CONFLICT"));
    }

    @Test
    @DisplayName("Verify login with valid and invalid credentials")
    void testLoginFlow() throws Exception {
        RegisterRequest registerReq = new RegisterRequest(
                "Login User", "login@example.com", "Secret123", null, null, null
        );
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        // Invalid password
        LoginRequest invalidReq = new LoginRequest("login@example.com", "WrongPassword");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        // Valid password
        LoginRequest validReq = new LoginRequest("login@example.com", "Secret123");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("Verify refresh token rotation and revocation")
    void testRefreshTokenRotation() throws Exception {
        RegisterRequest registerReq = new RegisterRequest(
                "Refresh User", "refresh@example.com", "Secret123", null, null, null
        );
        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
                registerResult.getResponse().getContentAsString(), AuthResponse.class
        );
        String firstRefreshToken = authResponse.getRefreshToken();

        // Refresh tokens
        RefreshTokenRequest refreshReq = new RefreshTokenRequest(firstRefreshToken);
        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        AuthResponse newAuthResponse = objectMapper.readValue(
                refreshResult.getResponse().getContentAsString(), AuthResponse.class
        );
        String secondRefreshToken = newAuthResponse.getRefreshToken();
        assertThat(secondRefreshToken).isNotEqualTo(firstRefreshToken);

        // Attempting to reuse old refresh token must fail with 401
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Verify /api/v1/me authorization, retrieval and update")
    void testOwnerMeEndpoints() throws Exception {
        // Without token -> 401
        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        // Register user
        RegisterRequest registerReq = new RegisterRequest(
                "Me User", "me@example.com", "Secret123", "+919999999999", "Asia/Kolkata", "INR"
        );
        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse auth = objectMapper.readValue(
                registerResult.getResponse().getContentAsString(), AuthResponse.class
        );
        String token = auth.getAccessToken();

        // With valid token -> 200 OK
        mockMvc.perform(get("/api/v1/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Me User"))
                .andExpect(jsonPath("$.email").value("me@example.com"));

        // Update profile
        UpdateOwnerRequest updateReq = new UpdateOwnerRequest("Me User Updated", "+918888888888", "Asia/Kolkata", "INR");
        mockMvc.perform(patch("/api/v1/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Me User Updated"))
                .andExpect(jsonPath("$.phone").value("+918888888888"));
    }
}
