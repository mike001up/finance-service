package com.ehome.mal.financeservice.service;

import com.ehome.mal.financeservice.entity.BizAccount;
import com.ehome.mal.financeservice.fixture.AccountTestDataFactory;
import com.ehome.mal.financeservice.mapper.BizAccountMapper;
import com.ehome.mal.financeservice.service.impl.BizAccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("科目管理Service测试")
class BizAccountServiceTest {

    @Mock
    private BizAccountMapper accountMapper;

    @InjectMocks
    private BizAccountServiceImpl accountService;

    private BizAccount testAccount;

    @BeforeEach
    void setUp() {
        testAccount = AccountTestDataFactory.createDefaultAccount();
    }

    @Nested
    @DisplayName("科目树形结构测试")
    class AccountTreeTests {

        @Test
        @DisplayName("给定有效租户ID_当查询科目树_则返回树形结构")
        void givenValidTenantId_whenGetAccountTree_thenReturnTreeStructure() {
            List<BizAccount> expectedTree = AccountTestDataFactory.createAccountTree();
            when(accountMapper.selectTreeList(anyString())).thenReturn(expectedTree);

            List<BizAccount> result = accountService.getAccountTree("tenant-001");

            assertThat(result).isNotNull();
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getParentId()).isEqualTo(0L);
            assertThat(result.get(1).getParentId()).isEqualTo(1L);
            verify(accountMapper).selectTreeList("tenant-001");
        }

        @Test
        @DisplayName("给定不存在数据的租户ID_当查询科目树_则返回空列表")
        void givenNonExistentTenantId_whenGetAccountTree_thenReturnEmptyList() {
            when(accountMapper.selectTreeList(anyString())).thenReturn(Collections.emptyList());

            List<BizAccount> result = accountService.getAccountTree("non-existent");

            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("给定空租户ID_当查询科目树_则正常处理")
        void givenEmptyTenantId_whenGetAccountTree_thenHandleGracefully() {
            when(accountMapper.selectTreeList(anyString())).thenReturn(Collections.emptyList());

            List<BizAccount> result = accountService.getAccountTree("");

            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
            verify(accountMapper).selectTreeList("");
        }
    }

    @Nested
    @DisplayName("科目创建测试")
    class CreateAccountTests {

        @Test
        @DisplayName("给定有效科目_当创建科目_则成功")
        void givenValidAccount_whenCreate_thenSuccess() {
            when(accountMapper.insert(any(BizAccount.class))).thenReturn(1);

            boolean result = accountService.saveAccount(testAccount);

            assertThat(result).isTrue();
            assertThat(testAccount.getCreateTime()).isNotNull();
            assertThat(testAccount.getUpdateTime()).isNotNull();
            assertThat(testAccount.getDelFlag()).isEqualTo(0);
            verify(accountMapper).insert(any(BizAccount.class));
        }

        @Test
        @DisplayName("给定资产类科目_当创建科目_则方向为借方")
        void givenAssetAccount_whenCreate_thenDirectionIsDebit() {
            BizAccount assetAccount = AccountTestDataFactory.createAssetAccount();
            when(accountMapper.insert(any(BizAccount.class))).thenReturn(1);

            boolean result = accountService.saveAccount(assetAccount);

            assertThat(result).isTrue();
            assertThat(assetAccount.getDirection()).isEqualTo("DEBIT");
            assertThat(assetAccount.getAccountType()).isEqualTo("ASSET");
        }

        @Test
        @DisplayName("给定负债类科目_当创建科目_则方向为贷方")
        void givenLiabilityAccount_whenCreate_thenDirectionIsCredit() {
            BizAccount liabilityAccount = AccountTestDataFactory.createLiabilityAccount();
            when(accountMapper.insert(any(BizAccount.class))).thenReturn(1);

            boolean result = accountService.saveAccount(liabilityAccount);

            assertThat(result).isTrue();
            assertThat(liabilityAccount.getDirection()).isEqualTo("CREDIT");
            assertThat(liabilityAccount.getAccountType()).isEqualTo("LIABILITY");
        }

        @Test
        @DisplayName("给定子科目_当创建科目_则成功并保留父科目ID")
        void givenChildAccount_whenCreate_thenSuccessWithParentId() {
            BizAccount childAccount = AccountTestDataFactory.createChildAccount(1L, 2);
            when(accountMapper.insert(any(BizAccount.class))).thenReturn(1);

            boolean result = accountService.saveAccount(childAccount);

            assertThat(result).isTrue();
            assertThat(childAccount.getParentId()).isEqualTo(1L);
            assertThat(childAccount.getLevel()).isEqualTo(2);
        }

        @Test
        @DisplayName("给定科目带初始余额_当创建科目_则成功保留余额")
        void givenAccountWithInitialBalance_whenCreate_thenSuccessWithBalance() {
            BizAccount account = AccountTestDataFactory.createAccountWithInitialBalance(
                    new java.math.BigDecimal("10000.00"),
                    java.math.BigDecimal.ZERO
            );
            when(accountMapper.insert(any(BizAccount.class))).thenReturn(1);

            boolean result = accountService.saveAccount(account);

            assertThat(result).isTrue();
            assertThat(account.getInitialDebit()).isEqualByComparingTo("10000.00");
            assertThat(account.getInitialCredit()).isEqualByComparingTo("0");
        }
    }

    @Nested
    @DisplayName("科目更新测试")
    class UpdateAccountTests {

        @Test
        @DisplayName("给定有效科目_当更新科目_则成功")
        void givenValidAccount_whenUpdate_thenSuccess() {
            testAccount.setId(1L);
            testAccount.setAccountName("更新后的科目名称");
            when(accountMapper.updateById(any(BizAccount.class))).thenReturn(1);

            boolean result = accountService.updateAccount(testAccount);

            assertThat(result).isTrue();
            assertThat(testAccount.getUpdateTime()).isNotNull();
            verify(accountMapper).updateById(any(BizAccount.class));
        }

        @Test
        @DisplayName("给定科目修改科目类型_当更新科目_则成功")
        void givenAccountWithTypeChange_whenUpdate_thenSuccess() {
            testAccount.setId(1L);
            testAccount.setAccountType("LIABILITY");
            when(accountMapper.updateById(any(BizAccount.class))).thenReturn(1);

            boolean result = accountService.updateAccount(testAccount);

            assertThat(result).isTrue();
            assertThat(testAccount.getAccountType()).isEqualTo("LIABILITY");
        }
    }

    @Nested
    @DisplayName("科目删除测试")
    class DeleteAccountTests {

        @Test
        @DisplayName("给定存在的科目ID_当删除科目_则逻辑删除成功")
        void givenExistentAccountId_whenDelete_thenLogicalDeleteSuccess() {
            testAccount.setId(1L);
            when(accountMapper.selectById(1L)).thenReturn(testAccount);
            when(accountMapper.updateById(any(BizAccount.class))).thenReturn(1);

            boolean result = accountService.deleteAccount(1L);

            assertThat(result).isTrue();
            assertThat(testAccount.getDelFlag()).isEqualTo(1);
            assertThat(testAccount.getUpdateTime()).isNotNull();
            verify(accountMapper).selectById(1L);
            verify(accountMapper).updateById(any(BizAccount.class));
        }

        @Test
        @DisplayName("给定不存在的科目ID_当删除科目_则返回失败")
        void givenNonExistentAccountId_whenDelete_thenReturnFalse() {
            when(accountMapper.selectById(999L)).thenReturn(null);

            boolean result = accountService.deleteAccount(999L);

            assertThat(result).isFalse();
            verify(accountMapper).selectById(999L);
            verify(accountMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("给定空ID_当删除科目_则返回失败")
        void givenNullId_whenDelete_thenReturnFalse() {
            when(accountMapper.selectById(null)).thenReturn(null);

            boolean result = accountService.deleteAccount(null);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("科目查询测试")
    class QueryAccountTests {

        @Test
        @DisplayName("给定存在的ID_当查询科目_则返回科目")
        void givenExistentId_whenGetById_thenReturnAccount() {
            testAccount.setId(1L);
            when(accountMapper.selectById(1L)).thenReturn(testAccount);

            BizAccount result = accountService.getById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getAccountCode()).isEqualTo("1001");
        }

        @Test
        @DisplayName("给定不存在的ID_当查询科目_则返回null")
        void givenNonExistentId_whenGetById_thenReturnNull() {
            when(accountMapper.selectById(999L)).thenReturn(null);

            BizAccount result = accountService.getById(999L);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("科目编码唯一性测试")
    class AccountCodeUniquenessTests {

        @Test
        @DisplayName("给定相同编码的科目列表_当验证编码唯一性_则可识别重复")
        void givenAccountsWitSameCode_whenValidateUniqueness_thenCanIdentifyDuplicate() {
            BizAccount account1 = AccountTestDataFactory.createDefaultAccount();
            account1.setAccountCode("1001");
            BizAccount account2 = AccountTestDataFactory.createDefaultAccount();
            account2.setAccountCode("1001");

            List<BizAccount> accounts = Arrays.asList(account1, account2);

            long distinctCount = accounts.stream()
                    .map(BizAccount::getAccountCode)
                    .distinct()
                    .count();

            assertThat(distinctCount).isLessThan(accounts.size());
        }

        @Test
        @DisplayName("给定不同编码的科目列表_当验证编码唯一性_则通过")
        void givenAccountsWithDifferentCodes_whenValidateUniqueness_thenPass() {
            BizAccount account1 = AccountTestDataFactory.createAccountWithCode("1001");
            BizAccount account2 = AccountTestDataFactory.createAccountWithCode("1002");
            BizAccount account3 = AccountTestDataFactory.createAccountWithCode("1003");

            List<BizAccount> accounts = Arrays.asList(account1, account2, account3);

            long distinctCount = accounts.stream()
                    .map(BizAccount::getAccountCode)
                    .distinct()
                    .count();

            assertThat(distinctCount).isEqualTo(accounts.size());
        }
    }

    @Nested
    @DisplayName("科目类型验证测试")
    class AccountTypeValidationTests {

        @Test
        @DisplayName("给定资产类科目_当验证类型_则正确")
        void givenAssetAccount_whenValidateType_thenCorrect() {
            BizAccount account = AccountTestDataFactory.createAssetAccount();
            assertThat(account.getAccountType()).isEqualTo("ASSET");
            assertThat(account.getDirection()).isEqualTo("DEBIT");
        }

        @Test
        @DisplayName("给定负债类科目_当验证类型_则正确")
        void givenLiabilityAccount_whenValidateType_thenCorrect() {
            BizAccount account = AccountTestDataFactory.createLiabilityAccount();
            assertThat(account.getAccountType()).isEqualTo("LIABILITY");
            assertThat(account.getDirection()).isEqualTo("CREDIT");
        }

        @Test
        @DisplayName("给定所有科目类型_当验证类型列表_则全部有效")
        void givenAllAccountTypes_whenValidateTypes_thenAllValid() {
            List<String> validTypes = Arrays.asList("ASSET", "LIABILITY", "EQUITY", "EXPENSE", "REVENUE");

            BizAccount asset = AccountTestDataFactory.createAssetAccount();
            BizAccount liability = AccountTestDataFactory.createLiabilityAccount();
            BizAccount equity = AccountTestDataFactory.createEquityAccount();
            BizAccount expense = AccountTestDataFactory.createExpenseAccount();
            BizAccount revenue = AccountTestDataFactory.createRevenueAccount();

            assertThat(validTypes).contains(asset.getAccountType());
            assertThat(validTypes).contains(liability.getAccountType());
            assertThat(validTypes).contains(equity.getAccountType());
            assertThat(validTypes).contains(expense.getAccountType());
            assertThat(validTypes).contains(revenue.getAccountType());
        }
    }

    @Nested
    @DisplayName("科目层级关系测试")
    class AccountLevelTests {

        @Test
        @DisplayName("给定科目树_当验证层级关系_则正确")
        void givenAccountTree_whenValidateLevelRelations_thenCorrect() {
            List<BizAccount> tree = AccountTestDataFactory.createAccountTree();

            BizAccount parent = tree.get(0);
            BizAccount child1 = tree.get(1);
            BizAccount child2 = tree.get(2);

            assertThat(parent.getLevel()).isEqualTo(1);
            assertThat(child1.getLevel()).isEqualTo(2);
            assertThat(child2.getLevel()).isEqualTo(2);

            assertThat(child1.getParentId()).isEqualTo(parent.getId());
            assertThat(child2.getParentId()).isEqualTo(parent.getId());
        }

        @Test
        @DisplayName("给定多层科目_当构建路径_则正确")
        void givenMultiLevelAccounts_whenBuildPath_thenCorrect() {
            BizAccount level1 = AccountTestDataFactory.createDefaultAccount();
            level1.setLevel(1);
            level1.setAccountCode("1001");

            BizAccount level2 = AccountTestDataFactory.createChildAccount(1L, 2);
            level2.setAccountCode("100101");

            assertThat(level2.getAccountCode()).startsWith(level1.getAccountCode());
            assertThat(level2.getLevel()).isGreaterThan(level1.getLevel());
        }
    }
}
