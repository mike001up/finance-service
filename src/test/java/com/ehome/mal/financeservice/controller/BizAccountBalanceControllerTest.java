package com.ehome.mal.financeservice.controller;

import com.ehome.mal.financeservice.entity.BizAccountBalance;
import com.ehome.mal.financeservice.service.BizAccountBalanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BizAccountBalanceController.class)
@DisplayName("科目余额Controller测试")
class BizAccountBalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BizAccountBalanceService accountBalanceService;

    private BizAccountBalance testBalance;

    @BeforeEach
    void setUp() {
        testBalance = createDefaultBalance();
    }

    private BizAccountBalance createDefaultBalance() {
        BizAccountBalance balance = new BizAccountBalance();
        balance.setId(1L);
        balance.setTenantId("tenant-001");
        balance.setAccountId(1L);
        balance.setPeriodCode("202401");
        balance.setPeriodDebit(new BigDecimal("1000.00"));
        balance.setPeriodCredit(BigDecimal.ZERO);
        balance.setYearDebit(new BigDecimal("1000.00"));
        balance.setYearCredit(BigDecimal.ZERO);
        balance.setEndingBalance(new BigDecimal("1000.00"));
        return balance;
    }

    @Nested
    @DisplayName("查询科目余额测试")
    class GetBalancesByPeriodTests {

        @Test
        @DisplayName("GET /account-balance/list - 成功查询余额")
        void getBalancesByPeriod_Success() throws Exception {
            when(accountBalanceService.getBalancesByPeriod("tenant-001", "202401"))
                    .thenReturn(Collections.singletonList(testBalance));

            mockMvc.perform(get("/account-balance/list")
                            .param("tenantId", "tenant-001")
                            .param("periodCode", "202401"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1));

            verify(accountBalanceService).getBalancesByPeriod("tenant-001", "202401");
        }

        @Test
        @DisplayName("GET /account-balance/list - 空列表")
        void getBalancesByPeriod_Empty() throws Exception {
            when(accountBalanceService.getBalancesByPeriod(anyString(), anyString()))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/account-balance/list")
                            .param("tenantId", "tenant-001")
                            .param("periodCode", "202401"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    @Nested
    @DisplayName("试算平衡测试")
    class CalculateTrialBalanceTests {

        @Test
        @DisplayName("GET /account-balance/trialBalance - 平衡")
        void calculateTrialBalance_Balanced() throws Exception {
            when(accountBalanceService.calculateTrialBalance("tenant-001", "202401"))
                    .thenReturn(true);

            mockMvc.perform(get("/account-balance/trialBalance")
                            .param("tenantId", "tenant-001")
                            .param("periodCode", "202401"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").value(true));

            verify(accountBalanceService).calculateTrialBalance("tenant-001", "202401");
        }

        @Test
        @DisplayName("GET /account-balance/trialBalance - 不平衡")
        void calculateTrialBalance_Unbalanced() throws Exception {
            when(accountBalanceService.calculateTrialBalance("tenant-001", "202401"))
                    .thenReturn(false);

            mockMvc.perform(get("/account-balance/trialBalance")
                            .param("tenantId", "tenant-001")
                            .param("periodCode", "202401"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(false));
        }
    }
}
