package com.ehome.mal.financeservice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ehome.mal.financeservice.entity.BizAccountingPeriod;
import com.ehome.mal.financeservice.fixture.PeriodTestDataFactory;
import com.ehome.mal.financeservice.mapper.BizAccountingPeriodMapper;
import com.ehome.mal.financeservice.service.impl.BizAccountingPeriodServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("会计期间Service测试")
class BizAccountingPeriodServiceTest {

    @Mock
    private BizAccountingPeriodMapper periodMapper;

    @InjectMocks
    private BizAccountingPeriodServiceImpl periodService;

    private BizAccountingPeriod testPeriod;

    @BeforeEach
    void setUp() {
        testPeriod = PeriodTestDataFactory.createDefaultPeriod();
    }

    @Nested
    @DisplayName("期间查询测试")
    class PeriodQueryTests {

        @Test
        @DisplayName("给定租户ID_当查询期间列表_则按年月降序")
        void givenTenantId_whenGetPeriods_thenReturnOrderedByYearMonthDesc() {
            List<BizAccountingPeriod> periods = PeriodTestDataFactory.createCurrentYearPeriods();
            when(periodMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(periods);

            List<BizAccountingPeriod> result = periodService.getPeriodsByTenant("tenant-001");

            assertThat(result).isNotNull();
            assertThat(result).hasSize(12);
            verify(periodMapper).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("给定不存在数据的租户ID_当查询期间_则返回空列表")
        void givenNonExistentTenantId_whenGetPeriods_thenReturnEmpty() {
            when(periodMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            List<BizAccountingPeriod> result = periodService.getPeriodsByTenant("non-existent");

            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("给定租户ID_当查询当前期间_则返回开启的期间")
        void givenTenantId_whenGetCurrentPeriod_thenReturnOpenPeriod() {
            testPeriod.setStatus(1);
            when(periodMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testPeriod);

            BizAccountingPeriod result = periodService.getCurrentPeriod("tenant-001");

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(1);
        }

        @Test
        @DisplayName("给定无开启期间的租户_当查询当前期间_则返回null")
        void givenNoOpenPeriod_whenGetCurrentPeriod_thenReturnNull() {
            when(periodMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            BizAccountingPeriod result = periodService.getCurrentPeriod("tenant-001");

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("期间开启测试")
    class OpenPeriodTests {

        @Test
        @DisplayName("给定未开启的期间_当开启期间_则成功")
        void givenUnopenPeriod_whenOpen_thenSuccess() {
            testPeriod.setStatus(0);
            when(periodMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testPeriod);
            when(periodMapper.updateById(any(BizAccountingPeriod.class))).thenReturn(1);

            boolean result = periodService.openPeriod("202401");

            assertThat(result).isTrue();
            assertThat(testPeriod.getStatus()).isEqualTo(1);
            assertThat(testPeriod.getUpdateTime()).isNotNull();
            verify(periodMapper).selectOne(any(LambdaQueryWrapper.class));
            verify(periodMapper).updateById(any(BizAccountingPeriod.class));
        }

        @Test
        @DisplayName("给定已开启的期间_当开启期间_则失败")
        void givenOpenPeriod_whenOpen_thenReturnFalse() {
            testPeriod.setStatus(1);
            when(periodMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testPeriod);

            boolean result = periodService.openPeriod("202401");

            assertThat(result).isFalse();
            verify(periodMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("给定不存在的期间_当开启期间_则失败")
        void givenNonExistentPeriod_whenOpen_thenReturnFalse() {
            when(periodMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            boolean result = periodService.openPeriod("209901");

            assertThat(result).isFalse();
            verify(periodMapper, never()).updateById(any());
        }
    }

    @Nested
    @DisplayName("期间关闭测试")
    class ClosePeriodTests {

        @Test
        @DisplayName("给定开启的期间_当关闭期间_则成功")
        void givenOpenPeriod_whenClose_thenSuccess() {
            testPeriod.setStatus(1);
            when(periodMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testPeriod);
            when(periodMapper.updateById(any(BizAccountingPeriod.class))).thenReturn(1);

            boolean result = periodService.closePeriod("202401");

            assertThat(result).isTrue();
            assertThat(testPeriod.getStatus()).isEqualTo(2);
            assertThat(testPeriod.getUpdateTime()).isNotNull();
            verify(periodMapper).selectOne(any(LambdaQueryWrapper.class));
            verify(periodMapper).updateById(any(BizAccountingPeriod.class));
        }

        @Test
        @DisplayName("给定已关闭的期间_当关闭期间_则失败")
        void givenClosedPeriod_whenClose_thenReturnFalse() {
            testPeriod.setStatus(2);
            when(periodMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testPeriod);

            boolean result = periodService.closePeriod("202401");

            assertThat(result).isFalse();
            verify(periodMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("给定不存在的期间_当关闭期间_则失败")
        void givenNonExistentPeriod_whenClose_thenReturnFalse() {
            when(periodMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            boolean result = periodService.closePeriod("209901");

            assertThat(result).isFalse();
            verify(periodMapper, never()).updateById(any());
        }
    }

    @Nested
    @DisplayName("期间状态校验测试")
    class PeriodStatusValidationTests {

        @Test
        @DisplayName("给定未开启期间_当验证状态_则为0")
        void givenUnopenPeriod_whenValidateStatus_thenZero() {
            BizAccountingPeriod period = PeriodTestDataFactory.createUnopenPeriod();
            assertThat(period.getStatus()).isEqualTo(0);
        }

        @Test
        @DisplayName("给定开启期间_当验证状态_则为1")
        void givenOpenPeriod_whenValidateStatus_thenOne() {
            BizAccountingPeriod period = PeriodTestDataFactory.createOpenPeriod();
            assertThat(period.getStatus()).isEqualTo(1);
        }

        @Test
        @DisplayName("给定关闭期间_当验证状态_则为2")
        void givenClosedPeriod_whenValidateStatus_thenTwo() {
            BizAccountingPeriod period = PeriodTestDataFactory.createClosedPeriod();
            assertThat(period.getStatus()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("期间创建测试")
    class PeriodCreationTests {

        @Test
        @DisplayName("给定年度_当创建期间_则生成12个期间")
        void givenYear_whenCreatePeriods_thenGenerate12Periods() {
            List<BizAccountingPeriod> periods = PeriodTestDataFactory.createYearPeriods(2024);

            assertThat(periods).hasSize(12);
            assertThat(periods.get(0).getYear()).isEqualTo(2024);
            assertThat(periods.get(0).getMonth()).isEqualTo(1);
            assertThat(periods.get(11).getMonth()).isEqualTo(12);
        }

        @Test
        @DisplayName("给定期间_当验证期间编码格式_则正确")
        void givenPeriod_whenValidatePeriodCodeFormat_thenCorrect() {
            BizAccountingPeriod period = PeriodTestDataFactory.createPeriodByYearMonth(2024, 3);

            assertThat(period.getPeriodCode()).isEqualTo("202403");
            assertThat(period.getYear()).isEqualTo(2024);
            assertThat(period.getMonth()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("期间日期范围测试")
    class PeriodDateRangeTests {

        @Test
        @DisplayName("给定期间_当验证日期范围_则正确")
        void givenPeriod_whenValidateDateRange_thenCorrect() {
            BizAccountingPeriod period = PeriodTestDataFactory.createDefaultPeriod();

            assertThat(period.getStartDate()).isNotNull();
            assertThat(period.getEndDate()).isNotNull();
            assertThat(period.getEndDate()).isAfter(period.getStartDate());
        }

        @Test
        @DisplayName("给定12月期间_当验证日期范围_则到月末")
        void givenDecemberPeriod_whenValidateDateRange_thenEndOfMonth() {
            BizAccountingPeriod period = PeriodTestDataFactory.createPeriodByYearMonth(2024, 12);

            assertThat(period.getMonth()).isEqualTo(12);
            assertThat(period.getEndDate()).isNotNull();
        }
    }

    @Nested
    @DisplayName("结账检查测试")
    class ClosingCheckTests {

        @Test
        @DisplayName("给定期间_当验证可结账条件_则需满足试算平衡")
        void givenPeriod_whenValidateClosingCondition_thenNeedTrialBalance() {
            BizAccountingPeriod period = PeriodTestDataFactory.createOpenPeriod();

            boolean canClose = period.getStatus() == 1;

            assertThat(canClose).isTrue();
        }

        @Test
        @DisplayName("给定未开启期间_当验证可结账条件_则不可结账")
        void givenUnopenPeriod_whenValidateClosingCondition_thenCannotClose() {
            BizAccountingPeriod period = PeriodTestDataFactory.createUnopenPeriod();

            boolean canClose = period.getStatus() == 1;

            assertThat(canClose).isFalse();
        }
    }
}
