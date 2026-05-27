package com.ehome.mal.financeservice.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("凭证实体测试")
class BizVoucherTest {

    @Nested
    @DisplayName("字段正确性验证测试")
    class FieldValidationTests {

        @Test
        @DisplayName("创建凭证_所有字段可设置和获取")
        void createVoucher_AllFieldsCanBeSetAndGet() {
            BizVoucher voucher = new BizVoucher();
            voucher.setId(1L);
            voucher.setTenantId("tenant-001");
            voucher.setVoucherNo("202401-JZ-0001");
            voucher.setPeriodCode("202401");
            voucher.setVoucherType("JZ");
            voucher.setStatus(0);
            voucher.setMaker("user-001");
            voucher.setReviewer("auditor-001");

            assertThat(voucher.getId()).isEqualTo(1L);
            assertThat(voucher.getTenantId()).isEqualTo("tenant-001");
            assertThat(voucher.getVoucherNo()).isEqualTo("202401-JZ-0001");
            assertThat(voucher.getPeriodCode()).isEqualTo("202401");
            assertThat(voucher.getVoucherType()).isEqualTo("JZ");
            assertThat(voucher.getStatus()).isEqualTo(0);
            assertThat(voucher.getMaker()).isEqualTo("user-001");
            assertThat(voucher.getReviewer()).isEqualTo("auditor-001");
        }
    }

    @Nested
    @DisplayName("凭证状态测试")
    class VoucherStatusTests {

        @Test
        @DisplayName("未审核状态为0")
        void unauditedStatusIsZero() {
            BizVoucher voucher = new BizVoucher();
            voucher.setStatus(0);

            assertThat(voucher.getStatus()).isEqualTo(0);
        }

        @Test
        @DisplayName("已审核状态为1")
        void auditedStatusIsOne() {
            BizVoucher voucher = new BizVoucher();
            voucher.setStatus(1);

            assertThat(voucher.getStatus()).isEqualTo(1);
        }

        @Test
        @DisplayName("已过账状态为2")
        void postedStatusIsTwo() {
            BizVoucher voucher = new BizVoucher();
            voucher.setStatus(2);

            assertThat(voucher.getStatus()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("凭证号格式测试")
    class VoucherNoFormatTests {

        @Test
        @DisplayName("凭证号格式正确")
        void voucherNoFormatIsCorrect() {
            BizVoucher voucher = new BizVoucher();
            voucher.setVoucherNo("202401-JZ-0001");

            assertThat(voucher.getVoucherNo()).matches("\\d{6}-[A-Z]{2}-\\d{4}");
        }

        @Test
        @DisplayName("凭证号包含期间编码")
        void voucherNoContainsPeriodCode() {
            BizVoucher voucher = new BizVoucher();
            voucher.setVoucherNo("202401-JZ-0001");
            voucher.setPeriodCode("202401");

            assertThat(voucher.getVoucherNo()).startsWith(voucher.getPeriodCode());
        }
    }

    @Nested
    @DisplayName("Lombok注解验证测试")
    class LombokAnnotationTests {

        @Test
        @DisplayName("两个相同内容的凭证_比较相等")
        void twoEqualVouchers_AreEqual() {
            BizVoucher voucher1 = new BizVoucher();
            voucher1.setId(1L);
            voucher1.setVoucherNo("202401-JZ-0001");

            BizVoucher voucher2 = new BizVoucher();
            voucher2.setId(1L);
            voucher2.setVoucherNo("202401-JZ-0001");

            assertThat(voucher1).isEqualTo(voucher2);
        }

        @Test
        @DisplayName("凭证toString方法_包含字段信息")
        void voucherToString_ContainsFieldInfo() {
            BizVoucher voucher = new BizVoucher();
            voucher.setId(1L);
            voucher.setVoucherNo("202401-JZ-0001");

            String toString = voucher.toString();
            assertThat(toString).contains("id=1");
            assertThat(toString).contains("voucherNo=202401-JZ-0001");
        }
    }
}
