package com.example.maidmanager.maid.controller;

import com.example.maidmanager.auth.security.OwnerPrincipal;
import com.example.maidmanager.employment.dto.CreateEmploymentConfigRequest;
import com.example.maidmanager.employment.dto.EmploymentConfigDto;
import com.example.maidmanager.maid.dto.CreateMaidRequest;
import com.example.maidmanager.maid.dto.MaidDetailDto;
import com.example.maidmanager.maid.dto.MaidSummaryDto;
import com.example.maidmanager.maid.dto.UpdateMaidRequest;
import com.example.maidmanager.maid.service.MaidService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/maids")
public class MaidController {

    private final MaidService maidService;

    public MaidController(MaidService maidService) {
        this.maidService = maidService;
    }

    @GetMapping
    public ResponseEntity<List<MaidSummaryDto>> listMaids(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search) {
        List<MaidSummaryDto> maids = maidService.listMaids(principal.getId(), active, search);
        return ResponseEntity.ok(maids);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<MaidDetailDto> createMaid(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @Valid @RequestBody CreateMaidRequest request) {
        MaidDetailDto created = maidService.createMaid(principal.getId(), request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{maidId}")
    public ResponseEntity<MaidDetailDto> getMaid(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable UUID maidId) {
        MaidDetailDto maid = maidService.getMaid(principal.getId(), maidId);
        return ResponseEntity.ok(maid);
    }

    @PatchMapping("/{maidId}")
    public ResponseEntity<MaidDetailDto> updateMaid(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable UUID maidId,
            @Valid @RequestBody UpdateMaidRequest request) {
        MaidDetailDto updated = maidService.updateMaid(principal.getId(), maidId, request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{maidId}/archive")
    public ResponseEntity<MaidDetailDto> archiveMaid(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable UUID maidId) {
        MaidDetailDto archived = maidService.archiveMaid(principal.getId(), maidId);
        return ResponseEntity.ok(archived);
    }

    @PostMapping("/{maidId}/employment-configs")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<EmploymentConfigDto> createEmploymentConfig(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable UUID maidId,
            @Valid @RequestBody CreateEmploymentConfigRequest request) {
        EmploymentConfigDto config = maidService.createEmploymentConfig(principal.getId(), maidId, request);
        return new ResponseEntity<>(config, HttpStatus.CREATED);
    }

    @GetMapping("/{maidId}/employment-configs")
    public ResponseEntity<List<EmploymentConfigDto>> getEmploymentConfigs(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable UUID maidId) {
        List<EmploymentConfigDto> configs = maidService.getEmploymentConfigs(principal.getId(), maidId);
        return ResponseEntity.ok(configs);
    }
}
