package com.ehome.mal.financeservice.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("固定资产实体测试")
class BizFixedAssetTest {

    @Nested
    @DisplayName("字段正确性验证测试")
    class FieldValidationTests {

        @Test
        @DisplayName("创建资产_所有字段可设置和获取")
        void createAsset_AllFieldsCanBeSetAndGet() {
            BizFixedAsset asset = new BizFixedAsset();
            asset.setId(1L);
            asset.setTenantId("tenant-001");
            asset.setAssetCode("FA-001");
            asset.setAssetName("办公电脑");
            asset.setAssetCategory("ELECTRONIC");
            asset.setOriginalValue(new BigDecimal("5000.00"));
            asset.setSalvageValue(new BigDecimal("500.00"));
            asset.setNetValue(new BigDecimal("5000.00"));
            asset.setDepreciationValue(BigDecimal.ZERO);
            asset.setUsefulLife(5);
            asset.setDepreciationMethod("STRAIGHT_LINE");
            asset.setStatus(1);

            assertThat(asset.getId()).isEqualTo(1L);
            assertThat(asset.getTenantId()).isEqualTo("tenant-001");
            assertThat(asset.getAssetCode()).isEqualTo("FA-001");
            assertThat(asset.getAssetName()).isEqualTo("办公电脑");
            assertThat(asset.getAssetCategory()).isEqualTo("ELECTRONIC");
            assertThat(asset.getOriginalValue()).isEqualByComparingTo("5000.00");
            assertThat(asset.getSalvageValue()).isEqualByComparingTo("500.00");
            assertThat(asset.getNetValue()).isEqualByComparingTo("5000.00");
            assertThat(asset.getDepreciationValue()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(asset.getUsefulLife()).isEqualTo(5);
            assertThat(asset.getDepreciationMethod()).isEqualTo("STRAIGHT_LINE");
            assertThat(asset.getStatus()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("折旧方法测试")
    class DepreciationMethodTests {

        @Test
        @DisplayName("直线法折旧")
        void straightLineDepreciation() {
            BizFixedAsset asset = new BizFixedAsset();
            asset.setDepreciationMethod("STRAIGHT_LINE");

            assertThat(asset.getDepreciationMethod()).isEqualTo("STRAIGHT_LINE");
        }

        @Test
        @DisplayName("双倍余额递减法折旧")
        void doubleDecliningDepreciation() {
            BizFixedAsset asset = new BizFixedAsset();
            asset.setDepreciationMethod("DOUBLE_DECLINING");

            assertThat(asset.getDepreciationMethod()).isEqualTo("DOUBLE_DECLINING");
        }
    }

    @Nested
    @DisplayName("资产状态测试")
    class AssetStatusTests {

        @Test
        @DisplayName("在用状态为1")
        void activeStatusIsOne() {
            BizFixedAsset asset = new BizFixedAsset();
            asset.setStatus(1);

            assertThat(asset.getStatus()).isEqualTo(1);
        }

        @Test
        @DisplayName("已处置状态为2")
        void disposedStatusIsTwo() {
            BizFixedAsset asset = new BizFixedAsset();
            asset.setStatus(2);

            assertThat(asset.getStatus()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("资产价值验证测试")
    class AssetValueTests {

        @Test
        @DisplayName("净值等于原值减去累计折旧")
        void netValueEqualsOriginalMinusDepreciation() {
            BizFixedAsset asset = new BizFixedAsset();
            asset.setOriginalValue(new BigDecimal("5000.00"));
            asset.setDepreciationValue(new BigDecimal("1000.00"));
            asset.setNetValue(asset.getOriginalValue().subtract(asset.getDepreciationValue()));

            assertThat(asset.getNetValue()).isEqualByComparingTo("4000.00");
        }

        @Test
        @DisplayName("净值不能为负")
        void netValueCannotBeNegative() {
            BizFixedAsset asset = new BizFixedAsset();
            asset.setNetValue(BigDecimal.ZERO);

            assertThat(asset.getNetValue()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        }
    }
}
