package com.example.maidmanager.attendance.service;

import com.example.maidmanager.attendance.dto.AttendanceSessionDto;
import com.example.maidmanager.attendance.dto.DashboardMaidEntryDto;
import com.example.maidmanager.attendance.dto.DashboardResponse;
import com.example.maidmanager.attendance.dto.EntryRequest;
import com.example.maidmanager.attendance.dto.ExitRequest;
import com.example.maidmanager.attendance.dto.ManualStatusRequest;
import com.example.maidmanager.attendance.dto.UpdateAttendanceSessionRequest;
import com.example.maidmanager.attendance.entity.AttendanceSession;
import com.example.maidmanager.attendance.repository.AttendanceSessionRepository;
import com.example.maidmanager.audit.entity.AuditEvent;
import com.example.maidmanager.audit.repository.AuditEventRepository;
import com.example.maidmanager.common.enums.AttendanceStatus;
import com.example.maidmanager.common.exception.BadRequestException;
import com.example.maidmanager.common.exception.ConflictException;
import com.example.maidmanager.common.exception.ResourceNotFoundException;
import com.example.maidmanager.employment.entity.EmploymentConfig;
import com.example.maidmanager.employment.repository.EmploymentConfigRepository;
import com.example.maidmanager.leave.repository.LeaveRecordRepository;
import com.example.maidmanager.maid.entity.Maid;
import com.example.maidmanager.maid.repository.MaidRepository;
import com.example.maidmanager.owner.entity.Owner;
import com.example.maidmanager.owner.repository.OwnerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceSessionRepository attendanceSessionRepository;
    private final MaidRepository maidRepository;
    private final OwnerRepository ownerRepository;
    private final EmploymentConfigRepository employmentConfigRepository;
    private final LeaveRecordRepository leaveRecordRepository;
    private final AuditEventRepository auditEventRepository;
    private final ObjectMapper objectMapper;

    public AttendanceService(
            AttendanceSessionRepository attendanceSessionRepository,
            MaidRepository maidRepository,
            OwnerRepository ownerRepository,
            EmploymentConfigRepository employmentConfigRepository,
            LeaveRecordRepository leaveRecordRepository,
            AuditEventRepository auditEventRepository,
            ObjectMapper objectMapper) {
        this.attendanceSessionRepository = attendanceSessionRepository;
        this.maidRepository = maidRepository;
        this.ownerRepository = ownerRepository;
        this.employmentConfigRepository = employmentConfigRepository;
        this.leaveRecordRepository = leaveRecordRepository;
        this.auditEventRepository = auditEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AttendanceSessionDto recordEntry(UUID ownerId, UUID maidId, EntryRequest request) {
        Owner owner = getOwnerOrThrow(ownerId);
        Maid maid = getMaidOrThrow(maidId, ownerId);

        ZoneId zoneId = resolveZoneId(owner.getTimezone());
        Instant now = Instant.now();
        LocalDate businessDate = (request != null && request.getBusinessDate() != null)
                ? request.getBusinessDate()
                : LocalDate.ofInstant(now, zoneId);

        // Check if an open session already exists
        Optional<AttendanceSession> activeSession = attendanceSessionRepository.findActiveOpenSession(
                maidId, ownerId, businessDate, AttendanceStatus.WORKING
        );
        if (activeSession.isPresent()) {
            throw new ConflictException("An active attendance session is already open for this worker on " + businessDate);
        }

        AttendanceSession session = new AttendanceSession(
                UUID.randomUUID(),
                maid,
                businessDate,
                now,
                null,
                AttendanceStatus.WORKING,
                request != null ? request.getNote() : null,
                owner,
                owner
        );

        session = attendanceSessionRepository.save(session);
        return AttendanceSessionDto.fromEntity(session);
    }

    @Transactional
    public AttendanceSessionDto recordExit(UUID ownerId, UUID maidId, UUID sessionId, ExitRequest request) {
        Owner owner = getOwnerOrThrow(ownerId);
        getMaidOrThrow(maidId, ownerId);

        AttendanceSession session = attendanceSessionRepository.findByIdAndOwnerId(sessionId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session not found"));

        if (!session.getMaid().getId().equals(maidId)) {
            throw new ResourceNotFoundException("Attendance session does not belong to specified worker");
        }

        if (session.getExitAt() != null || session.getStatus() == AttendanceStatus.COMPLETED) {
            throw new ConflictException("Session has already been closed with an exit.");
        }

        Instant now = Instant.now();
        if (session.getEntryAt() != null && now.isBefore(session.getEntryAt())) {
            throw new BadRequestException("Exit timestamp cannot be before entry timestamp.");
        }

        session.setExitAt(now);
        session.setStatus(AttendanceStatus.COMPLETED);
        session.setUpdatedBy(owner);
        if (request != null && request.getNote() != null && !request.getNote().isBlank()) {
            String note = session.getNote() == null ? request.getNote() : session.getNote() + " | " + request.getNote();
            session.setNote(note);
        }

        session = attendanceSessionRepository.save(session);
        return AttendanceSessionDto.fromEntity(session);
    }

    @Transactional
    public AttendanceSessionDto recordManualStatus(UUID ownerId, UUID maidId, ManualStatusRequest request) {
        Owner owner = getOwnerOrThrow(ownerId);
        Maid maid = getMaidOrThrow(maidId, ownerId);

        Optional<AttendanceSession> active = attendanceSessionRepository.findActiveOpenSession(
                maidId, ownerId, request.getBusinessDate(), AttendanceStatus.WORKING
        );
        if (active.isPresent()) {
            throw new ConflictException("Cannot set manual status while an attendance session is actively open.");
        }

        AttendanceSession session = new AttendanceSession(
                UUID.randomUUID(),
                maid,
                request.getBusinessDate(),
                null,
                null,
                request.getStatus(),
                request.getNote(),
                owner,
                owner
        );

        session = attendanceSessionRepository.save(session);
        recordAudit(owner, "ATTENDANCE_SESSION", session.getId(), "CREATE_MANUAL_STATUS", null, session);

        return AttendanceSessionDto.fromEntity(session);
    }

    @Transactional
    public AttendanceSessionDto updateSession(UUID ownerId, UUID maidId, UUID sessionId, UpdateAttendanceSessionRequest request) {
        Owner owner = getOwnerOrThrow(ownerId);
        getMaidOrThrow(maidId, ownerId);

        AttendanceSession session = attendanceSessionRepository.findByIdAndOwnerId(sessionId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session not found"));

        if (!session.getMaid().getId().equals(maidId)) {
            throw new ResourceNotFoundException("Attendance session does not belong to specified worker");
        }

        String beforeJson = serializeQuietly(session);

        Instant entryAt = request.getEntryAt() != null ? request.getEntryAt() : session.getEntryAt();
        Instant exitAt = request.getExitAt() != null ? request.getExitAt() : session.getExitAt();

        if (entryAt != null && exitAt != null && exitAt.isBefore(entryAt)) {
            throw new BadRequestException("Exit timestamp cannot be before entry timestamp.");
        }

        session.setEntryAt(entryAt);
        session.setExitAt(exitAt);
        if (request.getStatus() != null) {
            session.setStatus(request.getStatus());
        }
        if (request.getNote() != null) {
            session.setNote(request.getNote());
        }
        session.setUpdatedBy(owner);

        session = attendanceSessionRepository.save(session);
        recordAudit(owner, "ATTENDANCE_SESSION", session.getId(), "UPDATE", beforeJson, session);

        return AttendanceSessionDto.fromEntity(session);
    }

    @Transactional
    public void deleteSession(UUID ownerId, UUID maidId, UUID sessionId) {
        Owner owner = getOwnerOrThrow(ownerId);
        getMaidOrThrow(maidId, ownerId);

        AttendanceSession session = attendanceSessionRepository.findByIdAndOwnerId(sessionId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session not found"));

        if (!session.getMaid().getId().equals(maidId)) {
            throw new ResourceNotFoundException("Attendance session does not belong to specified worker");
        }

        String beforeJson = serializeQuietly(session);
        attendanceSessionRepository.delete(session);

        recordAudit(owner, "ATTENDANCE_SESSION", sessionId, "DELETE", beforeJson, null);
    }

    @Transactional(readOnly = true)
    public List<AttendanceSessionDto> getSessions(UUID ownerId, UUID maidId, LocalDate fromDate, LocalDate toDate) {
        getMaidOrThrow(maidId, ownerId);

        LocalDate from = fromDate != null ? fromDate : YearMonth.now().atDay(1);
        LocalDate to = toDate != null ? toDate : YearMonth.now().atEndOfMonth();

        List<AttendanceSession> sessions = attendanceSessionRepository.findAllByMaidIdAndDateRange(
                maidId, ownerId, from, to
        );

        return sessions.stream().map(AttendanceSessionDto::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(UUID ownerId, LocalDate targetDate) {
        Owner owner = getOwnerOrThrow(ownerId);
        ZoneId zoneId = resolveZoneId(owner.getTimezone());
        LocalDate date = targetDate != null ? targetDate : LocalDate.now(zoneId);

        List<Maid> activeMaids = maidRepository.findAllByOwnerIdAndIsActiveOrderByCreatedAtDesc(ownerId, true);
        List<DashboardMaidEntryDto> entries = new ArrayList<>();
        BigDecimal totalEstimatedPayable = BigDecimal.ZERO;

        for (Maid maid : activeMaids) {
            List<AttendanceSession> sessions = attendanceSessionRepository.findAllByMaidIdAndDate(
                    maid.getId(), ownerId, date
            );

            boolean onLeave = leaveRecordRepository.findByMaidIdAndDate(maid.getId(), ownerId, date).isPresent();

            String state = "NOT_STARTED";
            UUID activeSessionId = null;
            Instant entryAt = null;
            Instant exitAt = null;
            int completedCount = 0;
            long totalMinutes = 0;

            if (onLeave) {
                state = "ON_LEAVE";
            } else {
                for (AttendanceSession s : sessions) {
                    if (s.getStatus() == AttendanceStatus.WORKING && s.getExitAt() == null) {
                        state = "WORKING";
                        activeSessionId = s.getId();
                        entryAt = s.getEntryAt();
                    } else if (s.getStatus() == AttendanceStatus.COMPLETED) {
                        completedCount++;
                        if (s.getEntryAt() != null && s.getExitAt() != null) {
                            totalMinutes += Duration.between(s.getEntryAt(), s.getExitAt()).toMinutes();
                        }
                        if (!"WORKING".equals(state)) {
                            state = "COMPLETED";
                            entryAt = s.getEntryAt();
                            exitAt = s.getExitAt();
                        }
                    } else if (s.getStatus() == AttendanceStatus.ABSENT) {
                        state = "ABSENT";
                    } else if (s.getStatus() == AttendanceStatus.INCOMPLETE) {
                        state = "INCOMPLETE";
                    }
                }
            }

            entries.add(new DashboardMaidEntryDto(
                    maid.getId(),
                    maid.getName(),
                    state,
                    activeSessionId,
                    entryAt,
                    exitAt,
                    completedCount,
                    totalMinutes
            ));

            // Accumulate monthly salary estimate
            Optional<EmploymentConfig> config = employmentConfigRepository.findEffectiveConfigAtDate(maid.getId(), ownerId, date);
            if (config.isPresent()) {
                totalEstimatedPayable = totalEstimatedPayable.add(config.get().getSalaryAmount());
            }
        }

        DashboardResponse.CurrentMonthSnapshot monthSnapshot = new DashboardResponse.CurrentMonthSnapshot(
                totalEstimatedPayable, owner.getCurrencyCode()
        );

        return new DashboardResponse(date, activeMaids.size(), entries, monthSnapshot);
    }

    private Owner getOwnerOrThrow(UUID ownerId) {
        return ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));
    }

    private Maid getMaidOrThrow(UUID maidId, UUID ownerId) {
        return maidRepository.findByIdAndOwnerId(maidId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found"));
    }

    private ZoneId resolveZoneId(String timezone) {
        try {
            return (timezone != null && !timezone.isBlank()) ? ZoneId.of(timezone) : ZoneId.of("Asia/Kolkata");
        } catch (Exception e) {
            return ZoneId.of("Asia/Kolkata");
        }
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
