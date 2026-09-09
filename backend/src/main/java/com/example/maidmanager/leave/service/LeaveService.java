package com.example.maidmanager.leave.service;

import com.example.maidmanager.audit.entity.AuditEvent;
import com.example.maidmanager.audit.repository.AuditEventRepository;
import com.example.maidmanager.common.exception.ConflictException;
import com.example.maidmanager.common.exception.ResourceNotFoundException;
import com.example.maidmanager.leave.dto.CreateLeaveRequest;
import com.example.maidmanager.leave.dto.LeaveRecordDto;
import com.example.maidmanager.leave.dto.UpdateLeaveRequest;
import com.example.maidmanager.leave.entity.LeaveRecord;
import com.example.maidmanager.leave.repository.LeaveRecordRepository;
import com.example.maidmanager.maid.entity.Maid;
import com.example.maidmanager.maid.repository.MaidRepository;
import com.example.maidmanager.owner.entity.Owner;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LeaveService {

    private final LeaveRecordRepository leaveRecordRepository;
    private final MaidRepository maidRepository;
    private final AuditEventRepository auditEventRepository;
    private final ObjectMapper objectMapper;

    public LeaveService(
            LeaveRecordRepository leaveRecordRepository,
            MaidRepository maidRepository,
            AuditEventRepository auditEventRepository,
            ObjectMapper objectMapper) {
        this.leaveRecordRepository = leaveRecordRepository;
        this.maidRepository = maidRepository;
        this.auditEventRepository = auditEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public LeaveRecordDto createLeave(UUID ownerId, UUID maidId, CreateLeaveRequest request) {
        Maid maid = getMaidOrThrow(maidId, ownerId);

        if (leaveRecordRepository.findByMaidIdAndDate(maidId, ownerId, request.getLeaveDate()).isPresent()) {
            throw new ConflictException("A leave record already exists for this worker on " + request.getLeaveDate());
        }

        LeaveRecord record = new LeaveRecord(
                UUID.randomUUID(),
                maid,
                request.getLeaveDate(),
                request.getLeaveType(),
                request.getNote()
        );

        record = leaveRecordRepository.save(record);
        recordAudit(maid.getOwner(), "LEAVE_RECORD", record.getId(), "CREATE", null, record);

        return LeaveRecordDto.fromEntity(record);
    }

    @Transactional(readOnly = true)
    public List<LeaveRecordDto> getLeaveRecords(UUID ownerId, UUID maidId, LocalDate fromDate, LocalDate toDate) {
        getMaidOrThrow(maidId, ownerId);

        LocalDate from = fromDate != null ? fromDate : YearMonth.now().atDay(1);
        LocalDate to = toDate != null ? toDate : YearMonth.now().atEndOfMonth();

        List<LeaveRecord> records = leaveRecordRepository.findAllByMaidIdAndDateRange(maidId, ownerId, from, to);
        return records.stream().map(LeaveRecordDto::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public LeaveRecordDto updateLeave(UUID ownerId, UUID maidId, UUID leaveId, UpdateLeaveRequest request) {
        Maid maid = getMaidOrThrow(maidId, ownerId);

        LeaveRecord record = leaveRecordRepository.findByIdAndOwnerId(leaveId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave record not found"));

        if (!record.getMaid().getId().equals(maidId)) {
            throw new ResourceNotFoundException("Leave record does not belong to specified worker");
        }

        String beforeJson = serializeQuietly(record);

        if (request.getLeaveType() != null) {
            record.setLeaveType(request.getLeaveType());
        }
        if (request.getNote() != null) {
            record.setNote(request.getNote());
        }

        record = leaveRecordRepository.save(record);
        recordAudit(maid.getOwner(), "LEAVE_RECORD", record.getId(), "UPDATE", beforeJson, record);

        return LeaveRecordDto.fromEntity(record);
    }

    @Transactional
    public void deleteLeave(UUID ownerId, UUID maidId, UUID leaveId) {
        Maid maid = getMaidOrThrow(maidId, ownerId);

        LeaveRecord record = leaveRecordRepository.findByIdAndOwnerId(leaveId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave record not found"));

        if (!record.getMaid().getId().equals(maidId)) {
            throw new ResourceNotFoundException("Leave record does not belong to specified worker");
        }

        String beforeJson = serializeQuietly(record);
        leaveRecordRepository.delete(record);
        recordAudit(maid.getOwner(), "LEAVE_RECORD", leaveId, "DELETE", beforeJson, null);
    }

    private Maid getMaidOrThrow(UUID maidId, UUID ownerId) {
        return maidRepository.findByIdAndOwnerId(maidId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found"));
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
