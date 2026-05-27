package com.ehome.mal.financeservice.controller;

import com.ehome.mal.financeservice.service.SystemConfigService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SystemConfigController.class)
@DisplayName("系统配置Controller测试")
class SystemConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SystemConfigService systemConfigService;

    @Nested
    @DisplayName("获取配置值测试")
    class GetConfigValueTests {

        @Test
        @DisplayName("GET /system-config/value - 成功获取配置值")
        void getConfigValue_Success() throws Exception {
            when(systemConfigService.getConfigValue("tenant-001", "DEFAULT_VOUCHER_TYPE"))
                    .thenReturn("JZ");

            mockMvc.perform(get("/system-config/value")
                            .param("tenantId", "tenant-001")
                            .param("configKey", "DEFAULT_VOUCHER_TYPE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").value("JZ"));

            verify(systemConfigService).getConfigValue("tenant-001", "DEFAULT_VOUCHER_TYPE");
        }

        @Test
        @DisplayName("GET /system-config/value - 配置不存在")
        void getConfigValue_NotFound() throws Exception {
            when(systemConfigService.getConfigValue(anyString(), anyString()))
                    .thenReturn(null);

            mockMvc.perform(get("/system-config/value")
                            .param("tenantId", "tenant-001")
                            .param("configKey", "NON_EXISTENT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").doesNotExist());
        }
    }

    @Nested
    @DisplayName("设置配置值测试")
    class SetConfigValueTests {

        @Test
        @DisplayName("POST /system-config/value - 成功设置配置值")
        void setConfigValue_Success() throws Exception {
            when(systemConfigService.setConfigValue("tenant-001", "DEFAULT_VOUCHER_TYPE", "SK"))
                    .thenReturn(true);

            mockMvc.perform(post("/system-config/value")
                            .param("tenantId", "tenant-001")
                            .param("configKey", "DEFAULT_VOUCHER_TYPE")
                            .param("configValue", "SK"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").value(true));

            verify(systemConfigService).setConfigValue("tenant-001", "DEFAULT_VOUCHER_TYPE", "SK");
        }

        @Test
        @DisplayName("POST /system-config/value - 创建新配置")
        void setConfigValue_CreateNew() throws Exception {
            when(systemConfigService.setConfigValue(anyString(), anyString(), anyString()))
                    .thenReturn(true);

            mockMvc.perform(post("/system-config/value")
                            .param("tenantId", "tenant-001")
                            .param("configKey", "NEW_CONFIG")
                            .param("configValue", "VALUE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("POST /system-config/value - 设置空值")
        void setConfigValue_EmptyValue() throws Exception {
            when(systemConfigService.setConfigValue(anyString(), anyString(), anyString()))
                    .thenReturn(true);

            mockMvc.perform(post("/system-config/value")
                            .param("tenantId", "tenant-001")
                            .param("configKey", "DEFAULT_VOUCHER_TYPE")
                            .param("configValue", ""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(true));
        }
    }
}
