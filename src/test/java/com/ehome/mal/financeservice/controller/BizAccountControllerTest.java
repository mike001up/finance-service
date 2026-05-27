package com.ehome.mal.financeservice.controller;

import com.ehome.mal.financeservice.entity.BizAccount;
import com.ehome.mal.financeservice.fixture.AccountTestDataFactory;
import com.ehome.mal.financeservice.service.BizAccountService;
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

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BizAccountController.class)
@DisplayName("科目管理Controller测试")
class BizAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BizAccountService accountService;

    private BizAccount testAccount;

    @BeforeEach
    void setUp() {
        testAccount = AccountTestDataFactory.createDefaultAccount();
    }

    @Nested
    @DisplayName("获取科目树测试")
    class GetAccountTreeTests {

        @Test
        @DisplayName("GET /account/tree - 成功获取科目树")
        void getAccountTree_Success() throws Exception {
            when(accountService.getAccountTree(anyString()))
                    .thenReturn(AccountTestDataFactory.createAccountTree());

            mockMvc.perform(get("/account/tree")
                            .param("tenantId", "tenant-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(3));

            verify(accountService).getAccountTree("tenant-001");
        }

        @Test
        @DisplayName("GET /account/tree - 空租户ID")
        void getAccountTree_EmptyTenantId() throws Exception {
            when(accountService.getAccountTree(anyString()))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/account/tree")
                            .param("tenantId", ""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    @Nested
    @DisplayName("新增科目测试")
    class SaveAccountTests {

        @Test
        @DisplayName("POST /account - 成功创建科目")
        void saveAccount_Success() throws Exception {
            when(accountService.saveAccount(any(BizAccount.class))).thenReturn(true);

            mockMvc.perform(post("/account")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testAccount)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").value(true));

            verify(accountService).saveAccount(any(BizAccount.class));
        }

        @Test
        @DisplayName("POST /account - 创建资产类科目")
        void saveAccount_AssetAccount() throws Exception {
            BizAccount assetAccount = AccountTestDataFactory.createAssetAccount();
            when(accountService.saveAccount(any(BizAccount.class))).thenReturn(true);

            mockMvc.perform(post("/account")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(assetAccount)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(true));
        }
    }

    @Nested
    @DisplayName("更新科目测试")
    class UpdateAccountTests {

        @Test
        @DisplayName("PUT /account - 成功更新科目")
        void updateAccount_Success() throws Exception {
            testAccount.setId(1L);
            when(accountService.updateAccount(any(BizAccount.class))).thenReturn(true);

            mockMvc.perform(put("/account")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testAccount)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").value(true));

            verify(accountService).updateAccount(any(BizAccount.class));
        }
    }

    @Nested
    @DisplayName("删除科目测试")
    class DeleteAccountTests {

        @Test
        @DisplayName("DELETE /account/{id} - 成功删除科目")
        void deleteAccount_Success() throws Exception {
            when(accountService.deleteAccount(1L)).thenReturn(true);

            mockMvc.perform(delete("/account/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").value(true));

            verify(accountService).deleteAccount(1L);
        }

        @Test
        @DisplayName("DELETE /account/{id} - 删除不存在的科目")
        void deleteAccount_NotFound() throws Exception {
            when(accountService.deleteAccount(999L)).thenReturn(false);

            mockMvc.perform(delete("/account/{id}", 999L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(false));
        }
    }

    @Nested
    @DisplayName("根据ID查询科目测试")
    class GetAccountByIdTests {

        @Test
        @DisplayName("GET /account/{id} - 成功查询科目")
        void getAccountById_Success() throws Exception {
            testAccount.setId(1L);
            when(accountService.getById(1L)).thenReturn(testAccount);

            mockMvc.perform(get("/account/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.accountCode").value("1001"));

            verify(accountService).getById(1L);
        }

        @Test
        @DisplayName("GET /account/{id} - 查询不存在的科目")
        void getAccountById_NotFound() throws Exception {
            when(accountService.getById(999L)).thenReturn(null);

            mockMvc.perform(get("/account/{id}", 999L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").doesNotExist());
        }
    }
}
