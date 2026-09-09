package com.example.maidmanager.owner.controller;

import com.example.maidmanager.auth.security.OwnerPrincipal;
import com.example.maidmanager.owner.dto.OwnerDto;
import com.example.maidmanager.owner.dto.UpdateOwnerRequest;
import com.example.maidmanager.owner.service.OwnerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
public class OwnerController {

    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @GetMapping
    public ResponseEntity<OwnerDto> getCurrentOwner(@AuthenticationPrincipal OwnerPrincipal principal) {
        OwnerDto profile = ownerService.getOwnerProfile(principal.getId());
        return ResponseEntity.ok(profile);
    }

    @PatchMapping
    public ResponseEntity<OwnerDto> updateCurrentOwner(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @Valid @RequestBody UpdateOwnerRequest request) {
        OwnerDto updated = ownerService.updateOwnerProfile(principal.getId(), request);
        return ResponseEntity.ok(updated);
    }
}
