package com.example.maidmanager.auth.controller;

import com.example.maidmanager.auth.dto.AuthResponse;
import com.example.maidmanager.auth.dto.LoginRequest;
import com.example.maidmanager.auth.dto.RefreshTokenRequest;
import com.example.maidmanager.auth.dto.RegisterRequest;
import com.example.maidmanager.auth.security.OwnerPrincipal;
import com.example.maidmanager.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @RequestBody(required = false) RefreshTokenRequest request) {
        UUID ownerId = principal != null ? principal.getId() : null;
        String refreshToken = request != null ? request.getRefreshToken() : null;
        authService.logout(ownerId, refreshToken);
        return ResponseEntity.noContent().build();
    }
}
