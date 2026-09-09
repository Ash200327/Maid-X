package com.example.maidmanager;

import com.example.maidmanager.attendance.dto.AttendanceSessionDto;
import com.example.maidmanager.attendance.dto.DashboardResponse;
import com.example.maidmanager.attendance.dto.EntryRequest;
import com.example.maidmanager.attendance.dto.ExitRequest;
import com.example.maidmanager.attendance.dto.ManualStatusRequest;
import com.example.maidmanager.attendance.dto.UpdateAttendanceSessionRequest;
import com.example.maidmanager.audit.entity.AuditEvent;
import com.example.maidmanager.audit.repository.AuditEventRepository;
import com.example.maidmanager.auth.dto.AuthResponse;
import com.example.maidmanager.auth.dto.RegisterRequest;
import com.example.maidmanager.common.enums.AttendanceStatus;
import com.example.maidmanager.common.enums.SalaryMode;
import com.example.maidmanager.employment.dto.CreateEmploymentConfigRequest;
import com.example.maidmanager.maid.dto.CreateMaidRequest;
import com.example.maidmanager.maid.dto.MaidDetailDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AttendanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuditEventRepository auditEventRepository;

    private String tokenOwnerA;
    private String tokenOwnerB;
    private UUID ownerAId;
    private UUID maidAId;

    @BeforeEach
    void setUp() throws Exception {
        RegisterRequest reqA = new RegisterRequest("Owner Alpha", "alpha_att@example.com", "Password123", "+919000000001", "Asia/Kolkata", "INR");
        MvcResult resA = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqA)))
                .andExpect(status().isCreated())
                .andReturn();
        AuthResponse authA = objectMapper.readValue(resA.getResponse().getContentAsString(), AuthResponse.class);
        tokenOwnerA = authA.getAccessToken();
        ownerAId = authA.getOwner().getId();

        RegisterRequest reqB = new RegisterRequest("Owner Beta", "beta_att@example.com", "Password123", "+919000000002", "Asia/Kolkata", "INR");
        MvcResult resB = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqB)))
                .andExpect(status().isCreated())
                .andReturn();
        tokenOwnerB = objectMapper.readValue(resB.getResponse().getContentAsString(), AuthResponse.class).getAccessToken();

        // Create maid for Owner A
        CreateEmploymentConfigRequest configReq = new CreateEmploymentConfigRequest(
                LocalDate.of(2026, 1, 1), null,
                SalaryMode.MONTHLY, new BigDecimal("12000.00"),
                480, 450, false, new BigDecimal("1.000"),
                Set.of((short) 1, (short) 2, (short) 3, (short) 4, (short) 5, (short) 6)
        );
        CreateMaidRequest createMaidReq = new CreateMaidRequest(
                "Sunita", "+919876543210", LocalDate.of(2026, 1, 1), null, "Regular", configReq
        );
        MvcResult maidResult = mockMvc.perform(post("/api/v1/maids")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createMaidReq)))
                .andExpect(status().isCreated())
                .andReturn();
        maidAId = objectMapper.readValue(maidResult.getResponse().getContentAsString(), MaidDetailDto.class).getId();
    }

    @Test
    @DisplayName("Verify entry and exit recording, conflict handling, and multiple sessions")
    void testEntryExitWorkflow() throws Exception {
        LocalDate today = LocalDate.of(2026, 9, 9);

        // 1. Record Entry
        EntryRequest entryReq = new EntryRequest(today, "req-1", "Morning entry");
        MvcResult entryRes = mockMvc.perform(post("/api/v1/maids/" + maidAId + "/attendance/sessions/entry")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entryReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("WORKING"))
                .andExpect(jsonPath("$.entryAt").isNotEmpty())
                .andExpect(jsonPath("$.exitAt").isEmpty())
                .andReturn();

        AttendanceSessionDto session1 = objectMapper.readValue(
                entryRes.getResponse().getContentAsString(), AttendanceSessionDto.class
        );

        // 2. Duplicate entry on same date while open -> 409 Conflict
        mockMvc.perform(post("/api/v1/maids/" + maidAId + "/attendance/sessions/entry")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entryReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_CONFLICT"));

        // 3. Record Exit
        ExitRequest exitReq = new ExitRequest("exit-1", "Morning exit");
        mockMvc.perform(post("/api/v1/maids/" + maidAId + "/attendance/sessions/" + session1.getId() + "/exit")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exitReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.exitAt").isNotEmpty());

        // 4. Duplicate exit on same session -> 409 Conflict
        mockMvc.perform(post("/api/v1/maids/" + maidAId + "/attendance/sessions/" + session1.getId() + "/exit")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exitReq)))
                .andExpect(status().isConflict());

        // 5. Multiple sessions: Record second entry on same date (e.g. evening shift)
        EntryRequest entryReq2 = new EntryRequest(today, "req-2", "Evening entry");
        MvcResult entryRes2 = mockMvc.perform(post("/api/v1/maids/" + maidAId + "/attendance/sessions/entry")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entryReq2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("WORKING"))
                .andReturn();

        AttendanceSessionDto session2 = objectMapper.readValue(
                entryRes2.getResponse().getContentAsString(), AttendanceSessionDto.class
        );

        mockMvc.perform(post("/api/v1/maids/" + maidAId + "/attendance/sessions/" + session2.getId() + "/exit")
                        .header("Authorization", "Bearer " + tokenOwnerA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // 6. Query attendance list for the date range
        MvcResult listRes = mockMvc.perform(get("/api/v1/maids/" + maidAId + "/attendance")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .param("from", today.toString())
                        .param("to", today.toString()))
                .andExpect(status().isOk())
                .andReturn();

        List<AttendanceSessionDto> sessions = objectMapper.readValue(
                listRes.getResponse().getContentAsString(), new TypeReference<List<AttendanceSessionDto>>() {}
        );
        assertThat(sessions).hasSize(2);
    }

    @Test
    @DisplayName("Verify manual edit, deletion and audit event trail")
    void testManualEditAndAuditTrail() throws Exception {
        LocalDate date = LocalDate.of(2026, 9, 8);

        // Record Entry and Exit
        MvcResult entryRes = mockMvc.perform(post("/api/v1/maids/" + maidAId + "/attendance/sessions/entry")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EntryRequest(date, null, null))))
                .andExpect(status().isCreated())
                .andReturn();
        AttendanceSessionDto session = objectMapper.readValue(
                entryRes.getResponse().getContentAsString(), AttendanceSessionDto.class
        );

        mockMvc.perform(post("/api/v1/maids/" + maidAId + "/attendance/sessions/" + session.getId() + "/exit")
                        .header("Authorization", "Bearer " + tokenOwnerA))
                .andExpect(status().isOk());

        // Update session
        Instant newEntry = Instant.parse("2026-09-08T03:30:00Z");
        Instant newExit = Instant.parse("2026-09-08T11:30:00Z");
        UpdateAttendanceSessionRequest updateReq = new UpdateAttendanceSessionRequest(
                newEntry, newExit, AttendanceStatus.COMPLETED, "Manual correction by owner"
        );

        mockMvc.perform(patch("/api/v1/maids/" + maidAId + "/attendance/sessions/" + session.getId())
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.durationMinutes").value(480));

        // Verify audit event exists
        List<AuditEvent> auditEvents = auditEventRepository.findAllByEntity(ownerAId, "ATTENDANCE_SESSION", session.getId());
        assertThat(auditEvents).hasSize(1);
        assertThat(auditEvents.get(0).getAction()).isEqualTo("UPDATE");

        // Delete session
        mockMvc.perform(delete("/api/v1/maids/" + maidAId + "/attendance/sessions/" + session.getId())
                        .header("Authorization", "Bearer " + tokenOwnerA))
                .andExpect(status().isNoContent());

        List<AuditEvent> updatedAuditEvents = auditEventRepository.findAllByEntity(ownerAId, "ATTENDANCE_SESSION", session.getId());
        assertThat(updatedAuditEvents).hasSize(2);
        assertThat(updatedAuditEvents.stream().anyMatch(e -> "DELETE".equals(e.getAction()))).isTrue();
    }

    @Test
    @DisplayName("Verify manual status absent and dashboard summary")
    void testManualStatusAndDashboard() throws Exception {
        LocalDate date = LocalDate.of(2026, 9, 10);

        // Record manual absent status
        ManualStatusRequest absentReq = new ManualStatusRequest(date, AttendanceStatus.ABSENT, "Sick leave without notice");
        mockMvc.perform(post("/api/v1/maids/" + maidAId + "/attendance/manual-status")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(absentReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABSENT"));

        // Check Dashboard
        MvcResult dashRes = mockMvc.perform(get("/api/v1/dashboard")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeMaidCount").value(1))
                .andExpect(jsonPath("$.entries[0].state").value("ABSENT"))
                .andExpect(jsonPath("$.entries[0].maidName").value("Sunita"))
                .andExpect(jsonPath("$.currentMonth.salaryPayable").value(12000.00))
                .andReturn();

        DashboardResponse dashboard = objectMapper.readValue(dashRes.getResponse().getContentAsString(), DashboardResponse.class);
        assertThat(dashboard.getEntries()).hasSize(1);
    }

    @Test
    @DisplayName("Verify strict tenant isolation on attendance endpoints")
    void testCrossTenantAttendanceIsolation() throws Exception {
        LocalDate date = LocalDate.of(2026, 9, 9);
        MvcResult entryRes = mockMvc.perform(post("/api/v1/maids/" + maidAId + "/attendance/sessions/entry")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EntryRequest(date, null, null))))
                .andExpect(status().isCreated())
                .andReturn();
        AttendanceSessionDto session = objectMapper.readValue(
                entryRes.getResponse().getContentAsString(), AttendanceSessionDto.class
        );

        // Owner B attempts to entry for Owner A's maid -> 404
        mockMvc.perform(post("/api/v1/maids/" + maidAId + "/attendance/sessions/entry")
                        .header("Authorization", "Bearer " + tokenOwnerB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EntryRequest(date, null, null))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        // Owner B attempts to exit Owner A's session -> 404
        mockMvc.perform(post("/api/v1/maids/" + maidAId + "/attendance/sessions/" + session.getId() + "/exit")
                        .header("Authorization", "Bearer " + tokenOwnerB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        // Owner B attempts to patch Owner A's session -> 404
        mockMvc.perform(patch("/api/v1/maids/" + maidAId + "/attendance/sessions/" + session.getId())
                        .header("Authorization", "Bearer " + tokenOwnerB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateAttendanceSessionRequest())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        // Owner B attempts to delete Owner A's session -> 404
        mockMvc.perform(delete("/api/v1/maids/" + maidAId + "/attendance/sessions/" + session.getId())
                        .header("Authorization", "Bearer " + tokenOwnerB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        // Owner B dashboard does not see Owner A's maid
        mockMvc.perform(get("/api/v1/dashboard")
                        .header("Authorization", "Bearer " + tokenOwnerB)
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeMaidCount").value(0))
                .andExpect(jsonPath("$.entries.length()").value(0));
    }
}
