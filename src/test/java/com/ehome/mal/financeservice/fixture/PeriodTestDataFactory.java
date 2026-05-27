package com.ehome.mal.financeservice.fixture;

import com.ehome.mal.financeservice.entity.BizAccountingPeriod;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class PeriodTestDataFactory {

    private static final String DEFAULT_TENANT_ID = "tenant-001";

    private PeriodTestDataFactory() {
    }

    public static BizAccountingPeriod createDefaultPeriod() {
        BizAccountingPeriod period = new BizAccountingPeriod();
        period.setTenantId(DEFAULT_TENANT_ID);
        period.setPeriodCode("202401");
        period.setYear(2024);
        period.setMonth(1);
        period.setStartDate(LocalDateTime.of(2024, 1, 1, 0, 0));
        period.setEndDate(LocalDateTime.of(2024, 1, 31, 23, 59, 59));
        period.setStatus(1);
        period.setRemark("2024年1月会计期间");
        period.setDelFlag(0);
        return period;
    }

    public static BizAccountingPeriod createPeriodByYearMonth(int year, int month) {
        BizAccountingPeriod period = new BizAccountingPeriod();
        period.setTenantId(DEFAULT_TENANT_ID);
        period.setPeriodCode(String.format("%04d%02d", year, month));
        period.setYear(year);
        period.setMonth(month);
        period.setStartDate(LocalDateTime.of(year, month, 1, 0, 0));
        period.setEndDate(LocalDateTime.of(year, month, 1, 0, 0).plusMonths(1).minusSeconds(1));
        period.setStatus(0);
        period.setDelFlag(0);
        return period;
    }

    public static BizAccountingPeriod createOpenPeriod() {
        BizAccountingPeriod period = createDefaultPeriod();
        period.setStatus(1);
        return period;
    }

    public static BizAccountingPeriod createClosedPeriod() {
        BizAccountingPeriod period = createDefaultPeriod();
        period.setStatus(2);
        return period;
    }

    public static BizAccountingPeriod createUnopenPeriod() {
        BizAccountingPeriod period = createDefaultPeriod();
        period.setStatus(0);
        return period;
    }

    public static List<BizAccountingPeriod> createYearPeriods(int year) {
        List<BizAccountingPeriod> periods = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            BizAccountingPeriod period = createPeriodByYearMonth(year, month);
            if (month == 1) {
                period.setStatus(1);
            }
            periods.add(period);
        }
        return periods;
    }

    public static List<BizAccountingPeriod> createCurrentYearPeriods() {
        return createYearPeriods(2024);
    }

    public static BizAccountingPeriod createPeriodWithCode(String periodCode) {
        BizAccountingPeriod period = createDefaultPeriod();
        period.setPeriodCode(periodCode);
        
        int year = Integer.parseInt(periodCode.substring(0, 4));
        int month = Integer.parseInt(periodCode.substring(4, 6));
        period.setYear(year);
        period.setMonth(month);
        period.setStartDate(LocalDateTime.of(year, month, 1, 0, 0));
        period.setEndDate(LocalDateTime.of(year, month, 1, 0, 0).plusMonths(1).minusSeconds(1));
        
        return period;
    }

    public static String getDefaultTenantId() {
        return DEFAULT_TENANT_ID;
    }
}
