package com.example.maidmanager.auth.service;

import com.example.maidmanager.auth.dto.AuthResponse;
import com.example.maidmanager.auth.dto.LoginRequest;
import com.example.maidmanager.auth.dto.RefreshTokenRequest;
import com.example.maidmanager.auth.dto.RegisterRequest;
import com.example.maidmanager.auth.entity.AuthRefreshToken;
import com.example.maidmanager.auth.repository.AuthRefreshTokenRepository;
import com.example.maidmanager.auth.security.JwtService;
import com.example.maidmanager.common.exception.ConflictException;
import com.example.maidmanager.common.exception.UnauthorizedException;
import com.example.maidmanager.owner.dto.OwnerDto;
import com.example.maidmanager.owner.entity.Owner;
import com.example.maidmanager.owner.repository.OwnerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final OwnerRepository ownerRepository;
    private final AuthRefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            OwnerRepository ownerRepository,
            AuthRefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.ownerRepository = ownerRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (ownerRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("An account with this email already exists.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        String timezone = request.getTimezone() != null ? request.getTimezone() : "Asia/Kolkata";
        String currencyCode = request.getCurrencyCode() != null ? request.getCurrencyCode() : "INR";

        Owner owner = new Owner(
                UUID.randomUUID(),
                request.getName().trim(),
                request.getPhone(),
                normalizedEmail,
                encodedPassword,
                timezone,
                currencyCode
        );
        owner = ownerRepository.save(owner);

        return issueTokens(owner);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        Owner owner = ownerRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword(), owner.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        return issueTokens(owner);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String rawToken = request.getRefreshToken();
        String tokenHash = jwtService.hashToken(rawToken);

        AuthRefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token."));

        if (storedToken.getRevokedAt() != null) {
            throw new UnauthorizedException("Refresh token has been revoked.");
        }

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token has expired. Please log in again.");
        }

        // Token rotation: revoke old token
        storedToken.setRevokedAt(Instant.now());
        refreshTokenRepository.save(storedToken);

        // Issue new tokens for the owner
        return issueTokens(storedToken.getOwner());
    }

    @Transactional
    public void logout(UUID ownerId, String rawRefreshToken) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            String tokenHash = jwtService.hashToken(rawRefreshToken.trim());
            refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
                token.setRevokedAt(Instant.now());
                refreshTokenRepository.save(token);
            });
        } else if (ownerId != null) {
            refreshTokenRepository.revokeAllByOwnerId(ownerId, Instant.now());
        }
    }

    private AuthResponse issueTokens(Owner owner) {
        String accessToken = jwtService.generateAccessToken(owner.getId(), owner.getEmail());
        String rawRefreshToken = jwtService.generateRefreshTokenString();
        String refreshTokenHash = jwtService.hashToken(rawRefreshToken);

        Instant refreshExpiresAt = Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds());
        AuthRefreshToken refreshTokenEntity = new AuthRefreshToken(
                UUID.randomUUID(),
                owner,
                refreshTokenHash,
                refreshExpiresAt
        );
        refreshTokenRepository.save(refreshTokenEntity);

        return new AuthResponse(
                accessToken,
                rawRefreshToken,
                jwtService.getAccessTtlSeconds(),
                OwnerDto.fromEntity(owner)
        );
    }
}
