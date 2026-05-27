package com.ehome.mal.financeservice.controller;

import com.ehome.mal.financeservice.entity.BizAccountingPeriod;
import com.ehome.mal.financeservice.fixture.PeriodTestDataFactory;
import com.ehome.mal.financeservice.service.BizAccountingPeriodService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BizAccountingPeriodController.class)
@DisplayName("会计期间Controller测试")
class BizAccountingPeriodControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BizAccountingPeriodService accountingPeriodService;

    private BizAccountingPeriod testPeriod;

    @BeforeEach
    void setUp() {
        testPeriod = PeriodTestDataFactory.createDefaultPeriod();
    }

    @Nested
    @DisplayName("查询会计期间列表测试")
    class GetPeriodsByTenantTests {

        @Test
        @DisplayName("GET /accounting-period/list - 成功查询期间列表")
        void getPeriodsByTenant_Success() throws Exception {
            when(accountingPeriodService.getPeriodsByTenant("tenant-001"))
                    .thenReturn(PeriodTestDataFactory.createCurrentYearPeriods());

            mockMvc.perform(get("/accounting-period/list")
                            .param("tenantId", "tenant-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(12));

            verify(accountingPeriodService).getPeriodsByTenant("tenant-001");
        }

        @Test
        @DisplayName("GET /accounting-period/list - 空列表")
        void getPeriodsByTenant_Empty() throws Exception {
            when(accountingPeriodService.getPeriodsByTenant(anyString()))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/accounting-period/list")
                            .param("tenantId", "tenant-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    @Nested
    @DisplayName("开启会计期间测试")
    class OpenPeriodTests {

        @Test
        @DisplayName("POST /accounting-period/open - 成功开启期间")
        void openPeriod_Success() throws Exception {
            when(accountingPeriodService.openPeriod("202401")).thenReturn(true);

            mockMvc.perform(post("/accounting-period/open")
                            .param("periodCode", "202401"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").value(true));

            verify(accountingPeriodService).openPeriod("202401");
        }

        @Test
        @DisplayName("POST /accounting-period/open - 开启已开启的期间")
        void openPeriod_AlreadyOpen() throws Exception {
            when(accountingPeriodService.openPeriod("202401")).thenReturn(false);

            mockMvc.perform(post("/accounting-period/open")
                            .param("periodCode", "202401"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(false));
        }
    }

    @Nested
    @DisplayName("关闭会计期间测试")
    class ClosePeriodTests {

        @Test
        @DisplayName("POST /accounting-period/close - 成功关闭期间")
        void closePeriod_Success() throws Exception {
            when(accountingPeriodService.closePeriod("202401")).thenReturn(true);

            mockMvc.perform(post("/accounting-period/close")
                            .param("periodCode", "202401"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").value(true));

            verify(accountingPeriodService).closePeriod("202401");
        }

        @Test
        @DisplayName("POST /accounting-period/close - 关闭已关闭的期间")
        void closePeriod_AlreadyClosed() throws Exception {
            when(accountingPeriodService.closePeriod("202401")).thenReturn(false);

            mockMvc.perform(post("/accounting-period/close")
                            .param("periodCode", "202401"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(false));
        }
    }

    @Nested
    @DisplayName("获取当前会计期间测试")
    class GetCurrentPeriodTests {

        @Test
        @DisplayName("GET /accounting-period/current - 成功获取当前期间")
        void getCurrentPeriod_Success() throws Exception {
            when(accountingPeriodService.getCurrentPeriod("tenant-001"))
                    .thenReturn(testPeriod);

            mockMvc.perform(get("/accounting-period/current")
                            .param("tenantId", "tenant-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.periodCode").value("202401"));

            verify(accountingPeriodService).getCurrentPeriod("tenant-001");
        }

        @Test
        @DisplayName("GET /accounting-period/current - 无当前期间")
        void getCurrentPeriod_NotFound() throws Exception {
            when(accountingPeriodService.getCurrentPeriod(anyString()))
                    .thenReturn(null);

            mockMvc.perform(get("/accounting-period/current")
                            .param("tenantId", "tenant-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").doesNotExist());
        }
    }
}
