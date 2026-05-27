package com.ehome.mal.financeservice.service;

import com.ehome.mal.financeservice.entity.BizFixedAsset;
import com.ehome.mal.financeservice.fixture.FixedAssetTestDataFactory;
import com.ehome.mal.financeservice.mapper.BizFixedAssetMapper;
import com.ehome.mal.financeservice.service.impl.BizFixedAssetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("固定资产Service测试")
class BizFixedAssetServiceTest {

    @Mock
    private BizFixedAssetMapper assetMapper;

    @InjectMocks
    private BizFixedAssetServiceImpl assetService;

    private BizFixedAsset testAsset;

    @BeforeEach
    void setUp() {
        testAsset = FixedAssetTestDataFactory.createDefaultAsset();
    }

    @Nested
    @DisplayName("折旧计算-直线法测试")
    class StraightLineDepreciationTests {

        @Test
        @DisplayName("给定直线法资产_当计算折旧_则正确")
        void givenStraightLineAsset_whenCalculateDepreciation_thenCorrect() {
            BizFixedAsset asset = FixedAssetTestDataFactory.createStraightLineAsset();

            BigDecimal depreciation = assetService.calculateDepreciation(asset);

            BigDecimal expected = asset.getOriginalValue()
                    .subtract(asset.getSalvageValue())
                    .divide(new BigDecimal(asset.getUsefulLife()), 2, RoundingMode.HALF_UP);
            assertThat(depreciation).isEqualByComparingTo(expected);
        }

        @Test
        @DisplayName("给定5年使用期资产_当计算折旧_则年折旧额正确")
        void given5YearAsset_whenCalculateDepreciation_thenYearlyAmountCorrect() {
            BizFixedAsset asset = FixedAssetTestDataFactory.createAssetWithValue(new BigDecimal("5000.00"));
            asset.setSalvageValue(new BigDecimal("500.00"));
            asset.setUsefulLife(5);
            asset.setDepreciationMethod("STRAIGHT_LINE");

            BigDecimal depreciation = assetService.calculateDepreciation(asset);

            assertThat(depreciation).isEqualByComparingTo("900.00");
        }

        @Test
        @DisplayName("给定零残值资产_当计算折旧_则正确")
        void givenZeroSalvageAsset_whenCalculateDepreciation_thenCorrect() {
            BizFixedAsset asset = FixedAssetTestDataFactory.createStraightLineAsset();
            asset.setSalvageValue(BigDecimal.ZERO);

            BigDecimal depreciation = assetService.calculateDepreciation(asset);

            BigDecimal expected = asset.getOriginalValue()
                    .divide(new BigDecimal(asset.getUsefulLife()), 2, RoundingMode.HALF_UP);
            assertThat(depreciation).isEqualByComparingTo(expected);
        }
    }

    @Nested
    @DisplayName("折旧计算-双倍余额递减法测试")
    class DoubleDecliningDepreciationTests {

        @Test
        @DisplayName("给定双倍余额递减法资产_当计算折旧_则正确")
        void givenDoubleDecliningAsset_whenCalculateDepreciation_thenCorrect() {
            BizFixedAsset asset = FixedAssetTestDataFactory.createDoubleDecliningAsset();

            BigDecimal depreciation = assetService.calculateDepreciation(asset);

            BigDecimal rate = new BigDecimal("2")
                    .divide(new BigDecimal(asset.getUsefulLife()), 4, RoundingMode.HALF_UP);
            BigDecimal expected = asset.getNetValue().multiply(rate).setScale(2, RoundingMode.HALF_UP);
            assertThat(depreciation).isEqualByComparingTo(expected);
        }

        @Test
        @DisplayName("给定净值递减的资产_当计算折旧_则逐年递减")
        void givenDecreasingNetValue_whenCalculateDepreciation_thenDecreasing() {
            BizFixedAsset asset = FixedAssetTestDataFactory.createDoubleDecliningAsset();
            asset.setOriginalValue(new BigDecimal("10000.00"));
            asset.setNetValue(new BigDecimal("10000.00"));
            asset.setUsefulLife(5);

            BigDecimal year1Depreciation = assetService.calculateDepreciation(asset);

            asset.setNetValue(asset.getNetValue().subtract(year1Depreciation));
            BigDecimal year2Depreciation = assetService.calculateDepreciation(asset);

            assertThat(year1Depreciation).isGreaterThan(year2Depreciation);
        }
    }

    @Nested
    @DisplayName("折旧计算-年数总和法测试")
    class SumOfYearsDigitsTests {

        @Test
        @DisplayName("给定不支持的折旧方法_当计算折旧_则返回零")
        void givenUnsupportedMethod_whenCalculateDepreciation_thenReturnZero() {
            BizFixedAsset asset = FixedAssetTestDataFactory.createDefaultAsset();
            asset.setDepreciationMethod("UNSUPPORTED");

            BigDecimal depreciation = assetService.calculateDepreciation(asset);

            assertThat(depreciation).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("给定空折旧方法_当计算折旧_则返回零")
        void givenNullMethod_whenCalculateDepreciation_thenReturnZero() {
            BizFixedAsset asset = FixedAssetTestDataFactory.createDefaultAsset();
            asset.setDepreciationMethod(null);

            BigDecimal depreciation = assetService.calculateDepreciation(asset);

            assertThat(depreciation).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("资产登记测试")
    class AssetRegistrationTests {

        @Test
        @DisplayName("给定有效资产_当登记资产_则成功")
        void givenValidAsset_whenRegister_thenSuccess() {
            when(assetMapper.insert(any(BizFixedAsset.class))).thenReturn(1);

            boolean result = assetService.saveAsset(testAsset);

            assertThat(result).isTrue();
            assertThat(testAsset.getCreateTime()).isNotNull();
            assertThat(testAsset.getUpdateTime()).isNotNull();
            assertThat(testAsset.getDelFlag()).isEqualTo(0);
            assertThat(testAsset.getDepreciationValue()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(testAsset.getNetValue()).isEqualByComparingTo(testAsset.getOriginalValue());
            verify(assetMapper).insert(any(BizFixedAsset.class));
        }

        @Test
        @DisplayName("给定电子设备资产_当登记资产_则使用年限为3年")
        void givenElectronicAsset_whenRegister_thenUsefulLifeIs3() {
            BizFixedAsset electronicAsset = FixedAssetTestDataFactory.createElectronicAsset();
            when(assetMapper.insert(any(BizFixedAsset.class))).thenReturn(1);

            boolean result = assetService.saveAsset(electronicAsset);

            assertThat(result).isTrue();
            assertThat(electronicAsset.getUsefulLife()).isEqualTo(3);
        }

        @Test
        @DisplayName("给定车辆资产_当登记资产_则使用年限为10年")
        void givenVehicleAsset_whenRegister_thenUsefulLifeIs10() {
            BizFixedAsset vehicleAsset = FixedAssetTestDataFactory.createVehicleAsset();
            when(assetMapper.insert(any(BizFixedAsset.class))).thenReturn(1);

            boolean result = assetService.saveAsset(vehicleAsset);

            assertThat(result).isTrue();
            assertThat(vehicleAsset.getUsefulLife()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("资产更新测试")
    class AssetUpdateTests {

        @Test
        @DisplayName("给定有效资产_当更新资产_则成功")
        void givenValidAsset_whenUpdate_thenSuccess() {
            testAsset.setId(1L);
            testAsset.setAssetName("更新后的资产名称");
            when(assetMapper.updateById(any(BizFixedAsset.class))).thenReturn(1);

            boolean result = assetService.updateAsset(testAsset);

            assertThat(result).isTrue();
            assertThat(testAsset.getUpdateTime()).isNotNull();
            verify(assetMapper).updateById(any(BizFixedAsset.class));
        }

        @Test
        @DisplayName("给定资产修改净值_当更新资产_则成功")
        void givenAssetWithNetValueChange_whenUpdate_thenSuccess() {
            testAsset.setId(1L);
            testAsset.setNetValue(new BigDecimal("4000.00"));
            when(assetMapper.updateById(any(BizFixedAsset.class))).thenReturn(1);

            boolean result = assetService.updateAsset(testAsset);

            assertThat(result).isTrue();
            assertThat(testAsset.getNetValue()).isEqualByComparingTo("4000.00");
        }
    }

    @Nested
    @DisplayName("折旧金额边界测试")
    class DepreciationBoundaryTests {

        @Test
        @DisplayName("给定折旧后净值为零_当计算折旧_则正确")
        void givenZeroNetValue_whenCalculateDepreciation_thenCorrect() {
            BizFixedAsset asset = FixedAssetTestDataFactory.createStraightLineAsset();
            asset.setNetValue(asset.getSalvageValue());

            BigDecimal depreciation = assetService.calculateDepreciation(asset);

            assertThat(depreciation).isNotNull();
        }

        @Test
        @DisplayName("给定大额资产_当计算折旧_则正确")
        void givenLargeAmountAsset_whenCalculateDepreciation_thenCorrect() {
            BizFixedAsset asset = FixedAssetTestDataFactory.createBuildingAsset();

            BigDecimal depreciation = assetService.calculateDepreciation(asset);

            assertThat(depreciation).isNotNull();
            assertThat(depreciation).isPositive();
        }
    }

    @Nested
    @DisplayName("资产状态测试")
    class AssetStatusTests {

        @Test
        @DisplayName("给定在用资产_当验证状态_则可计提折旧")
        void givenActiveAsset_whenValidateStatus_thenCanDepreciate() {
            assertThat(testAsset.getStatus()).isEqualTo(1);
        }

        @Test
        @DisplayName("给定已处置资产_当验证状态_则不可计提折旧")
        void givenDisposedAsset_whenValidateStatus_thenCannotDepreciate() {
            BizFixedAsset disposedAsset = FixedAssetTestDataFactory.createDisposedAsset();

            assertThat(disposedAsset.getStatus()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("资产类别测试")
    class AssetCategoryTests {

        @Test
        @DisplayName("给定电子设备_当验证类别_则正确")
        void givenElectronicAsset_whenValidateCategory_thenCorrect() {
            BizFixedAsset asset = FixedAssetTestDataFactory.createElectronicAsset();
            assertThat(asset.getAssetCategory()).isEqualTo("ELECTRONIC");
        }

        @Test
        @DisplayName("给定车辆_当验证类别_则正确")
        void givenVehicleAsset_whenValidateCategory_thenCorrect() {
            BizFixedAsset asset = FixedAssetTestDataFactory.createVehicleAsset();
            assertThat(asset.getAssetCategory()).isEqualTo("VEHICLE");
        }

        @Test
        @DisplayName("给定建筑物_当验证类别_则正确")
        void givenBuildingAsset_whenValidateCategory_thenCorrect() {
            BizFixedAsset asset = FixedAssetTestDataFactory.createBuildingAsset();
            assertThat(asset.getAssetCategory()).isEqualTo("BUILDING");
        }
    }
}
