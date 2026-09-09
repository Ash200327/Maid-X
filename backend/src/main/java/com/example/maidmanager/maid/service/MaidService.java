package com.example.maidmanager.maid.service;

import com.example.maidmanager.common.exception.BadRequestException;
import com.example.maidmanager.common.exception.ResourceNotFoundException;
import com.example.maidmanager.employment.dto.CreateEmploymentConfigRequest;
import com.example.maidmanager.employment.dto.EmploymentConfigDto;
import com.example.maidmanager.employment.entity.EmploymentConfig;
import com.example.maidmanager.employment.entity.MaidWorkingDay;
import com.example.maidmanager.employment.repository.EmploymentConfigRepository;
import com.example.maidmanager.maid.dto.CreateMaidRequest;
import com.example.maidmanager.maid.dto.MaidDetailDto;
import com.example.maidmanager.maid.dto.MaidSummaryDto;
import com.example.maidmanager.maid.dto.UpdateMaidRequest;
import com.example.maidmanager.maid.entity.Maid;
import com.example.maidmanager.maid.repository.MaidRepository;
import com.example.maidmanager.owner.entity.Owner;
import com.example.maidmanager.owner.repository.OwnerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MaidService {

    private final MaidRepository maidRepository;
    private final OwnerRepository ownerRepository;
    private final EmploymentConfigRepository employmentConfigRepository;

    public MaidService(
            MaidRepository maidRepository,
            OwnerRepository ownerRepository,
            EmploymentConfigRepository employmentConfigRepository) {
        this.maidRepository = maidRepository;
        this.ownerRepository = ownerRepository;
        this.employmentConfigRepository = employmentConfigRepository;
    }

    @Transactional(readOnly = true)
    public List<MaidSummaryDto> listMaids(UUID ownerId, Boolean active, String search) {
        List<Maid> maids;
        if (search != null && !search.isBlank()) {
            maids = maidRepository.searchByName(ownerId, search.trim());
        } else if (active != null) {
            maids = maidRepository.findAllByOwnerIdAndIsActiveOrderByCreatedAtDesc(ownerId, active);
        } else {
            maids = maidRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId);
        }

        LocalDate today = LocalDate.now();
        return maids.stream().map(maid -> {
            EmploymentConfig currentConfig = employmentConfigRepository
                    .findEffectiveConfigAtDate(maid.getId(), ownerId, today)
                    .orElse(null);
            return MaidSummaryDto.fromEntity(maid, currentConfig);
        }).collect(Collectors.toList());
    }

    @Transactional
    public MaidDetailDto createMaid(UUID ownerId, CreateMaidRequest request) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        if (request.getLeavingDate() != null && request.getLeavingDate().isBefore(request.getJoiningDate())) {
            throw new BadRequestException("Leaving date cannot precede joining date.");
        }

        CreateEmploymentConfigRequest configReq = request.getEmploymentConfig();
        if (configReq.getShortfallThresholdMinutes() > configReq.getExpectedMinutesPerDay()) {
            throw new BadRequestException("Shortfall threshold minutes cannot exceed expected minutes per day.");
        }

        Maid maid = new Maid(
                UUID.randomUUID(),
                owner,
                request.getName().trim(),
                request.getPhone() != null ? request.getPhone().trim() : null,
                request.getJoiningDate(),
                request.getLeavingDate(),
                request.getNotes(),
                true
        );
        maid = maidRepository.save(maid);

        LocalDate effectiveFrom = configReq.getEffectiveFrom() != null
                ? configReq.getEffectiveFrom()
                : request.getJoiningDate();

        EmploymentConfig config = new EmploymentConfig(
                UUID.randomUUID(),
                maid,
                effectiveFrom,
                configReq.getEffectiveTo(),
                configReq.getSalaryMode(),
                configReq.getSalaryAmount(),
                configReq.getExpectedMinutesPerDay(),
                configReq.getShortfallThresholdMinutes(),
                configReq.isOvertimeEnabled(),
                configReq.getOvertimeMultiplier()
        );

        Set<MaidWorkingDay> workingDays = new HashSet<>();
        for (Short weekday : configReq.getWorkingDays()) {
            workingDays.add(new MaidWorkingDay(UUID.randomUUID(), config, weekday));
        }
        config.setWorkingDays(workingDays);

        config = employmentConfigRepository.save(config);

        return MaidDetailDto.fromEntity(maid, config);
    }

    @Transactional(readOnly = true)
    public MaidDetailDto getMaid(UUID ownerId, UUID maidId) {
        Maid maid = maidRepository.findByIdAndOwnerId(maidId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found"));

        EmploymentConfig currentConfig = employmentConfigRepository
                .findEffectiveConfigAtDate(maidId, ownerId, LocalDate.now())
                .orElse(null);

        return MaidDetailDto.fromEntity(maid, currentConfig);
    }

    @Transactional
    public MaidDetailDto updateMaid(UUID ownerId, UUID maidId, UpdateMaidRequest request) {
        Maid maid = maidRepository.findByIdAndOwnerId(maidId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found"));

        LocalDate newJoining = request.getJoiningDate() != null ? request.getJoiningDate() : maid.getJoiningDate();
        LocalDate newLeaving = request.getLeavingDate() != null ? request.getLeavingDate() : maid.getLeavingDate();

        if (newLeaving != null && newLeaving.isBefore(newJoining)) {
            throw new BadRequestException("Leaving date cannot precede joining date.");
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            maid.setName(request.getName().trim());
        }
        if (request.getPhone() != null) {
            maid.setPhone(request.getPhone().trim());
        }
        if (request.getJoiningDate() != null) {
            maid.setJoiningDate(request.getJoiningDate());
        }
        if (request.getLeavingDate() != null) {
            maid.setLeavingDate(request.getLeavingDate());
        }
        if (request.getNotes() != null) {
            maid.setNotes(request.getNotes());
        }
        if (request.getIsActive() != null) {
            maid.setActive(request.getIsActive());
        }

        maid = maidRepository.save(maid);

        EmploymentConfig currentConfig = employmentConfigRepository
                .findEffectiveConfigAtDate(maidId, ownerId, LocalDate.now())
                .orElse(null);

        return MaidDetailDto.fromEntity(maid, currentConfig);
    }

    @Transactional
    public MaidDetailDto archiveMaid(UUID ownerId, UUID maidId) {
        Maid maid = maidRepository.findByIdAndOwnerId(maidId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found"));

        maid.setActive(false);
        if (maid.getLeavingDate() == null) {
            maid.setLeavingDate(LocalDate.now());
        }
        maid = maidRepository.save(maid);

        EmploymentConfig currentConfig = employmentConfigRepository
                .findEffectiveConfigAtDate(maidId, ownerId, LocalDate.now())
                .orElse(null);

        return MaidDetailDto.fromEntity(maid, currentConfig);
    }

    @Transactional
    public EmploymentConfigDto createEmploymentConfig(UUID ownerId, UUID maidId, CreateEmploymentConfigRequest request) {
        Maid maid = maidRepository.findByIdAndOwnerId(maidId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found"));

        if (request.getEffectiveFrom() == null) {
            throw new BadRequestException("effectiveFrom date is required.");
        }

        if (request.getShortfallThresholdMinutes() > request.getExpectedMinutesPerDay()) {
            throw new BadRequestException("Shortfall threshold minutes cannot exceed expected minutes per day.");
        }

        // Close any prior open-ended configuration
        List<EmploymentConfig> existingConfigs = employmentConfigRepository.findAllByMaidIdAndOwnerId(maidId, ownerId);
        for (EmploymentConfig existing : existingConfigs) {
            if (existing.getEffectiveTo() == null && existing.getEffectiveFrom().isBefore(request.getEffectiveFrom())) {
                existing.setEffectiveTo(request.getEffectiveFrom().minusDays(1));
                employmentConfigRepository.save(existing);
            }
        }

        EmploymentConfig newConfig = new EmploymentConfig(
                UUID.randomUUID(),
                maid,
                request.getEffectiveFrom(),
                request.getEffectiveTo(),
                request.getSalaryMode(),
                request.getSalaryAmount(),
                request.getExpectedMinutesPerDay(),
                request.getShortfallThresholdMinutes(),
                request.isOvertimeEnabled(),
                request.getOvertimeMultiplier()
        );

        Set<MaidWorkingDay> workingDays = new HashSet<>();
        for (Short weekday : request.getWorkingDays()) {
            workingDays.add(new MaidWorkingDay(UUID.randomUUID(), newConfig, weekday));
        }
        newConfig.setWorkingDays(workingDays);

        newConfig = employmentConfigRepository.save(newConfig);
        return EmploymentConfigDto.fromEntity(newConfig);
    }

    @Transactional(readOnly = true)
    public List<EmploymentConfigDto> getEmploymentConfigs(UUID ownerId, UUID maidId) {
        if (!maidRepository.existsById(maidId)) {
            throw new ResourceNotFoundException("Worker not found");
        }
        // Validate ownership
        maidRepository.findByIdAndOwnerId(maidId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found"));

        List<EmploymentConfig> configs = employmentConfigRepository.findAllByMaidIdAndOwnerId(maidId, ownerId);
        return configs.stream().map(EmploymentConfigDto::fromEntity).collect(Collectors.toList());
    }
}
