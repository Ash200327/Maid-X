package com.example.maidmanager.holiday.service;

import com.example.maidmanager.audit.entity.AuditEvent;
import com.example.maidmanager.audit.repository.AuditEventRepository;
import com.example.maidmanager.common.exception.ConflictException;
import com.example.maidmanager.common.exception.ResourceNotFoundException;
import com.example.maidmanager.holiday.dto.CreateHolidayRequest;
import com.example.maidmanager.holiday.dto.HolidayDto;
import com.example.maidmanager.holiday.dto.UpdateHolidayRequest;
import com.example.maidmanager.holiday.entity.Holiday;
import com.example.maidmanager.holiday.repository.HolidayRepository;
import com.example.maidmanager.owner.entity.Owner;
import com.example.maidmanager.owner.repository.OwnerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HolidayService {

    private final HolidayRepository holidayRepository;
    private final OwnerRepository ownerRepository;
    private final AuditEventRepository auditEventRepository;
    private final ObjectMapper objectMapper;

    public HolidayService(
            HolidayRepository holidayRepository,
            OwnerRepository ownerRepository,
            AuditEventRepository auditEventRepository,
            ObjectMapper objectMapper) {
        this.holidayRepository = holidayRepository;
        this.ownerRepository = ownerRepository;
        this.auditEventRepository = auditEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public HolidayDto createHoliday(UUID ownerId, CreateHolidayRequest request) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        if (holidayRepository.existsByOwnerIdAndHolidayDate(ownerId, request.getHolidayDate())) {
            throw new ConflictException("A holiday already exists on " + request.getHolidayDate());
        }

        Holiday holiday = new Holiday(
                UUID.randomUUID(),
                owner,
                request.getHolidayDate(),
                request.getName().trim()
        );

        holiday = holidayRepository.save(holiday);
        recordAudit(owner, "HOLIDAY", holiday.getId(), "CREATE", null, holiday);

        return HolidayDto.fromEntity(holiday);
    }

    @Transactional(readOnly = true)
    public List<HolidayDto> listHolidays(UUID ownerId, Integer year, Integer month, LocalDate from, LocalDate to) {
        LocalDate startDate;
        LocalDate endDate;

        if (from != null && to != null) {
            startDate = from;
            endDate = to;
        } else if (year != null && month != null) {
            YearMonth ym = YearMonth.of(year, month);
            startDate = ym.atDay(1);
            endDate = ym.atEndOfMonth();
        } else if (year != null) {
            startDate = LocalDate.of(year, 1, 1);
            endDate = LocalDate.of(year, 12, 31);
        } else {
            LocalDate now = LocalDate.now();
            startDate = LocalDate.of(now.getYear(), 1, 1);
            endDate = LocalDate.of(now.getYear(), 12, 31);
        }

        List<Holiday> holidays = holidayRepository.findAllByOwnerIdAndDateRange(ownerId, startDate, endDate);
        return holidays.stream().map(HolidayDto::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public HolidayDto updateHoliday(UUID ownerId, UUID holidayId, UpdateHolidayRequest request) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        Holiday holiday = holidayRepository.findByIdAndOwnerId(holidayId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found"));

        String beforeJson = serializeQuietly(holiday);

        if (request.getHolidayDate() != null && !request.getHolidayDate().equals(holiday.getHolidayDate())) {
            Optional<Holiday> existing = holidayRepository.findByOwnerIdAndDate(ownerId, request.getHolidayDate());
            if (existing.isPresent()) {
                throw new ConflictException("A holiday already exists on " + request.getHolidayDate());
            }
            holiday.setHolidayDate(request.getHolidayDate());
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            holiday.setName(request.getName().trim());
        }

        holiday = holidayRepository.save(holiday);
        recordAudit(owner, "HOLIDAY", holiday.getId(), "UPDATE", beforeJson, holiday);

        return HolidayDto.fromEntity(holiday);
    }

    @Transactional
    public void deleteHoliday(UUID ownerId, UUID holidayId) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        Holiday holiday = holidayRepository.findByIdAndOwnerId(holidayId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found"));

        String beforeJson = serializeQuietly(holiday);
        holidayRepository.delete(holiday);
        recordAudit(owner, "HOLIDAY", holidayId, "DELETE", beforeJson, null);
    }

    private void recordAudit(Owner owner, String entityType, UUID entityId, String action, String beforeJson, Object afterObject) {
        String afterJson = afterObject != null ? serializeQuietly(afterObject) : null;
        AuditEvent audit = new AuditEvent(
                UUID.randomUUID(), owner, owner, entityType, entityId, action, beforeJson, afterJson
        );
        auditEventRepository.save(audit);
    }

    private String serializeQuietly(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
