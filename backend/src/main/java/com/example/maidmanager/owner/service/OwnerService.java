package com.example.maidmanager.owner.service;

import com.example.maidmanager.common.exception.ResourceNotFoundException;
import com.example.maidmanager.owner.dto.OwnerDto;
import com.example.maidmanager.owner.dto.UpdateOwnerRequest;
import com.example.maidmanager.owner.entity.Owner;
import com.example.maidmanager.owner.repository.OwnerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OwnerService {

    private final OwnerRepository ownerRepository;

    public OwnerService(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @Transactional(readOnly = true)
    public OwnerDto getOwnerProfile(UUID ownerId) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner profile not found"));
        return OwnerDto.fromEntity(owner);
    }

    @Transactional
    public OwnerDto updateOwnerProfile(UUID ownerId, UpdateOwnerRequest request) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner profile not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            owner.setName(request.getName().trim());
        }
        if (request.getPhone() != null) {
            owner.setPhone(request.getPhone().trim());
        }
        if (request.getTimezone() != null && !request.getTimezone().isBlank()) {
            owner.setTimezone(request.getTimezone().trim());
        }
        if (request.getCurrencyCode() != null && !request.getCurrencyCode().isBlank()) {
            owner.setCurrencyCode(request.getCurrencyCode().trim().toUpperCase());
        }

        owner = ownerRepository.save(owner);
        return OwnerDto.fromEntity(owner);
    }
}
