package com.ehome.mal.financeservice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ehome.mal.financeservice.entity.BizVoucher;
import com.ehome.mal.financeservice.fixture.VoucherTestDataFactory;
import com.ehome.mal.financeservice.mapper.BizVoucherMapper;
import com.ehome.mal.financeservice.service.impl.BizVoucherServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("凭证管理Service测试")
class BizVoucherServiceTest {

    @Mock
    private BizVoucherMapper voucherMapper;

    @InjectMocks
    private BizVoucherServiceImpl voucherService;

    private BizVoucher testVoucher;

    @BeforeEach
    void setUp() {
        testVoucher = VoucherTestDataFactory.createDefaultVoucher();
    }

    @Nested
    @DisplayName("凭证号自动生成测试")
    class VoucherNoGenerationTests {

        @Test
        @DisplayName("给定期间无凭证_当生成凭证号_则为0001号")
        void givenNoVoucherInPeriod_whenGenerateNo_then0001() {
            when(voucherMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            String voucherNo = voucherService.generateVoucherNo("tenant-001", "202401", "JZ");

            assertThat(voucherNo).isEqualTo("202401-JZ-0001");
            verify(voucherMapper).selectOne(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("给定期间有凭证_当生成凭证号_则序号递增")
        void givenExistingVoucherInPeriod_whenGenerateNo_thenIncrement() {
            BizVoucher lastVoucher = new BizVoucher();
            lastVoucher.setVoucherNo("202401-JZ-0005");
            when(voucherMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(lastVoucher);

            String voucherNo = voucherService.generateVoucherNo("tenant-001", "202401", "JZ");

            assertThat(voucherNo).isEqualTo("202401-JZ-0006");
        }

        @Test
        @DisplayName("给定不同凭证类型_当生成凭证号_则独立序号")
        void givenDifferentVoucherType_whenGenerateNo_thenIndependentSequence() {
            when(voucherMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            String jzNo = voucherService.generateVoucherNo("tenant-001", "202401", "JZ");
            String skNo = voucherService.generateVoucherNo("tenant-001", "202401", "SK");

            assertThat(jzNo).isEqualTo("202401-JZ-0001");
            assertThat(skNo).isEqualTo("202401-SK-0001");
        }

        @Test
        @DisplayName("给定不同期间_当生成凭证号_则独立序号")
        void givenDifferentPeriod_whenGenerateNo_thenIndependentSequence() {
            when(voucherMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            String period1No = voucherService.generateVoucherNo("tenant-001", "202401", "JZ");
            String period2No = voucherService.generateVoucherNo("tenant-001", "202402", "JZ");

            assertThat(period1No).isEqualTo("202401-JZ-0001");
            assertThat(period2No).isEqualTo("202402-JZ-0001");
        }

        @Test
        @DisplayName("给定最大序号_当生成凭证号_则正确递增")
        void givenMaxSequence_whenGenerateNo_thenCorrectIncrement() {
            BizVoucher lastVoucher = new BizVoucher();
            lastVoucher.setVoucherNo("202401-JZ-9999");
            when(voucherMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(lastVoucher);

            String voucherNo = voucherService.generateVoucherNo("tenant-001", "202401", "JZ");

            assertThat(voucherNo).isEqualTo("202401-JZ-10000");
        }
    }

    @Nested
    @DisplayName("凭证创建测试")
    class CreateVoucherTests {

        @Test
        @DisplayName("给定有效凭证_当创建凭证_则成功且状态为未审核")
        void givenValidVoucher_whenCreate_thenSuccessWithUnauditedStatus() {
            when(voucherMapper.insert(any(BizVoucher.class))).thenReturn(1);

            boolean result = voucherService.saveVoucher(testVoucher);

            assertThat(result).isTrue();
            assertThat(testVoucher.getStatus()).isEqualTo(0);
            assertThat(testVoucher.getCreateTime()).isNotNull();
            assertThat(testVoucher.getUpdateTime()).isNotNull();
            assertThat(testVoucher.getDelFlag()).isEqualTo(0);
            verify(voucherMapper).insert(any(BizVoucher.class));
        }

        @Test
        @DisplayName("给定记账凭证_当创建凭证_则类型为JZ")
        void givenJournalVoucher_whenCreate_thenTypeIsJZ() {
            BizVoucher jzVoucher = VoucherTestDataFactory.createVoucherWithType("JZ");
            when(voucherMapper.insert(any(BizVoucher.class))).thenReturn(1);

            boolean result = voucherService.saveVoucher(jzVoucher);

            assertThat(result).isTrue();
            assertThat(jzVoucher.getVoucherType()).isEqualTo("JZ");
        }

        @Test
        @DisplayName("给定收款凭证_当创建凭证_则类型为SK")
        void givenReceiptVoucher_whenCreate_thenTypeIsSK() {
            BizVoucher skVoucher = VoucherTestDataFactory.createVoucherWithType("SK");
            when(voucherMapper.insert(any(BizVoucher.class))).thenReturn(1);

            boolean result = voucherService.saveVoucher(skVoucher);

            assertThat(result).isTrue();
            assertThat(skVoucher.getVoucherType()).isEqualTo("SK");
        }
    }

    @Nested
    @DisplayName("凭证审核测试")
    class AuditVoucherTests {

        @Test
        @DisplayName("给定未审核凭证_当审核凭证_则成功")
        void givenUnauditedVoucher_whenAudit_thenSuccess() {
            testVoucher.setId(1L);
            testVoucher.setStatus(0);
            when(voucherMapper.selectById(1L)).thenReturn(testVoucher);
            when(voucherMapper.updateById(any(BizVoucher.class))).thenReturn(1);

            boolean result = voucherService.reviewVoucher(1L, "auditor-001");

            assertThat(result).isTrue();
            assertThat(testVoucher.getStatus()).isEqualTo(1);
            assertThat(testVoucher.getReviewer()).isEqualTo("auditor-001");
            assertThat(testVoucher.getReviewTime()).isNotNull();
            verify(voucherMapper).selectById(1L);
            verify(voucherMapper).updateById(any(BizVoucher.class));
        }

        @Test
        @DisplayName("给定已审核凭证_当审核凭证_则失败")
        void givenAuditedVoucher_whenAudit_thenReturnFalse() {
            testVoucher.setId(1L);
            testVoucher.setStatus(1);
            when(voucherMapper.selectById(1L)).thenReturn(testVoucher);

            boolean result = voucherService.reviewVoucher(1L, "auditor-001");

            assertThat(result).isFalse();
            verify(voucherMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("给定不存在的凭证_当审核凭证_则失败")
        void givenNonExistentVoucher_whenAudit_thenReturnFalse() {
            when(voucherMapper.selectById(999L)).thenReturn(null);

            boolean result = voucherService.reviewVoucher(999L, "auditor-001");

            assertThat(result).isFalse();
            verify(voucherMapper, never()).updateById(any());
        }
    }

    @Nested
    @DisplayName("凭证反审核测试")
    class UnauditVoucherTests {

        @Test
        @DisplayName("给定已审核凭证_当反审核凭证_则成功")
        void givenAuditedVoucher_whenUnaudit_thenSuccess() {
            testVoucher.setId(1L);
            testVoucher.setStatus(1);
            testVoucher.setReviewer("auditor-001");
            when(voucherMapper.selectById(1L)).thenReturn(testVoucher);
            when(voucherMapper.updateById(any(BizVoucher.class))).thenReturn(1);

            boolean result = voucherService.unreviewVoucher(1L);

            assertThat(result).isTrue();
            assertThat(testVoucher.getStatus()).isEqualTo(0);
            assertThat(testVoucher.getReviewer()).isNull();
            assertThat(testVoucher.getReviewTime()).isNull();
            verify(voucherMapper).selectById(1L);
            verify(voucherMapper).updateById(any(BizVoucher.class));
        }

        @Test
        @DisplayName("给定未审核凭证_当反审核凭证_则失败")
        void givenUnauditedVoucher_whenUnaudit_thenReturnFalse() {
            testVoucher.setId(1L);
            testVoucher.setStatus(0);
            when(voucherMapper.selectById(1L)).thenReturn(testVoucher);

            boolean result = voucherService.unreviewVoucher(1L);

            assertThat(result).isFalse();
            verify(voucherMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("给定不存在的凭证_当反审核凭证_则失败")
        void givenNonExistentVoucher_whenUnaudit_thenReturnFalse() {
            when(voucherMapper.selectById(999L)).thenReturn(null);

            boolean result = voucherService.unreviewVoucher(999L);

            assertThat(result).isFalse();
            verify(voucherMapper, never()).updateById(any());
        }
    }

    @Nested
    @DisplayName("凭证查询测试")
    class QueryVoucherTests {

        @Test
        @DisplayName("给定存在的ID_当查询凭证_则返回凭证")
        void givenExistentId_whenGetById_thenReturnVoucher() {
            testVoucher.setId(1L);
            when(voucherMapper.selectById(1L)).thenReturn(testVoucher);

            BizVoucher result = voucherService.getById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getVoucherNo()).isEqualTo("202401-JZ-0001");
        }

        @Test
        @DisplayName("给定不存在的ID_当查询凭证_则返回null")
        void givenNonExistentId_whenGetById_thenReturnNull() {
            when(voucherMapper.selectById(999L)).thenReturn(null);

            BizVoucher result = voucherService.getById(999L);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("凭证状态流转测试")
    class VoucherStatusFlowTests {

        @Test
        @DisplayName("给定凭证状态流转_当验证状态机_则正确")
        void givenVoucherStatusFlow_whenValidateStateMachine_thenCorrect() {
            assertThat(testVoucher.getStatus()).isEqualTo(0);

            testVoucher.setStatus(1);
            assertThat(testVoucher.getStatus()).isEqualTo(1);

            testVoucher.setStatus(0);
            assertThat(testVoucher.getStatus()).isEqualTo(0);
        }

        @Test
        @DisplayName("给定已审核凭证_当验证审核信息_则完整")
        void givenAuditedVoucher_whenValidateAuditInfo_thenComplete() {
            BizVoucher auditedVoucher = VoucherTestDataFactory.createAuditedVoucher();

            assertThat(auditedVoucher.getStatus()).isEqualTo(1);
            assertThat(auditedVoucher.getReviewer()).isNotNull();
            assertThat(auditedVoucher.getReviewTime()).isNotNull();
        }
    }

    @Nested
    @DisplayName("凭证多条件查询测试")
    class MultiConditionQueryTests {

        @Test
        @DisplayName("给定租户ID和期间_当查询凭证列表_则返回对应凭证")
        void givenTenantIdAndPeriod_whenListVouchers_thenReturnMatchedVouchers() {
            when(voucherMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.singletonList(testVoucher));

            var result = voucherService.list(any(LambdaQueryWrapper.class));

            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
        }
    }
}
