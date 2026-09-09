package com.example.maidmanager;

import com.example.maidmanager.audit.entity.AuditEvent;
import com.example.maidmanager.audit.repository.AuditEventRepository;
import com.example.maidmanager.auth.dto.AuthResponse;
import com.example.maidmanager.auth.dto.RegisterRequest;
import com.example.maidmanager.common.enums.LeaveType;
import com.example.maidmanager.common.enums.SalaryMode;
import com.example.maidmanager.employment.dto.CreateEmploymentConfigRequest;
import com.example.maidmanager.holiday.dto.CreateHolidayRequest;
import com.example.maidmanager.holiday.dto.HolidayDto;
import com.example.maidmanager.holiday.dto.UpdateHolidayRequest;
import com.example.maidmanager.leave.dto.CreateLeaveRequest;
import com.example.maidmanager.leave.dto.LeaveRecordDto;
import com.example.maidmanager.leave.dto.UpdateLeaveRequest;
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
class LeaveAndHolidayIntegrationTest {

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
        RegisterRequest reqA = new RegisterRequest("Owner Alpha", "alpha_leave@example.com", "Password123", "+919000000001", "Asia/Kolkata", "INR");
        MvcResult resA = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqA)))
                .andExpect(status().isCreated())
                .andReturn();
        AuthResponse authA = objectMapper.readValue(resA.getResponse().getContentAsString(), AuthResponse.class);
        tokenOwnerA = authA.getAccessToken();
        ownerAId = authA.getOwner().getId();

        RegisterRequest reqB = new RegisterRequest("Owner Beta", "beta_leave@example.com", "Password123", "+919000000002", "Asia/Kolkata", "INR");
        MvcResult resB = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqB)))
                .andExpect(status().isCreated())
                .andReturn();
        tokenOwnerB = objectMapper.readValue(resB.getResponse().getContentAsString(), AuthResponse.class).getAccessToken();

        // Create maid for Owner A
        CreateEmploymentConfigRequest configReq = new CreateEmploymentConfigRequest(
                LocalDate.of(2026, 1, 1), null,
                SalaryMode.MONTHLY, new BigDecimal("14000.00"),
                480, 450, false, new BigDecimal("1.000"),
                Set.of((short) 1, (short) 2, (short) 3, (short) 4, (short) 5, (short) 6)
        );
        CreateMaidRequest createMaidReq = new CreateMaidRequest(
                "Kamla", "+919876543210", LocalDate.of(2026, 1, 1), null, "Cooking", configReq
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
    @DisplayName("Verify leave lifecycle, conflict validation, and audit recording")
    void testLeaveLifecycle() throws Exception {
        LocalDate leaveDate = LocalDate.of(2026, 9, 15);

        // 1. Create Paid Leave
        CreateLeaveRequest leaveReq = new CreateLeaveRequest(leaveDate, LeaveType.PAID, "Family function");
        MvcResult leaveResult = mockMvc.perform(post("/api/v1/maids/" + maidAId + "/leave")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(leaveReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.leaveType").value("PAID"))
                .andExpect(jsonPath("$.leaveDate").value(leaveDate.toString()))
                .andReturn();

        LeaveRecordDto createdLeave = objectMapper.readValue(
                leaveResult.getResponse().getContentAsString(), LeaveRecordDto.class
        );

        // 2. Duplicate leave on same date -> 409 Conflict
        mockMvc.perform(post("/api/v1/maids/" + maidAId + "/leave")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(leaveReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_CONFLICT"));

        // 3. Update Leave
        UpdateLeaveRequest updateReq = new UpdateLeaveRequest(LeaveType.UNPAID, "Updated to unpaid leave");
        mockMvc.perform(patch("/api/v1/maids/" + maidAId + "/leave/" + createdLeave.getId())
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leaveType").value("UNPAID"))
                .andExpect(jsonPath("$.note").value("Updated to unpaid leave"));

        // 4. Query Leave list
        MvcResult listResult = mockMvc.perform(get("/api/v1/maids/" + maidAId + "/leave")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .param("from", "2026-09-01")
                        .param("to", "2026-09-30"))
                .andExpect(status().isOk())
                .andReturn();

        List<LeaveRecordDto> leaves = objectMapper.readValue(
                listResult.getResponse().getContentAsString(), new TypeReference<List<LeaveRecordDto>>() {}
        );
        assertThat(leaves).hasSize(1);

        // 5. Delete Leave
        mockMvc.perform(delete("/api/v1/maids/" + maidAId + "/leave/" + createdLeave.getId())
                        .header("Authorization", "Bearer " + tokenOwnerA))
                .andExpect(status().isNoContent());

        List<AuditEvent> auditEvents = auditEventRepository.findAllByEntity(ownerAId, "LEAVE_RECORD", createdLeave.getId());
        assertThat(auditEvents).hasSize(3); // CREATE, UPDATE, DELETE
    }

    @Test
    @DisplayName("Verify holiday lifecycle, conflict validation, and audit recording")
    void testHolidayLifecycle() throws Exception {
        LocalDate holidayDate = LocalDate.of(2026, 10, 2);

        // 1. Create Holiday
        CreateHolidayRequest holidayReq = new CreateHolidayRequest(holidayDate, "Gandhi Jayanti");
        MvcResult holidayResult = mockMvc.perform(post("/api/v1/holidays")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(holidayReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Gandhi Jayanti"))
                .andExpect(jsonPath("$.holidayDate").value(holidayDate.toString()))
                .andReturn();

        HolidayDto createdHoliday = objectMapper.readValue(
                holidayResult.getResponse().getContentAsString(), HolidayDto.class
        );

        // 2. Duplicate holiday on same date for owner -> 409 Conflict
        mockMvc.perform(post("/api/v1/holidays")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(holidayReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_CONFLICT"));

        // 3. Update Holiday
        UpdateHolidayRequest updateReq = new UpdateHolidayRequest(null, "Mahatma Gandhi Birthday");
        mockMvc.perform(patch("/api/v1/holidays/" + createdHoliday.getId())
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mahatma Gandhi Birthday"));

        // 4. Query Holiday list
        MvcResult listResult = mockMvc.perform(get("/api/v1/holidays")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .param("year", "2026"))
                .andExpect(status().isOk())
                .andReturn();

        List<HolidayDto> holidays = objectMapper.readValue(
                listResult.getResponse().getContentAsString(), new TypeReference<List<HolidayDto>>() {}
        );
        assertThat(holidays).hasSize(1);

        // 5. Delete Holiday
        mockMvc.perform(delete("/api/v1/holidays/" + createdHoliday.getId())
                        .header("Authorization", "Bearer " + tokenOwnerA))
                .andExpect(status().isNoContent());

        List<AuditEvent> auditEvents = auditEventRepository.findAllByEntity(ownerAId, "HOLIDAY", createdHoliday.getId());
        assertThat(auditEvents).hasSize(3); // CREATE, UPDATE, DELETE
    }

    @Test
    @DisplayName("Verify cross-tenant isolation on leave and holiday endpoints")
    void testCrossTenantIsolation() throws Exception {
        // Owner A creates leave and holiday
        LocalDate leaveDate = LocalDate.of(2026, 9, 20);
        MvcResult leaveRes = mockMvc.perform(post("/api/v1/maids/" + maidAId + "/leave")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateLeaveRequest(leaveDate, LeaveType.PAID, "Medical"))))
                .andExpect(status().isCreated())
                .andReturn();
        LeaveRecordDto leaveA = objectMapper.readValue(leaveRes.getResponse().getContentAsString(), LeaveRecordDto.class);

        LocalDate holidayDate = LocalDate.of(2026, 11, 1);
        MvcResult holidayRes = mockMvc.perform(post("/api/v1/holidays")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateHolidayRequest(holidayDate, "Diwali"))))
                .andExpect(status().isCreated())
                .andReturn();
        HolidayDto holidayA = objectMapper.readValue(holidayRes.getResponse().getContentAsString(), HolidayDto.class);

        // Owner B attempts to access Owner A's leave -> 404
        mockMvc.perform(get("/api/v1/maids/" + maidAId + "/leave")
                        .header("Authorization", "Bearer " + tokenOwnerB))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/v1/maids/" + maidAId + "/leave")
                        .header("Authorization", "Bearer " + tokenOwnerB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateLeaveRequest(leaveDate, LeaveType.PAID, "Hack"))))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/api/v1/maids/" + maidAId + "/leave/" + leaveA.getId())
                        .header("Authorization", "Bearer " + tokenOwnerB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateLeaveRequest(LeaveType.UNPAID, "Hack"))))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/v1/maids/" + maidAId + "/leave/" + leaveA.getId())
                        .header("Authorization", "Bearer " + tokenOwnerB))
                .andExpect(status().isNotFound());

        // Owner B attempts to access Owner A's holiday
        mockMvc.perform(get("/api/v1/holidays")
                        .header("Authorization", "Bearer " + tokenOwnerB)
                        .param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(patch("/api/v1/holidays/" + holidayA.getId())
                        .header("Authorization", "Bearer " + tokenOwnerB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateHolidayRequest(null, "Hack"))))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/v1/holidays/" + holidayA.getId())
                        .header("Authorization", "Bearer " + tokenOwnerB))
                .andExpect(status().isNotFound());
    }
}
