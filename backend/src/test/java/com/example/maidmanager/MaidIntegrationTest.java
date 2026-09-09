package com.example.maidmanager;

import com.example.maidmanager.auth.dto.AuthResponse;
import com.example.maidmanager.auth.dto.RegisterRequest;
import com.example.maidmanager.common.enums.SalaryMode;
import com.example.maidmanager.employment.dto.CreateEmploymentConfigRequest;
import com.example.maidmanager.employment.dto.EmploymentConfigDto;
import com.example.maidmanager.maid.dto.CreateMaidRequest;
import com.example.maidmanager.maid.dto.MaidDetailDto;
import com.example.maidmanager.maid.dto.UpdateMaidRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MaidIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String tokenOwnerA;
    private String tokenOwnerB;

    @BeforeEach
    void setUp() throws Exception {
        RegisterRequest reqA = new RegisterRequest("Owner Alpha", "alpha@example.com", "Password123", "+919000000001", "Asia/Kolkata", "INR");
        MvcResult resA = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqA)))
                .andExpect(status().isCreated())
                .andReturn();
        tokenOwnerA = objectMapper.readValue(resA.getResponse().getContentAsString(), AuthResponse.class).getAccessToken();

        RegisterRequest reqB = new RegisterRequest("Owner Beta", "beta@example.com", "Password123", "+919000000002", "Asia/Kolkata", "INR");
        MvcResult resB = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqB)))
                .andExpect(status().isCreated())
                .andReturn();
        tokenOwnerB = objectMapper.readValue(resB.getResponse().getContentAsString(), AuthResponse.class).getAccessToken();
    }

    @Test
    @DisplayName("Verify full lifecycle of Maid and Employment Config with tenant isolation")
    void testMaidLifecycleAndIsolation() throws Exception {
        // 1. Owner A creates a maid
        CreateEmploymentConfigRequest configReq = new CreateEmploymentConfigRequest(
                LocalDate.of(2026, 1, 1), null,
                SalaryMode.MONTHLY, new BigDecimal("15000.00"),
                480, 450, false, new BigDecimal("1.000"),
                Set.of((short) 1, (short) 2, (short) 3, (short) 4, (short) 5, (short) 6)
        );

        CreateMaidRequest createMaidReq = new CreateMaidRequest(
                "Geeta", "+919876543210", LocalDate.of(2026, 1, 1), null, "Daily cleaning and cooking", configReq
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/maids")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createMaidReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Geeta"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.currentConfig.salaryAmount").value(15000.00))
                .andExpect(jsonPath("$.currentConfig.salaryMode").value("MONTHLY"))
                .andReturn();

        MaidDetailDto createdMaid = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), MaidDetailDto.class
        );
        assertThat(createdMaid.getId()).isNotNull();

        // 2. Owner A lists maids
        mockMvc.perform(get("/api/v1/maids")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Geeta"))
                .andExpect(jsonPath("$[0].activeSalaryAmount").value(15000.00));

        // 3. Owner A gets maid detail
        mockMvc.perform(get("/api/v1/maids/" + createdMaid.getId())
                        .header("Authorization", "Bearer " + tokenOwnerA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Geeta"))
                .andExpect(jsonPath("$.currentConfig").isNotEmpty());

        // 4. Owner A updates maid
        UpdateMaidRequest updateReq = new UpdateMaidRequest("Geeta Devi", "+919876543211", null, null, "Updated notes", null);
        mockMvc.perform(patch("/api/v1/maids/" + createdMaid.getId())
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Geeta Devi"))
                .andExpect(jsonPath("$.phone").value("+919876543211"));

        // 5. Owner A creates new versioned employment config
        CreateEmploymentConfigRequest newConfigReq = new CreateEmploymentConfigRequest(
                LocalDate.of(2026, 6, 1), null,
                SalaryMode.MONTHLY, new BigDecimal("18000.00"),
                480, 450, true, new BigDecimal("1.500"),
                Set.of((short) 1, (short) 2, (short) 3, (short) 4, (short) 5)
        );

        mockMvc.perform(post("/api/v1/maids/" + createdMaid.getId() + "/employment-configs")
                        .header("Authorization", "Bearer " + tokenOwnerA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newConfigReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.salaryAmount").value(18000.00))
                .andExpect(jsonPath("$.overtimeMultiplier").value(1.5));

        // 6. Check employment config history
        MvcResult historyResult = mockMvc.perform(get("/api/v1/maids/" + createdMaid.getId() + "/employment-configs")
                        .header("Authorization", "Bearer " + tokenOwnerA))
                .andExpect(status().isOk())
                .andReturn();

        List<EmploymentConfigDto> history = objectMapper.readValue(
                historyResult.getResponse().getContentAsString(), new TypeReference<List<EmploymentConfigDto>>() {}
        );
        assertThat(history).hasSize(2);

        // 7. Owner A archives maid
        mockMvc.perform(post("/api/v1/maids/" + createdMaid.getId() + "/archive")
                        .header("Authorization", "Bearer " + tokenOwnerA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.leavingDate").isNotEmpty());

        // 8. STRICT CROSS-TENANT ISOLATION: Owner B cannot access Owner A's maid
        mockMvc.perform(get("/api/v1/maids")
                        .header("Authorization", "Bearer " + tokenOwnerB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/v1/maids/" + createdMaid.getId())
                        .header("Authorization", "Bearer " + tokenOwnerB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        mockMvc.perform(patch("/api/v1/maids/" + createdMaid.getId())
                        .header("Authorization", "Bearer " + tokenOwnerB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        mockMvc.perform(post("/api/v1/maids/" + createdMaid.getId() + "/archive")
                        .header("Authorization", "Bearer " + tokenOwnerB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        mockMvc.perform(post("/api/v1/maids/" + createdMaid.getId() + "/employment-configs")
                        .header("Authorization", "Bearer " + tokenOwnerB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newConfigReq)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }
}
