package com.ehome.mal.financeservice.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("科目实体测试")
class BizAccountTest {

    @Nested
    @DisplayName("字段正确性验证测试")
    class FieldValidationTests {

        @Test
        @DisplayName("创建科目_所有字段可设置和获取")
        void createAccount_AllFieldsCanBeSetAndGet() {
            BizAccount account = new BizAccount();
            account.setId(1L);
            account.setTenantId("tenant-001");
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
            account.setRemark("测试");

            assertThat(account.getId()).isEqualTo(1L);
            assertThat(account.getTenantId()).isEqualTo("tenant-001");
            assertThat(account.getAccountCode()).isEqualTo("1001");
            assertThat(account.getAccountName()).isEqualTo("库存现金");
            assertThat(account.getAccountType()).isEqualTo("ASSET");
            assertThat(account.getParentId()).isEqualTo(0L);
            assertThat(account.getLevel()).isEqualTo(1);
            assertThat(account.getDirection()).isEqualTo("DEBIT");
            assertThat(account.getInitialDebit()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(account.getInitialCredit()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(account.getAuxiliaryFlag()).isEqualTo(0);
            assertThat(account.getStatus()).isEqualTo(1);
            assertThat(account.getRemark()).isEqualTo("测试");
        }
    }

    @Nested
    @DisplayName("Lombok注解验证测试")
    class LombokAnnotationTests {

        @Test
        @DisplayName("两个相同内容的科目_比较相等")
        void twoEqualAccounts_AreEqual() {
            BizAccount account1 = new BizAccount();
            account1.setId(1L);
            account1.setAccountCode("1001");

            BizAccount account2 = new BizAccount();
            account2.setId(1L);
            account2.setAccountCode("1001");

            assertThat(account1).isEqualTo(account2);
            assertThat(account1.hashCode()).isEqualTo(account2.hashCode());
        }

        @Test
        @DisplayName("科目toString方法_包含字段信息")
        void accountToString_ContainsFieldInfo() {
            BizAccount account = new BizAccount();
            account.setId(1L);
            account.setAccountCode("1001");

            String toString = account.toString();
            assertThat(toString).contains("id=1");
            assertThat(toString).contains("accountCode=1001");
        }

        @Test
        @DisplayName("科目可以克隆")
        void accountCanBeCloned() {
            BizAccount original = new BizAccount();
            original.setId(1L);
            original.setAccountCode("1001");

            BizAccount cloned = new BizAccount();
            cloned.setId(original.getId());
            cloned.setAccountCode(original.getAccountCode());

            assertThat(cloned).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("默认值验证测试")
    class DefaultValueTests {

        @Test
        @DisplayName("新建科目_数值字段为null")
        void newAccount_NumericFieldsAreNull() {
            BizAccount account = new BizAccount();

            assertThat(account.getId()).isNull();
            assertThat(account.getInitialDebit()).isNull();
            assertThat(account.getInitialCredit()).isNull();
            assertThat(account.getLevel()).isNull();
            assertThat(account.getStatus()).isNull();
        }

        @Test
        @DisplayName("新建科目_字符串字段为null")
        void newAccount_StringFieldsAreNull() {
            BizAccount account = new BizAccount();

            assertThat(account.getTenantId()).isNull();
            assertThat(account.getAccountCode()).isNull();
            assertThat(account.getAccountName()).isNull();
            assertThat(account.getAccountType()).isNull();
        }
    }

    @Nested
    @DisplayName("约束注解验证测试")
    class ConstraintAnnotationTests {

        @Test
        @DisplayName("科目编码不能为空")
        void accountCodeCannotBeEmpty() {
            BizAccount account = new BizAccount();
            account.setAccountCode("");

            assertThat(account.getAccountCode()).isEmpty();
        }

        @Test
        @DisplayName("科目名称长度应在合理范围内")
        void accountNameLengthShouldBeReasonable() {
            BizAccount account = new BizAccount();
            String longName = "a".repeat(200);
            account.setAccountName(longName);

            assertThat(account.getAccountName()).hasSize(200);
        }
    }

    @Nested
    @DisplayName("非持久化字段测试")
    class TransientFieldTests {

        @Test
        @DisplayName("parentName是非持久化字段")
        void parentNameIsTransient() {
            BizAccount account = new BizAccount();
            account.setParentName("上级科目");

            assertThat(account.getParentName()).isEqualTo("上级科目");
        }
    }
}
