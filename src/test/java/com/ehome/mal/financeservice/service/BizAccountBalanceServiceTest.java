package com.ehome.mal.financeservice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ehome.mal.financeservice.entity.BizAccountBalance;
import com.ehome.mal.financeservice.mapper.BizAccountBalanceMapper;
import com.ehome.mal.financeservice.service.impl.BizAccountBalanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("科目余额Service测试")
class BizAccountBalanceServiceTest {

    @Mock
    private BizAccountBalanceMapper balanceMapper;

    @InjectMocks
    private BizAccountBalanceServiceImpl balanceService;

    private BizAccountBalance testBalance;

    @BeforeEach
    void setUp() {
        testBalance = createDefaultBalance();
    }

    private BizAccountBalance createDefaultBalance() {
        BizAccountBalance balance = new BizAccountBalance();
        balance.setId(1L);
        balance.setTenantId("tenant-001");
        balance.setAccountId(1L);
        balance.setPeriodCode("202401");
        balance.setPeriodDebit(BigDecimal.ZERO);
        balance.setPeriodCredit(BigDecimal.ZERO);
        balance.setYearDebit(BigDecimal.ZERO);
        balance.setYearCredit(BigDecimal.ZERO);
        balance.setEndingBalance(BigDecimal.ZERO);
        balance.setDelFlag(0);
        return balance;
    }

    @Nested
    @DisplayName("余额查询测试")
    class BalanceQueryTests {

        @Test
        @DisplayName("给定租户ID和期间_当查询余额_则返回余额列表")
        void givenTenantIdAndPeriod_whenGetBalances_thenReturnBalanceList() {
            List<BizAccountBalance> balances = Arrays.asList(testBalance);
            when(balanceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(balances);

            List<BizAccountBalance> result = balanceService.getBalancesByPeriod("tenant-001", "202401");

            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            verify(balanceMapper).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("给定无数据的期间_当查询余额_则返回空列表")
        void givenNoDataPeriod_whenGetBalances_thenReturnEmpty() {
            when(balanceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            List<BizAccountBalance> result = balanceService.getBalancesByPeriod("tenant-001", "202401");

            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("余额更新测试")
    class BalanceUpdateTests {

        @Test
        @DisplayName("给定存在的余额记录_当更新余额_则累加金额")
        void givenExistingBalance_whenUpdate_thenAccumulate() {
            testBalance.setPeriodDebit(new BigDecimal("100.00"));
            testBalance.setYearDebit(new BigDecimal("100.00"));
            when(balanceMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testBalance);
            when(balanceMapper.updateById(any(BizAccountBalance.class))).thenReturn(1);

            boolean result = balanceService.updateBalance(1L, "202401", new BigDecimal("50.00"), null);

            assertThat(result).isTrue();
            assertThat(testBalance.getPeriodDebit()).isEqualByComparingTo("150.00");
            assertThat(testBalance.getYearDebit()).isEqualByComparingTo("150.00");
            verify(balanceMapper).selectOne(any(LambdaQueryWrapper.class));
            verify(balanceMapper).updateById(any(BizAccountBalance.class));
        }

        @Test
        @DisplayName("给定不存在的余额记录_当更新余额_则新建记录")
        void givenNonExistentBalance_whenUpdate_thenCreateNew() {
            when(balanceMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(balanceMapper.insert(any(BizAccountBalance.class))).thenReturn(1);

            boolean result = balanceService.updateBalance(1L, "202401", new BigDecimal("100.00"), BigDecimal.ZERO);

            assertThat(result).isTrue();
            verify(balanceMapper).selectOne(any(LambdaQueryWrapper.class));
            verify(balanceMapper).insert(any(BizAccountBalance.class));
        }

        @Test
        @DisplayName("给定借方金额_当更新余额_则更新借方")
        void givenDebitAmount_whenUpdate_thenUpdateDebit() {
            when(balanceMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testBalance);
            when(balanceMapper.updateById(any(BizAccountBalance.class))).thenReturn(1);

            boolean result = balanceService.updateBalance(1L, "202401", new BigDecimal("100.00"), null);

            assertThat(result).isTrue();
            assertThat(testBalance.getPeriodDebit()).isEqualByComparingTo("100.00");
            assertThat(testBalance.getYearDebit()).isEqualByComparingTo("100.00");
        }

        @Test
        @DisplayName("给定贷方金额_当更新余额_则更新贷方")
        void givenCreditAmount_whenUpdate_thenUpdateCredit() {
            when(balanceMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testBalance);
            when(balanceMapper.updateById(any(BizAccountBalance.class))).thenReturn(1);

            boolean result = balanceService.updateBalance(1L, "202401", null, new BigDecimal("100.00"));

            assertThat(result).isTrue();
            assertThat(testBalance.getPeriodCredit()).isEqualByComparingTo("100.00");
            assertThat(testBalance.getYearCredit()).isEqualByComparingTo("100.00");
        }

        @Test
        @DisplayName("给定借贷金额_当更新余额_则同时更新")
        void givenDebitAndCreditAmount_whenUpdate_thenUpdateBoth() {
            when(balanceMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testBalance);
            when(balanceMapper.updateById(any(BizAccountBalance.class))).thenReturn(1);

            boolean result = balanceService.updateBalance(1L, "202401", new BigDecimal("100.00"), new BigDecimal("50.00"));

            assertThat(result).isTrue();
            assertThat(testBalance.getPeriodDebit()).isEqualByComparingTo("100.00");
            assertThat(testBalance.getPeriodCredit()).isEqualByComparingTo("50.00");
        }

        @Test
        @DisplayName("给定零金额_当更新余额_则不改变")
        void givenZeroAmount_whenUpdate_thenNoChange() {
            testBalance.setPeriodDebit(new BigDecimal("100.00"));
            when(balanceMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testBalance);
            when(balanceMapper.updateById(any(BizAccountBalance.class))).thenReturn(1);

            boolean result = balanceService.updateBalance(1L, "202401", BigDecimal.ZERO, null);

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("试算平衡检查测试")
    class TrialBalanceTests {

        @Test
        @DisplayName("给定平衡的余额_当检查试算平衡_则通过")
        void givenBalancedBalances_whenCheckTrialBalance_thenPass() {
            BizAccountBalance balance1 = createDefaultBalance();
            balance1.setYearDebit(new BigDecimal("1000.00"));
            balance1.setYearCredit(BigDecimal.ZERO);

            BizAccountBalance balance2 = createDefaultBalance();
            balance2.setAccountId(2L);
            balance2.setYearDebit(BigDecimal.ZERO);
            balance2.setYearCredit(new BigDecimal("1000.00"));

            List<BizAccountBalance> balances = Arrays.asList(balance1, balance2);
            when(balanceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(balances);

            boolean result = balanceService.calculateTrialBalance("tenant-001", "202401");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("给定不平衡的余额_当检查试算平衡_则失败")
        void givenUnbalancedBalances_whenCheckTrialBalance_thenFail() {
            BizAccountBalance balance1 = createDefaultBalance();
            balance1.setYearDebit(new BigDecimal("1000.00"));
            balance1.setYearCredit(BigDecimal.ZERO);

            BizAccountBalance balance2 = createDefaultBalance();
            balance2.setAccountId(2L);
            balance2.setYearDebit(BigDecimal.ZERO);
            balance2.setYearCredit(new BigDecimal("900.00"));

            List<BizAccountBalance> balances = Arrays.asList(balance1, balance2);
            when(balanceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(balances);

            boolean result = balanceService.calculateTrialBalance("tenant-001", "202401");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("给定空余额列表_当检查试算平衡_则通过")
        void givenEmptyBalances_whenCheckTrialBalance_thenPass() {
            when(balanceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            boolean result = balanceService.calculateTrialBalance("tenant-001", "202401");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("给定多条平衡余额_当检查试算平衡_则正确计算")
        void givenMultipleBalancedBalances_whenCheckTrialBalance_thenCalculateCorrectly() {
            BizAccountBalance balance1 = createDefaultBalance();
            balance1.setYearDebit(new BigDecimal("500.00"));
            balance1.setYearCredit(BigDecimal.ZERO);

            BizAccountBalance balance2 = createDefaultBalance();
            balance2.setAccountId(2L);
            balance2.setYearDebit(new BigDecimal("500.00"));
            balance2.setYearCredit(BigDecimal.ZERO);

            BizAccountBalance balance3 = createDefaultBalance();
            balance3.setAccountId(3L);
            balance3.setYearDebit(BigDecimal.ZERO);
            balance3.setYearCredit(new BigDecimal("1000.00"));

            List<BizAccountBalance> balances = Arrays.asList(balance1, balance2, balance3);
            when(balanceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(balances);

            boolean result = balanceService.calculateTrialBalance("tenant-001", "202401");

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("余额汇总测试")
    class BalanceSummaryTests {

        @Test
        @DisplayName("给定多个科目的余额_当汇总_则正确计算")
        void givenMultipleAccountBalances_whenSummarize_thenCalculateCorrectly() {
            BizAccountBalance balance1 = createDefaultBalance();
            balance1.setPeriodDebit(new BigDecimal("100.00"));
            balance1.setPeriodCredit(BigDecimal.ZERO);

            BizAccountBalance balance2 = createDefaultBalance();
            balance2.setAccountId(2L);
            balance2.setPeriodDebit(BigDecimal.ZERO);
            balance2.setPeriodCredit(new BigDecimal("50.00"));

            List<BizAccountBalance> balances = Arrays.asList(balance1, balance2);

            BigDecimal totalDebit = balances.stream()
                    .map(BizAccountBalance::getPeriodDebit)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalCredit = balances.stream()
                    .map(BizAccountBalance::getPeriodCredit)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            assertThat(totalDebit).isEqualByComparingTo("100.00");
            assertThat(totalCredit).isEqualByComparingTo("50.00");
        }
    }

    @Nested
    @DisplayName("余额初始化测试")
    class BalanceInitializationTests {

        @Test
        @DisplayName("给定新科目_当初始化余额_则创建余额记录")
        void givenNewAccount_whenInitializeBalance_thenCreateBalanceRecord() {
            when(balanceMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(balanceMapper.insert(any(BizAccountBalance.class))).thenReturn(1);

            boolean result = balanceService.updateBalance(1L, "202401", BigDecimal.ZERO, BigDecimal.ZERO);

            assertThat(result).isTrue();
            verify(balanceMapper).insert(any(BizAccountBalance.class));
        }
    }
}
