package com.ehome.mal.financeservice.controller;

import com.ehome.mal.financeservice.entity.BizFixedAsset;
import com.ehome.mal.financeservice.fixture.FixedAssetTestDataFactory;
import com.ehome.mal.financeservice.service.BizFixedAssetService;
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

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BizFixedAssetController.class)
@DisplayName("固定资产Controller测试")
class BizFixedAssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BizFixedAssetService fixedAssetService;

    private BizFixedAsset testAsset;

    @BeforeEach
    void setUp() {
        testAsset = FixedAssetTestDataFactory.createDefaultAsset();
    }

    @Nested
    @DisplayName("新增固定资产测试")
    class SaveAssetTests {

        @Test
        @DisplayName("POST /fixed-asset - 成功登记资产")
        void saveAsset_Success() throws Exception {
            when(fixedAssetService.saveAsset(any(BizFixedAsset.class))).thenReturn(true);

            mockMvc.perform(post("/fixed-asset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testAsset)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").value(true));

            verify(fixedAssetService).saveAsset(any(BizFixedAsset.class));
        }

        @Test
        @DisplayName("POST /fixed-asset - 登记电子设备")
        void saveAsset_Electronic() throws Exception {
            BizFixedAsset electronicAsset = FixedAssetTestDataFactory.createElectronicAsset();
            when(fixedAssetService.saveAsset(any(BizFixedAsset.class))).thenReturn(true);

            mockMvc.perform(post("/fixed-asset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(electronicAsset)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(true));
        }
    }

    @Nested
    @DisplayName("更新固定资产测试")
    class UpdateAssetTests {

        @Test
        @DisplayName("PUT /fixed-asset - 成功更新资产")
        void updateAsset_Success() throws Exception {
            testAsset.setId(1L);
            when(fixedAssetService.updateAsset(any(BizFixedAsset.class))).thenReturn(true);

            mockMvc.perform(put("/fixed-asset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testAsset)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").value(true));

            verify(fixedAssetService).updateAsset(any(BizFixedAsset.class));
        }
    }

    @Nested
    @DisplayName("计算折旧测试")
    class CalculateDepreciationTests {

        @Test
        @DisplayName("POST /fixed-asset/calculateDepreciation - 成功计算折旧")
        void calculateDepreciation_Success() throws Exception {
            BigDecimal depreciation = new BigDecimal("900.00");
            when(fixedAssetService.calculateDepreciation(any(BizFixedAsset.class)))
                    .thenReturn(depreciation);

            mockMvc.perform(post("/fixed-asset/calculateDepreciation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testAsset)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").value(900.00));

            verify(fixedAssetService).calculateDepreciation(any(BizFixedAsset.class));
        }

        @Test
        @DisplayName("POST /fixed-asset/calculateDepreciation - 直线法折旧")
        void calculateDepreciation_StraightLine() throws Exception {
            BizFixedAsset asset = FixedAssetTestDataFactory.createStraightLineAsset();
            when(fixedAssetService.calculateDepreciation(any(BizFixedAsset.class)))
                    .thenReturn(new BigDecimal("900.00"));

            mockMvc.perform(post("/fixed-asset/calculateDepreciation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(asset)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(900.00));
        }

        @Test
        @DisplayName("POST /fixed-asset/calculateDepreciation - 双倍余额递减法折旧")
        void calculateDepreciation_DoubleDeclining() throws Exception {
            BizFixedAsset asset = FixedAssetTestDataFactory.createDoubleDecliningAsset();
            BigDecimal expectedDepreciation = asset.getNetValue()
                    .multiply(new BigDecimal("2"))
                    .divide(new BigDecimal(asset.getUsefulLife()), 2, java.math.RoundingMode.HALF_UP);
            when(fixedAssetService.calculateDepreciation(any(BizFixedAsset.class)))
                    .thenReturn(expectedDepreciation);

            mockMvc.perform(post("/fixed-asset/calculateDepreciation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(asset)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isNumber());
        }
    }

    @Nested
    @DisplayName("根据ID查询固定资产测试")
    class GetAssetByIdTests {

        @Test
        @DisplayName("GET /fixed-asset/{id} - 成功查询资产")
        void getAssetById_Success() throws Exception {
            testAsset.setId(1L);
            when(fixedAssetService.getById(1L)).thenReturn(testAsset);

            mockMvc.perform(get("/fixed-asset/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.assetCode").value("FA-001"));

            verify(fixedAssetService).getById(1L);
        }

        @Test
        @DisplayName("GET /fixed-asset/{id} - 查询不存在的资产")
        void getAssetById_NotFound() throws Exception {
            when(fixedAssetService.getById(999L)).thenReturn(null);

            mockMvc.perform(get("/fixed-asset/{id}", 999L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").doesNotExist());
        }
    }

    @Nested
    @DisplayName("查询固定资产列表测试")
    class ListAssetsTests {

        @Test
        @DisplayName("GET /fixed-asset/list - 查询资产列表")
        void listAssets_Success() throws Exception {
            when(fixedAssetService.list(any())).thenReturn(Collections.singletonList(testAsset));

            mockMvc.perform(get("/fixed-asset/list")
                            .param("tenantId", "tenant-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").isArray());

            verify(fixedAssetService).list(any());
        }

        @Test
        @DisplayName("GET /fixed-asset/list - 空列表")
        void listAssets_Empty() throws Exception {
            when(fixedAssetService.list(any())).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/fixed-asset/list")
                            .param("tenantId", "tenant-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }
}
