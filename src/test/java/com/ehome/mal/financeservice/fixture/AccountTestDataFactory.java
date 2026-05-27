package com.ehome.mal.financeservice.fixture;

import com.ehome.mal.financeservice.entity.BizAccount;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class AccountTestDataFactory {

    private static final String DEFAULT_TENANT_ID = "tenant-001";

    private AccountTestDataFactory() {
    }

    public static BizAccount createDefaultAccount() {
        BizAccount account = new BizAccount();
        account.setTenantId(DEFAULT_TENANT_ID);
        account.setAccountCode("1001");
        account.setAccountName("库存现金");
        account.setAccountType("ASSET");
        account.setParentId(0L);
        account.setLevel(1);
        account.setDirection("DEBIT");
        account.setInitialDebit(BigDecimal.ZERO);
        account.setInitialCredit(BigDecimal.ZERO);
        account.setAuxiliaryFlag(0);
        account.setStatus(1);
        account.setRemark("测试科目");
        account.setDelFlag(0);
        return account;
    }

    public static BizAccount createAccountWithCode(String accountCode) {
        BizAccount account = createDefaultAccount();
        account.setAccountCode(accountCode);
        return account;
    }

    public static BizAccount createAccountWithType(String accountType) {
        BizAccount account = createDefaultAccount();
        account.setAccountType(accountType);
        return account;
    }

    public static BizAccount createChildAccount(Long parentId, Integer level) {
        BizAccount account = createDefaultAccount();
        account.setAccountCode("100101");
        account.setAccountName("现金-人民币");
        account.setParentId(parentId);
        account.setLevel(level);
        return account;
    }

    public static List<BizAccount> createAccountTree() {
        List<BizAccount> accounts = new ArrayList<>();
        
        BizAccount parent = createDefaultAccount();
        parent.setId(1L);
        accounts.add(parent);
        
        BizAccount child1 = createChildAccount(1L, 2);
        child1.setId(2L);
        child1.setAccountCode("100101");
        accounts.add(child1);
        
        BizAccount child2 = createChildAccount(1L, 2);
        child2.setId(3L);
        child2.setAccountCode("100102");
        child2.setAccountName("现金-美元");
        accounts.add(child2);
        
        return accounts;
    }

    public static BizAccount createAssetAccount() {
        return createAccountWithType("ASSET");
    }

    public static BizAccount createLiabilityAccount() {
        BizAccount account = createAccountWithType("LIABILITY");
        account.setDirection("CREDIT");
        return account;
    }

    public static BizAccount createEquityAccount() {
        BizAccount account = createAccountWithType("EQUITY");
        account.setDirection("CREDIT");
        return account;
    }

    public static BizAccount createExpenseAccount() {
        BizAccount account = createAccountWithType("EXPENSE");
        account.setDirection("DEBIT");
        return account;
    }

    public static BizAccount createRevenueAccount() {
        BizAccount account = createAccountWithType("REVENUE");
        account.setDirection("CREDIT");
        return account;
    }

    public static BizAccount createAccountWithInitialBalance(BigDecimal initialDebit, BigDecimal initialCredit) {
        BizAccount account = createDefaultAccount();
        account.setInitialDebit(initialDebit);
        account.setInitialCredit(initialCredit);
        return account;
    }

    public static String getDefaultTenantId() {
        return DEFAULT_TENANT_ID;
    }
}
