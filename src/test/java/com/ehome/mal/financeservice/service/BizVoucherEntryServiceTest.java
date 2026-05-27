package com.ehome.mal.financeservice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ehome.mal.financeservice.entity.BizVoucherEntry;
import com.ehome.mal.financeservice.fixture.VoucherTestDataFactory;
import com.ehome.mal.financeservice.mapper.BizVoucherEntryMapper;
import com.ehome.mal.financeservice.service.impl.BizVoucherEntryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("凭证分录Service测试")
class BizVoucherEntryServiceTest {

    @Mock
    private BizVoucherEntryMapper entryMapper;

    @InjectMocks
    private BizVoucherEntryServiceImpl entryService;

    private BizVoucherEntry testEntry;

    @BeforeEach
    void setUp() {
        testEntry = VoucherTestDataFactory.createDefaultEntry();
    }

    @Nested
    @DisplayName("借贷平衡校验测试")
    class BalanceCheckTests {

        @Test
        @DisplayName("给定平衡的分录_当校验借贷平衡_则通过")
        void givenBalancedEntries_whenCheckBalance_thenPass() {
            List<BizVoucherEntry> entries = VoucherTestDataFactory.createBalancedEntries();

            boolean result = entryService.checkBalance(entries);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("给定不平衡的分录_当校验借贷平衡_则失败")
        void givenUnbalancedEntries_whenCheckBalance_thenFail() {
            List<BizVoucherEntry> entries = VoucherTestDataFactory.createUnbalancedEntries();

            boolean result = entryService.checkBalance(entries);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("给定空分录列表_当校验借贷平衡_则通过")
        void givenEmptyEntries_whenCheckBalance_thenPass() {
            boolean result = entryService.checkBalance(Collections.emptyList());

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("给定多条分录_当校验借贷平衡_则正确计算")
        void givenMultipleEntries_whenCheckBalance_thenCalculateCorrectly() {
            List<BizVoucherEntry> entries = VoucherTestDataFactory.createMultiEntries();

            boolean result = entryService.checkBalance(entries);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("给定只有借方的分录_当校验借贷平衡_则失败")
        void givenDebitOnlyEntries_whenCheckBalance_thenFail() {
            List<BizVoucherEntry> entries = List.of(
                    VoucherTestDataFactory.createDebitEntry(1L, new BigDecimal("1000.00"))
            );

            boolean result = entryService.checkBalance(entries);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("给定只有贷方的分录_当校验借贷平衡_则失败")
        void givenCreditOnlyEntries_whenCheckBalance_thenFail() {
            List<BizVoucherEntry> entries = List.of(
                    VoucherTestDataFactory.createCreditEntry(1L, new BigDecimal("1000.00"))
            );

            boolean result = entryService.checkBalance(entries);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("分录增删改测试")
    class EntryCrudTests {

        @Test
        @DisplayName("给定凭证ID和分录列表_当保存分录_则成功")
        void givenVoucherIdAndEntries_whenSaveEntries_thenSuccess() {
            List<BizVoucherEntry> entries = VoucherTestDataFactory.createBalancedEntries();
            when(entryMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
            when(entryMapper.insert(any(BizVoucherEntry.class))).thenReturn(1);

            boolean result = entryService.saveEntries(1L, entries);

            assertThat(result).isTrue();
            assertThat(entries.get(0).getVoucherId()).isEqualTo(1L);
            assertThat(entries.get(0).getEntrySeq()).isEqualTo(1);
            assertThat(entries.get(1).getEntrySeq()).isEqualTo(2);
            verify(entryMapper).delete(any(LambdaQueryWrapper.class));
            verify(entryMapper, times(2)).insert(any(BizVoucherEntry.class));
        }

        @Test
        @DisplayName("给定空分录列表_当保存分录_则删除已有分录")
        void givenEmptyEntries_whenSaveEntries_thenDeleteExisting() {
            when(entryMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(0);

            boolean result = entryService.saveEntries(1L, Collections.emptyList());

            assertThat(result).isTrue();
            verify(entryMapper).delete(any(LambdaQueryWrapper.class));
            verify(entryMapper, never()).insert(any());
        }

        @Test
        @DisplayName("给定凭证ID_当查询分录_则按序号排序")
        void givenVoucherId_whenGetEntries_thenReturnOrderedBySeq() {
            List<BizVoucherEntry> entries = VoucherTestDataFactory.createBalancedEntries();
            when(entryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(entries);

            List<BizVoucherEntry> result = entryService.getEntriesByVoucherId(1L);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            verify(entryMapper).selectList(any(LambdaQueryWrapper.class));
        }
    }

    @Nested
    @DisplayName("借贷方向验证测试")
    class DirectionValidationTests {

        @Test
        @DisplayName("给定借方分录_当验证方向_则借方金额大于零")
        void givenDebitEntry_whenValidateDirection_thenDebitAmountGreaterThanZero() {
            BizVoucherEntry debitEntry = VoucherTestDataFactory.createDebitEntry(1L, new BigDecimal("100.00"));

            assertThat(debitEntry.getDebitAmount()).isGreaterThan(BigDecimal.ZERO);
            assertThat(debitEntry.getCreditAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(debitEntry.getDirection()).isEqualTo("DEBIT");
        }

        @Test
        @DisplayName("给定贷方分录_当验证方向_则贷方金额大于零")
        void givenCreditEntry_whenValidateDirection_thenCreditAmountGreaterThanZero() {
            BizVoucherEntry creditEntry = VoucherTestDataFactory.createCreditEntry(1L, new BigDecimal("100.00"));

            assertThat(creditEntry.getCreditAmount()).isGreaterThan(BigDecimal.ZERO);
            assertThat(creditEntry.getDebitAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(creditEntry.getDirection()).isEqualTo("CREDIT");
        }
    }

    @Nested
    @DisplayName("金额有效性验证测试")
    class AmountValidationTests {

        @Test
        @DisplayName("给定正数金额_当验证金额_则有效")
        void givenPositiveAmount_whenValidateAmount_thenValid() {
            BizVoucherEntry entry = VoucherTestDataFactory.createDebitEntry(1L, new BigDecimal("100.00"));

            assertThat(entry.getDebitAmount()).isPositive();
        }

        @Test
        @DisplayName("给定零金额_当验证金额_则允许")
        void givenZeroAmount_whenValidateAmount_thenAllowed() {
            BizVoucherEntry entry = VoucherTestDataFactory.createDebitEntry(1L, BigDecimal.ZERO);

            assertThat(entry.getDebitAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("给定精确到分的金额_当验证金额_则正确")
        void givenAmountWithCents_whenValidateAmount_thenCorrect() {
            BizVoucherEntry entry = VoucherTestDataFactory.createDebitEntry(1L, new BigDecimal("123.45"));

            assertThat(entry.getDebitAmount()).isEqualByComparingTo("123.45");
        }

        @Test
        @DisplayName("给定大金额_当验证金额_则正确处理")
        void givenLargeAmount_whenValidateAmount_thenHandleCorrectly() {
            BizVoucherEntry entry = VoucherTestDataFactory.createDebitEntry(1L, new BigDecimal("999999999.99"));

            assertThat(entry.getDebitAmount()).isEqualByComparingTo("999999999.99");
        }
    }

    @Nested
    @DisplayName("分录序号测试")
    class EntrySequenceTests {

        @Test
        @DisplayName("给定多条分录_当保存_则序号连续")
        void givenMultipleEntries_whenSave_thenSequenceContinuous() {
            List<BizVoucherEntry> entries = VoucherTestDataFactory.createMultiEntries();
            when(entryMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
            when(entryMapper.insert(any(BizVoucherEntry.class))).thenReturn(1);

            entryService.saveEntries(1L, entries);

            assertThat(entries.get(0).getEntrySeq()).isEqualTo(1);
            assertThat(entries.get(1).getEntrySeq()).isEqualTo(2);
            assertThat(entries.get(2).getEntrySeq()).isEqualTo(3);
        }

        @Test
        @DisplayName("给定单条分录_当保存_则序号为1")
        void givenSingleEntry_whenSave_thenSequenceIsOne() {
            List<BizVoucherEntry> entries = List.of(testEntry);
            when(entryMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(0);
            when(entryMapper.insert(any(BizVoucherEntry.class))).thenReturn(1);

            entryService.saveEntries(1L, entries);

            assertThat(entries.get(0).getEntrySeq()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("辅助核算项验证测试")
    class AuxiliaryItemTests {

        @Test
        @DisplayName("给定带辅助核算的分录_当验证_则辅助项完整")
        void givenEntryWithAuxiliary_whenValidate_thenAuxiliaryComplete() {
            testEntry.setAuxiliary1Type("CUSTOMER");
            testEntry.setAuxiliary1Value("C001");

            assertThat(testEntry.getAuxiliary1Type()).isEqualTo("CUSTOMER");
            assertThat(testEntry.getAuxiliary1Value()).isEqualTo("C001");
        }

        @Test
        @DisplayName("给定无辅助核算的分录_当验证_则辅助项为空")
        void givenEntryWithoutAuxiliary_whenValidate_thenAuxiliaryNull() {
            assertThat(testEntry.getAuxiliary1Type()).isNull();
            assertThat(testEntry.getAuxiliary1Value()).isNull();
        }
    }
}
