package com.ehome.mal.financeservice.fixture;

import com.ehome.mal.financeservice.entity.BizFixedAsset;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class FixedAssetTestDataFactory {

    private static final String DEFAULT_TENANT_ID = "tenant-001";

    private FixedAssetTestDataFactory() {
    }

    public static BizFixedAsset createDefaultAsset() {
        BizFixedAsset asset = new BizFixedAsset();
        asset.setTenantId(DEFAULT_TENANT_ID);
        asset.setAssetCode("FA-001");
        asset.setAssetName("办公电脑");
        asset.setAssetCategory("ELECTRONIC");
        asset.setOriginalValue(new BigDecimal("5000.00"));
        asset.setSalvageValue(new BigDecimal("500.00"));
        asset.setNetValue(new BigDecimal("5000.00"));
        asset.setDepreciationValue(BigDecimal.ZERO);
        asset.setUsefulLife(5);
        asset.setDepreciationMethod("STRAIGHT_LINE");
        asset.setAcquisitionDate(LocalDateTime.of(2024, 1, 1, 0, 0));
        asset.setDepreciationStartDate(LocalDateTime.of(2024, 2, 1, 0, 0));
        asset.setStatus(1);
        asset.setRemark("测试固定资产");
        asset.setDelFlag(0);
        return asset;
    }

    public static BizFixedAsset createAssetWithMethod(String depreciationMethod) {
        BizFixedAsset asset = createDefaultAsset();
        asset.setDepreciationMethod(depreciationMethod);
        return asset;
    }

    public static BizFixedAsset createStraightLineAsset() {
        return createAssetWithMethod("STRAIGHT_LINE");
    }

    public static BizFixedAsset createDoubleDecliningAsset() {
        BizFixedAsset asset = createDefaultAsset();
        asset.setDepreciationMethod("DOUBLE_DECLINING");
        asset.setUsefulLife(10);
        return asset;
    }

    public static BizFixedAsset createSumOfYearsDigitsAsset() {
        BizFixedAsset asset = createDefaultAsset();
        asset.setDepreciationMethod("SUM_OF_YEARS_DIGITS");
        return asset;
    }

    public static BizFixedAsset createAssetWithValue(BigDecimal originalValue) {
        BizFixedAsset asset = createDefaultAsset();
        asset.setOriginalValue(originalValue);
        asset.setNetValue(originalValue);
        return asset;
    }

    public static BizFixedAsset createAssetWithLife(int usefulLife) {
        BizFixedAsset asset = createDefaultAsset();
        asset.setUsefulLife(usefulLife);
        return asset;
    }

    public static BizFixedAsset createDepreciatedAsset(BigDecimal depreciationValue) {
        BizFixedAsset asset = createDefaultAsset();
        asset.setDepreciationValue(depreciationValue);
        asset.setNetValue(asset.getOriginalValue().subtract(depreciationValue));
        return asset;
    }

    public static BizFixedAsset createDisposedAsset() {
        BizFixedAsset asset = createDefaultAsset();
        asset.setStatus(2);
        return asset;
    }

    public static BizFixedAsset createElectronicAsset() {
        BizFixedAsset asset = createDefaultAsset();
        asset.setAssetCategory("ELECTRONIC");
        asset.setUsefulLife(3);
        return asset;
    }

    public static BizFixedAsset createVehicleAsset() {
        BizFixedAsset asset = createDefaultAsset();
        asset.setAssetCode("FA-002");
        asset.setAssetName("运输车辆");
        asset.setAssetCategory("VEHICLE");
        asset.setOriginalValue(new BigDecimal("200000.00"));
        asset.setSalvageValue(new BigDecimal("20000.00"));
        asset.setNetValue(new BigDecimal("200000.00"));
        asset.setUsefulLife(10);
        return asset;
    }

    public static BizFixedAsset createBuildingAsset() {
        BizFixedAsset asset = createDefaultAsset();
        asset.setAssetCode("FA-003");
        asset.setAssetName("办公楼");
        asset.setAssetCategory("BUILDING");
        asset.setOriginalValue(new BigDecimal("1000000.00"));
        asset.setSalvageValue(new BigDecimal("100000.00"));
        asset.setNetValue(new BigDecimal("1000000.00"));
        asset.setUsefulLife(20);
        return asset;
    }

    public static String getDefaultTenantId() {
        return DEFAULT_TENANT_ID;
    }
}
