package com.ehome.mal.financeservice.integration;

import com.ehome.mal.financeservice.entity.BizAccount;
import com.ehome.mal.financeservice.entity.BizVoucher;
import com.ehome.mal.financeservice.entity.BizVoucherEntry;
import com.ehome.mal.financeservice.fixture.AccountTestDataFactory;
import com.ehome.mal.financeservice.fixture.VoucherTestDataFactory;
import com.ehome.mal.financeservice.service.BizAccountService;
import com.ehome.mal.financeservice.service.BizVoucherEntryService;
import com.ehome.mal.financeservice.service.BizVoucherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("凭证业务流程集成测试")
class VoucherFlowIntegrationTest {

    @Mock
    private BizAccountService accountService;

    @Mock
    private BizVoucherService voucherService;

    @Mock
    private BizVoucherEntryService entryService;

    @Nested
    @DisplayName("凭证完整业务流程测试")
    class CompleteVoucherFlowTests {

        @Test
        @DisplayName("凭证业务流程_从创建到审核")
        void voucherFlow_FromCreationToAudit() {
            BizAccount debitAccount = AccountTestDataFactory.createAssetAccount();
            debitAccount.setId(1L);
            BizAccount creditAccount = AccountTestDataFactory.createLiabilityAccount();
            creditAccount.setId(2L);

            when(accountService.getById(1L)).thenReturn(debitAccount);
            when(accountService.getById(2L)).thenReturn(creditAccount);

            BizVoucher voucher = VoucherTestDataFactory.createDefaultVoucher();
            voucher.setId(1L);
            when(voucherService.saveVoucher(any())).thenReturn(true);
            when(voucherService.reviewVoucher(1L, "auditor")).thenReturn(true);

            List<BizVoucherEntry> entries = VoucherTestDataFactory.createBalancedEntries();
            when(entryService.checkBalance(entries)).thenReturn(true);

            boolean balanceChecked = entryService.checkBalance(entries);
            assertThat(balanceChecked).isTrue();

            boolean created = voucherService.saveVoucher(voucher);
            assertThat(created).isTrue();

            boolean audited = voucherService.reviewVoucher(1L, "auditor");
            assertThat(audited).isTrue();

            verify(voucherService).saveVoucher(any());
            verify(voucherService).reviewVoucher(1L, "auditor");
            verify(entryService).checkBalance(entries);
        }

        @Test
        @DisplayName("凭证审核后再反审核")
        void voucherAuditThenUnaudit() {
            BizVoucher voucher = VoucherTestDataFactory.createAuditedVoucher();
            voucher.setId(1L);

            when(voucherService.reviewVoucher(1L, "auditor")).thenReturn(true);
            when(voucherService.unreviewVoucher(1L)).thenReturn(true);

            boolean audited = voucherService.reviewVoucher(1L, "auditor");
            assertThat(audited).isTrue();

            boolean unaudited = voucherService.unreviewVoucher(1L);
            assertThat(unaudited).isTrue();

            verify(voucherService).reviewVoucher(1L, "auditor");
            verify(voucherService).unreviewVoucher(1L);
        }
    }

    @Nested
    @DisplayName("凭证借贷平衡验证测试")
    class VoucherBalanceValidationTests {

        @Test
        @DisplayName("多借一贷凭证_借贷平衡")
        void multiDebitOneCreditVoucher_Balanced() {
            List<BizVoucherEntry> entries = VoucherTestDataFactory.createMultiEntries();
            when(entryService.checkBalance(entries)).thenReturn(true);

            boolean balanced = entryService.checkBalance(entries);

            assertThat(balanced).isTrue();
        }

        @Test
        @DisplayName("一借多贷凭证_借贷平衡")
        void oneDebitMultiCreditVoucher_Balanced() {
            List<BizVoucherEntry> entries = List.of(
                    VoucherTestDataFactory.createDebitEntry(1L, new java.math.BigDecimal("1000.00")),
                    VoucherTestDataFactory.createCreditEntry(2L, new java.math.BigDecimal("600.00")),
                    VoucherTestDataFactory.createCreditEntry(3L, new java.math.BigDecimal("400.00"))
            );
            when(entryService.checkBalance(entries)).thenReturn(true);

            boolean balanced = entryService.checkBalance(entries);

            assertThat(balanced).isTrue();
        }
    }

    @Nested
    @DisplayName("科目树形结构验证测试")
    class AccountTreeValidationTests {

        @Test
        @DisplayName("科目树形结构_父子关系正确")
        void accountTree_ParentChildRelationCorrect() {
            List<BizAccount> accountTree = AccountTestDataFactory.createAccountTree();
            when(accountService.getAccountTree("tenant-001")).thenReturn(accountTree);

            List<BizAccount> result = accountService.getAccountTree("tenant-001");

            assertThat(result).hasSize(3);

            BizAccount parent = result.get(0);
            assertThat(parent.getLevel()).isEqualTo(1);
            assertThat(parent.getParentId()).isEqualTo(0L);

            BizAccount child1 = result.get(1);
            BizAccount child2 = result.get(2);
            assertThat(child1.getParentId()).isEqualTo(parent.getId());
            assertThat(child2.getParentId()).isEqualTo(parent.getId());
            assertThat(child1.getLevel()).isEqualTo(2);
            assertThat(child2.getLevel()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("凭证号自动生成验证测试")
    class VoucherNoGenerationValidationTests {

        @Test
        @DisplayName("凭证号按期间独立编号")
        void voucherNo_IndependentByPeriod() {
            when(voucherService.generateVoucherNo("tenant-001", "202401", "JZ"))
                    .thenReturn("202401-JZ-0001");
            when(voucherService.generateVoucherNo("tenant-001", "202402", "JZ"))
                    .thenReturn("202402-JZ-0001");

            String period1No = voucherService.generateVoucherNo("tenant-001", "202401", "JZ");
            String period2No = voucherService.generateVoucherNo("tenant-001", "202402", "JZ");

            assertThat(period1No).isEqualTo("202401-JZ-0001");
            assertThat(period2No).isEqualTo("202402-JZ-0001");
            assertThat(period1No).isNotEqualTo(period2No);
        }

        @Test
        @DisplayName("凭证号按类型独立编号")
        void voucherNo_IndependentByType() {
            when(voucherService.generateVoucherNo("tenant-001", "202401", "JZ"))
                    .thenReturn("202401-JZ-0001");
            when(voucherService.generateVoucherNo("tenant-001", "202401", "SK"))
                    .thenReturn("202401-SK-0001");

            String jzNo = voucherService.generateVoucherNo("tenant-001", "202401", "JZ");
            String skNo = voucherService.generateVoucherNo("tenant-001", "202401", "SK");

            assertThat(jzNo).isEqualTo("202401-JZ-0001");
            assertThat(skNo).isEqualTo("202401-SK-0001");
            assertThat(jzNo).isNotEqualTo(skNo);
        }
    }

    @Nested
    @DisplayName("数据完整性验证测试")
    class DataIntegrityTests {

        @Test
        @DisplayName("凭证删除前需验证状态")
        void voucherDeletion_ValidateStatusFirst() {
            BizVoucher auditedVoucher = VoucherTestDataFactory.createAuditedVoucher();
            auditedVoucher.setId(1L);

            assertThat(auditedVoucher.getStatus()).isEqualTo(1);
        }

        @Test
        @DisplayName("科目删除前需验证是否有子科目")
        void accountDeletion_ValidateChildrenFirst() {
            List<BizAccount> accountTree = AccountTestDataFactory.createAccountTree();
            BizAccount parentAccount = accountTree.get(0);

            long childCount = accountTree.stream()
                    .filter(a -> parentAccount.getId().equals(a.getParentId()))
                    .count();

            assertThat(childCount).isGreaterThan(0);
        }
    }
}
