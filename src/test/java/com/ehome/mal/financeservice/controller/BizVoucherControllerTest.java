package com.ehome.mal.financeservice.controller;

import com.ehome.mal.financeservice.entity.BizVoucher;
import com.ehome.mal.financeservice.fixture.VoucherTestDataFactory;
import com.ehome.mal.financeservice.service.BizVoucherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BizVoucherController.class)
@DisplayName("凭证管理Controller测试")
class BizVoucherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BizVoucherService voucherService;

    private BizVoucher testVoucher;

    @BeforeEach
    void setUp() {
        testVoucher = VoucherTestDataFactory.createDefaultVoucher();
    }

    @Nested
    @DisplayName("新增凭证测试")
    class SaveVoucherTests {

        @Test
        @DisplayName("POST /voucher - 成功创建凭证")
        void saveVoucher_Success() throws Exception {
            when(voucherService.saveVoucher(any(BizVoucher.class))).thenReturn(true);

            mockMvc.perform(post("/voucher")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testVoucher)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").value(true));

            verify(voucherService).saveVoucher(any(BizVoucher.class));
        }
    }

    @Nested
    @DisplayName("审核凭证测试")
    class ReviewVoucherTests {

        @Test
        @DisplayName("POST /voucher/review/{voucherId} - 成功审核凭证")
        void reviewVoucher_Success() throws Exception {
            when(voucherService.reviewVoucher(1L, "auditor-001")).thenReturn(true);

            mockMvc.perform(post("/voucher/review/{voucherId}", 1L)
                            .param("reviewer", "auditor-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").value(true));

            verify(voucherService).reviewVoucher(1L, "auditor-001");
        }

        @Test
        @DisplayName("POST /voucher/review/{voucherId} - 审核不存在的凭证")
        void reviewVoucher_NotFound() throws Exception {
            when(voucherService.reviewVoucher(999L, "auditor-001")).thenReturn(false);

            mockMvc.perform(post("/voucher/review/{voucherId}", 999L)
                            .param("reviewer", "auditor-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(false));
        }
    }

    @Nested
    @DisplayName("反审核凭证测试")
    class UnreviewVoucherTests {

        @Test
        @DisplayName("POST /voucher/unreview/{voucherId} - 成功反审核凭证")
        void unreviewVoucher_Success() throws Exception {
            when(voucherService.unreviewVoucher(1L)).thenReturn(true);

            mockMvc.perform(post("/voucher/unreview/{voucherId}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").value(true));

            verify(voucherService).unreviewVoucher(1L);
        }

        @Test
        @DisplayName("POST /voucher/unreview/{voucherId} - 反审核未审核的凭证")
        void unreviewVoucher_NotAudited() throws Exception {
            when(voucherService.unreviewVoucher(1L)).thenReturn(false);

            mockMvc.perform(post("/voucher/unreview/{voucherId}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(false));
        }
    }

    @Nested
    @DisplayName("生成凭证号测试")
    class GenerateVoucherNoTests {

        @Test
        @DisplayName("GET /voucher/generateNo - 成功生成凭证号")
        void generateVoucherNo_Success() throws Exception {
            when(voucherService.generateVoucherNo("tenant-001", "202401", "JZ"))
                    .thenReturn("202401-JZ-0001");

            mockMvc.perform(get("/voucher/generateNo")
                            .param("tenantId", "tenant-001")
                            .param("periodCode", "202401")
                            .param("voucherType", "JZ"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").value("202401-JZ-0001"));

            verify(voucherService).generateVoucherNo("tenant-001", "202401", "JZ");
        }

        @Test
        @DisplayName("GET /voucher/generateNo - 不同凭证类型独立编号")
        void generateVoucherNo_DifferentTypes() throws Exception {
            when(voucherService.generateVoucherNo("tenant-001", "202401", "SK"))
                    .thenReturn("202401-SK-0001");

            mockMvc.perform(get("/voucher/generateNo")
                            .param("tenantId", "tenant-001")
                            .param("periodCode", "202401")
                            .param("voucherType", "SK"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value("202401-SK-0001"));
        }
    }

    @Nested
    @DisplayName("根据ID查询凭证测试")
    class GetVoucherByIdTests {

        @Test
        @DisplayName("GET /voucher/{id} - 成功查询凭证")
        void getVoucherById_Success() throws Exception {
            testVoucher.setId(1L);
            when(voucherService.getById(1L)).thenReturn(testVoucher);

            mockMvc.perform(get("/voucher/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.voucherNo").value("202401-JZ-0001"));

            verify(voucherService).getById(1L);
        }

        @Test
        @DisplayName("GET /voucher/{id} - 查询不存在的凭证")
        void getVoucherById_NotFound() throws Exception {
            when(voucherService.getById(999L)).thenReturn(null);

            mockMvc.perform(get("/voucher/{id}", 999L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").doesNotExist());
        }
    }

    @Nested
    @DisplayName("查询凭证列表测试")
    class ListVouchersTests {

        @Test
        @DisplayName("GET /voucher/list - 查询凭证列表")
        void listVouchers_Success() throws Exception {
            when(voucherService.list(any())).thenReturn(Collections.singletonList(testVoucher));

            mockMvc.perform(get("/voucher/list")
                            .param("tenantId", "tenant-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("GET /voucher/list - 按期间过滤")
        void listVouchers_ByPeriod() throws Exception {
            when(voucherService.list(any())).thenReturn(Collections.singletonList(testVoucher));

            mockMvc.perform(get("/voucher/list")
                            .param("tenantId", "tenant-001")
                            .param("periodCode", "202401"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }
}
