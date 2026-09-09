package com.example.maidmanager;

import com.example.maidmanager.attendance.entity.AttendanceSession;
import com.example.maidmanager.attendance.repository.AttendanceSessionRepository;
import com.example.maidmanager.auth.dto.AuthResponse;
import com.example.maidmanager.auth.dto.RegisterRequest;
import com.example.maidmanager.common.enums.AttendanceStatus;
import com.example.maidmanager.common.enums.LeaveType;
import com.example.maidmanager.common.enums.PayrollAdjustmentType;
import com.example.maidmanager.common.enums.PayrollStatus;
import com.example.maidmanager.common.enums.SalaryMode;
import com.example.maidmanager.employment.dto.CreateEmploymentConfigRequest;
import com.example.maidmanager.leave.dto.CreateLeaveRequest;
import com.example.maidmanager.maid.dto.CreateMaidRequest;
import com.example.maidmanager.maid.dto.MaidDetailDto;
import com.example.maidmanager.owner.entity.Owner;
import com.example.maidmanager.owner.repository.OwnerRepository;
import com.example.maidmanager.payroll.dto.MarkPaidRequest;
import com.example.maidmanager.payroll.dto.MonthlyReportResponse;
import com.example.maidmanager.payroll.dto.PayrollAdjustmentDto;
import com.example.maidmanager.payroll.dto.PayrollAdjustmentRequest;
import com.example.maidmanager.payroll.dto.PayrollCalculateRequest;
import com.example.maidmanager.payroll.dto.PayrollRunDto;
import com.example.maidmanager.payroll.engine.PayrollCalculationResult;
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
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PayrollIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AttendanceSessionRepository attendanceSessionRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    private String tokenOwnerA;
    private String tokenOwnerB;
    private UUID ownerAId;
    private UUID ownerBId;
    private UUID maidAId;
    private final ZoneId zoneId = ZoneId.of("Asia/Kolkata");

    @BeforeEach
    void setUp() throws Exception {
        RegisterRequest reqA = new RegisterRequest("Owner Alpha", "alpha_payroll@example.com", "Password123", "+919000000001", "Asia/Kolkata", "INR");
        MvcResult resA = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqA)))
                .andExpect(status().isCreated())
                .andReturn();
        AuthResponse authA = objectMapper.readValue(resA.getResponse().getContentAsString(), AuthResponse.class);
        tokenOwnerA = authA.getAccessToken();
        ownerAId = authA.getOwner().getId();

        RegisterRequest reqB = new RegisterRequest("Owner Beta", "beta_payroll@example.com", "Password123", "+919000000002", "Asia/Kolkata", "INR");
        MvcResult resB = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqB)))
                .andExpect(status().isCreated())
                .andReturn();
        AuthResponse authB = objectMapper.readValue(resB.getResponse().getContentAsString(), AuthResponse.class);
        tokenOwnerB = authB.getAccessToken();
        ownerBId = authB.getOwner().getId();

        // Create maid for Owner A with Monthly ₹15,000 config
        CreateEmploymentConfigRequest configReq = new CreateEmploymentConfigRequest(
                LocalDate.of(2026, 1, 1), null,
                SalaryMode.MONTHLY, new BigDecimal("15000.00"),
                480, 450, true, new BigDecimal("1.500"),
                Set.of((short) 1, (short) 2, (short) 3, (short) 4, (short) 5, (short) 6)
        );
        CreateMaidRequest createMaidReq = new CreateMaidRequest(
                "Kamla Bai", "+919876543210", LocalDate.of(2026, 1, 1), null, "Cleaning & Cooking", configReq
        );
        MvcResult maidResult = mockMvc.perform(post("/api/v1/maids")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createMaidReq)))
                .andExpect(status().isCreated())
                .andReturn();
        maidAId = objectMapper.readValue(maidResult.getResponse().getContentAsString(), MaidDetailDto.class).getId();
    }

    private void createCompletedSession(LocalDate date, LocalTime start, LocalTime end) {
        Owner owner = ownerRepository.findById(ownerAId).orElseThrow();
        Instant entryAt = date.atTime(start).atZone(zoneId).toInstant();
        Instant exitAt = date.atTime(end).atZone(zoneId).toInstant();
        AttendanceSession session = new AttendanceSession(
                UUID.randomUUID(),
                null,
                date,
                entryAt,
                exitAt,
                AttendanceStatus.COMPLETED,
                "Shift",
                owner,
                owner
        );
        session.setMaid(new com.example.maidmanager.maid.entity.Maid(maidAId, owner, "Kamla Bai", "+919876543210", LocalDate.of(2026, 1, 1), null, null, true));
        attendanceSessionRepository.save(session);
    }

    @Test
    @DisplayName("Preview calculation returns dynamic projection without creating payroll run")
    void testPreviewCalculation() throws Exception {
        // Add attendance on Sept 1: 8h
        createCompletedSession(LocalDate.of(2026, 9, 1), LocalTime.of(9, 0), LocalTime.of(17, 0));

        PayrollCalculateRequest request = new PayrollCalculateRequest(maidAId, "2026-09");
        MvcResult result = mockMvc.perform(post("/api/v1/payroll/runs/calculate")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        PayrollCalculationResult calc = objectMapper.readValue(result.getResponse().getContentAsString(), PayrollCalculationResult.class);
        assertThat(calc.getMonth()).isEqualTo("2026-09");
        assertThat(calc.getBaseEarnings()).isNotNull();

        // Verify no run was persisted in DB
        mockMvc.perform(get("/api/v1/payroll/runs?month=2026-09")
                        .header("Authorization", "Bearer " + tokenOwnerA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Complete Lifecycle: Draft -> Adjustments -> Finalize (Snapshot) -> Mark Paid -> Report")
    void testCompletePayrollLifecycle() throws Exception {
        // Attendance for Sept 1 - Sept 5 (8h each)
        for (int d = 1; d <= 5; d++) {
            createCompletedSession(LocalDate.of(2026, 9, d), LocalTime.of(9, 0), LocalTime.of(17, 0));
        }

        // 1. Create Draft Run
        PayrollCalculateRequest createDraft = new PayrollCalculateRequest(maidAId, "2026-09");
        MvcResult draftRes = mockMvc.perform(post("/api/v1/payroll/runs")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDraft)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.maidName").value("Kamla Bai"))
                .andReturn();

        PayrollRunDto runDto = objectMapper.readValue(draftRes.getResponse().getContentAsString(), PayrollRunDto.class);
        UUID runId = runDto.getId();
        assertThat(runId).isNotNull();

        // 2. Add BONUS Adjustment (+1000)
        PayrollAdjustmentRequest bonusReq = new PayrollAdjustmentRequest(
                PayrollAdjustmentType.BONUS, new BigDecimal("1000.00"), LocalDate.of(2026, 9, 15), "Performance bonus"
        );
        mockMvc.perform(post("/api/v1/payroll/runs/" + runId + "/adjustments")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bonusReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("BONUS"))
                .andExpect(jsonPath("$.amount").value(1000.00));

        // 3. Add ADVANCE Adjustment (-500)
        PayrollAdjustmentRequest advReq = new PayrollAdjustmentRequest(
                PayrollAdjustmentType.ADVANCE, new BigDecimal("500.00"), LocalDate.of(2026, 9, 10), "Advance cash"
        );
        MvcResult advRes = mockMvc.perform(post("/api/v1/payroll/runs/" + runId + "/adjustments")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(advReq)))
                .andExpect(status().isCreated())
                .andReturn();
        UUID advId = objectMapper.readValue(advRes.getResponse().getContentAsString(), PayrollAdjustmentDto.class).getId();

        // 4. Add temporary DEDUCTION and delete it
        PayrollAdjustmentRequest dedReq = new PayrollAdjustmentRequest(
                PayrollAdjustmentType.DEDUCTION, new BigDecimal("200.00"), LocalDate.of(2026, 9, 20), "Temp"
        );
        MvcResult dedRes = mockMvc.perform(post("/api/v1/payroll/runs/" + runId + "/adjustments")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dedReq)))
                .andExpect(status().isCreated())
                .andReturn();
        UUID dedId = objectMapper.readValue(dedRes.getResponse().getContentAsString(), PayrollAdjustmentDto.class).getId();

        mockMvc.perform(delete("/api/v1/payroll/runs/" + runId + "/adjustments/" + dedId)
                        .header("Authorization", "Bearer " + tokenOwnerA))
                .andExpect(status().isNoContent());

        // 5. Finalize Payroll Run
        MvcResult finalizeRes = mockMvc.perform(post("/api/v1/payroll/runs/" + runId + "/finalize")
                        .header("Authorization", "Bearer " + tokenOwnerA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINALIZED"))
                .andExpect(jsonPath("$.finalizedAt").isNotEmpty())
                .andExpect(jsonPath("$.additions").value(1000.00))
                .andExpect(jsonPath("$.deductions").value(500.00))
                .andReturn();

        PayrollRunDto finalizedRun = objectMapper.readValue(finalizeRes.getResponse().getContentAsString(), PayrollRunDto.class);
        BigDecimal payableAtFinalize = finalizedRun.getFinalPayable();
        assertThat(payableAtFinalize).isNotNull();

        // 6. Verify Immutable Snapshot: Add subsequent attendance on Sept 10
        createCompletedSession(LocalDate.of(2026, 9, 10), LocalTime.of(9, 0), LocalTime.of(17, 0));

        // Fetching finalized run must return the exact same snapshot calculation
        MvcResult getFinalizedRes = mockMvc.perform(get("/api/v1/payroll/runs/" + runId)
                        .header("Authorization", "Bearer " + tokenOwnerA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINALIZED"))
                .andReturn();

        PayrollRunDto verifiedFinalized = objectMapper.readValue(getFinalizedRes.getResponse().getContentAsString(), PayrollRunDto.class);
        assertThat(verifiedFinalized.getFinalPayable()).isEqualByComparingTo(payableAtFinalize);

        // 7. Verify modifications on FINALIZED run are rejected
        mockMvc.perform(post("/api/v1/payroll/runs/" + runId + "/adjustments")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bonusReq)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/v1/payroll/runs/" + runId + "/adjustments/" + advId)
                        .header("Authorization", "Bearer " + tokenOwnerA))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/payroll/runs/" + runId + "/finalize")
                        .header("Authorization", "Bearer " + tokenOwnerA))
                .andExpect(status().isBadRequest());

        // 8. Mark Paid
        MarkPaidRequest markPaidReq = new MarkPaidRequest(Instant.now(), "UPI", "Paid via Google Pay");
        mockMvc.perform(post("/api/v1/payroll/runs/" + runId + "/mark-paid")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(markPaidReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.paymentMethod").value("UPI"))
                .andExpect(jsonPath("$.paymentNote").value("Paid via Google Pay"))
                .andExpect(jsonPath("$.paidAt").isNotEmpty());

        // Marking paid again rejected
        mockMvc.perform(post("/api/v1/payroll/runs/" + runId + "/mark-paid")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(markPaidReq)))
                .andExpect(status().isBadRequest());

        // 9. Monthly Report and CSV Export
        mockMvc.perform(get("/api/v1/reports/monthly?month=2026-09")
                        .header("Authorization", "Bearer " + tokenOwnerA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value("2026-09"))
                .andExpect(jsonPath("$.totalMaids").value(1))
                .andExpect(jsonPath("$.paidCount").value(1))
                .andExpect(jsonPath("$.runs[0].status").value("PAID"));

        MvcResult csvRes = mockMvc.perform(get("/api/v1/reports/monthly.csv?month=2026-09")
                        .header("Authorization", "Bearer " + tokenOwnerA))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"payroll-report-2026-09.csv\""))
                .andReturn();

        String csv = csvRes.getResponse().getContentAsString();
        assertThat(csv).contains("Maid Name,Month,Status,Configured Salary");
        assertThat(csv).contains("Kamla Bai");
        assertThat(csv).contains("PAID");
        assertThat(csv).contains("UPI");
    }

    @Test
    @DisplayName("Cannot mark paid directly from DRAFT status")
    void testCannotMarkPaidDraftRun() throws Exception {
        PayrollCalculateRequest createDraft = new PayrollCalculateRequest(maidAId, "2026-09");
        MvcResult draftRes = mockMvc.perform(post("/api/v1/payroll/runs")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDraft)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID runId = objectMapper.readValue(draftRes.getResponse().getContentAsString(), PayrollRunDto.class).getId();

        MarkPaidRequest markPaidReq = new MarkPaidRequest(Instant.now(), "CASH", "Direct cash");
        mockMvc.perform(post("/api/v1/payroll/runs/" + runId + "/mark-paid")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(markPaidReq)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Cross-tenant isolation: Owner B cannot calculate, view, or modify Owner A payroll")
    void testTenantIsolation() throws Exception {
        // Create draft for Owner A
        PayrollCalculateRequest createDraft = new PayrollCalculateRequest(maidAId, "2026-09");
        MvcResult draftRes = mockMvc.perform(post("/api/v1/payroll/runs")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDraft)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID runId = objectMapper.readValue(draftRes.getResponse().getContentAsString(), PayrollRunDto.class).getId();

        // Owner B tries to calculate preview for Owner A's maid -> 404
        mockMvc.perform(post("/api/v1/payroll/runs/calculate")
                        .header("Authorization", "Bearer " + tokenOwnerB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDraft)))
                .andExpect(status().isNotFound());

        // Owner B tries to get Owner A's run -> 404
        mockMvc.perform(get("/api/v1/payroll/runs/" + runId)
                        .header("Authorization", "Bearer " + tokenOwnerB))
                .andExpect(status().isNotFound());

        // Owner B tries to add adjustment to Owner A's run -> 404
        PayrollAdjustmentRequest adjReq = new PayrollAdjustmentRequest(
                PayrollAdjustmentType.BONUS, new BigDecimal("100.00"), LocalDate.of(2026, 9, 10), "Hacking"
        );
        mockMvc.perform(post("/api/v1/payroll/runs/" + runId + "/adjustments")
                        .header("Authorization", "Bearer " + tokenOwnerB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adjReq)))
                .andExpect(status().isNotFound());

        // Owner B tries to finalize Owner A's run -> 404
        mockMvc.perform(post("/api/v1/payroll/runs/" + runId + "/finalize")
                        .header("Authorization", "Bearer " + tokenOwnerB))
                .andExpect(status().isNotFound());

        // Owner B's report contains 0 maids
        mockMvc.perform(get("/api/v1/reports/monthly?month=2026-09")
                        .header("Authorization", "Bearer " + tokenOwnerB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMaids").value(0))
                .andExpect(jsonPath("$.runs.length()").value(0));
    }
}
