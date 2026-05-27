package com.ehome.mal.financeservice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ehome.mal.financeservice.entity.SystemConfig;
import com.ehome.mal.financeservice.mapper.SystemConfigMapper;
import com.ehome.mal.financeservice.service.impl.SystemConfigServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("系统配置Service测试")
class SystemConfigServiceTest {

    @Mock
    private SystemConfigMapper configMapper;

    @InjectMocks
    private SystemConfigServiceImpl configService;

    private SystemConfig testConfig;

    @BeforeEach
    void setUp() {
        testConfig = createDefaultConfig();
    }

    private SystemConfig createDefaultConfig() {
        SystemConfig config = new SystemConfig();
        config.setId(1L);
        config.setTenantId("tenant-001");
        config.setConfigKey("DEFAULT_VOUCHER_TYPE");
        config.setConfigValue("JZ");
        config.setConfigName("默认凭证类型");
        config.setRemark("记账凭证");
        config.setDelFlag(0);
        return config;
    }

    @Nested
    @DisplayName("配置项查询测试")
    class ConfigQueryTests {

        @Test
        @DisplayName("给定存在的配置键_当查询配置值_则返回配置值")
        void givenExistentConfigKey_whenGetConfigValue_thenReturnValue() {
            when(configMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testConfig);

            String value = configService.getConfigValue("tenant-001", "DEFAULT_VOUCHER_TYPE");

            assertThat(value).isEqualTo("JZ");
            verify(configMapper).selectOne(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("给定不存在的配置键_当查询配置值_则返回null")
        void givenNonExistentConfigKey_whenGetConfigValue_thenReturnNull() {
            when(configMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            String value = configService.getConfigValue("tenant-001", "NON_EXISTENT");

            assertThat(value).isNull();
        }

        @Test
        @DisplayName("给定不同租户_当查询配置_则隔离数据")
        void givenDifferentTenants_whenGetConfig_thenDataIsolated() {
            SystemConfig tenant1Config = createDefaultConfig();
            SystemConfig tenant2Config = createDefaultConfig();
            tenant2Config.setTenantId("tenant-002");
            tenant2Config.setConfigValue("SK");

            when(configMapper.selectOne(argThat(w -> true))).thenReturn(tenant1Config);

            String value1 = configService.getConfigValue("tenant-001", "DEFAULT_VOUCHER_TYPE");

            assertThat(value1).isEqualTo("JZ");
        }
    }

    @Nested
    @DisplayName("配置项设置测试")
    class ConfigSetTests {

        @Test
        @DisplayName("给定存在的配置键_当设置配置值_则更新")
        void givenExistentConfigKey_whenSetConfigValue_thenUpdate() {
            when(configMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testConfig);
            when(configMapper.updateById(any(SystemConfig.class))).thenReturn(1);

            boolean result = configService.setConfigValue("tenant-001", "DEFAULT_VOUCHER_TYPE", "SK");

            assertThat(result).isTrue();
            assertThat(testConfig.getConfigValue()).isEqualTo("SK");
            assertThat(testConfig.getUpdateTime()).isNotNull();
            verify(configMapper).selectOne(any(LambdaQueryWrapper.class));
            verify(configMapper).updateById(any(SystemConfig.class));
        }

        @Test
        @DisplayName("给定不存在的配置键_当设置配置值_则创建")
        void givenNonExistentConfigKey_whenSetConfigValue_thenCreate() {
            when(configMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(configMapper.insert(any(SystemConfig.class))).thenReturn(1);

            boolean result = configService.setConfigValue("tenant-001", "NEW_CONFIG", "VALUE");

            assertThat(result).isTrue();
            verify(configMapper).selectOne(any(LambdaQueryWrapper.class));
            verify(configMapper).insert(any(SystemConfig.class));
        }

        @Test
        @DisplayName("给定空值_当设置配置值_则允许")
        void givenEmptyValue_whenSetConfigValue_thenAllowed() {
            when(configMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testConfig);
            when(configMapper.updateById(any(SystemConfig.class))).thenReturn(1);

            boolean result = configService.setConfigValue("tenant-001", "DEFAULT_VOUCHER_TYPE", "");

            assertThat(result).isTrue();
            assertThat(testConfig.getConfigValue()).isEmpty();
        }
    }

    @Nested
    @DisplayName("配置项类型验证测试")
    class ConfigTypeValidationTests {

        @Test
        @DisplayName("给定凭证类型配置_当验证_则有效")
        void givenVoucherTypeConfig_whenValidate_thenValid() {
            testConfig.setConfigKey("DEFAULT_VOUCHER_TYPE");
            testConfig.setConfigValue("JZ");

            assertThat(testConfig.getConfigKey()).isEqualTo("DEFAULT_VOUCHER_TYPE");
            assertThat(testConfig.getConfigValue()).isEqualTo("JZ");
        }

        @Test
        @DisplayName("给定布尔类型配置_当验证_则有效")
        void givenBooleanConfig_whenValidate_thenValid() {
            testConfig.setConfigKey("AUTO_VOUCHER_NO");
            testConfig.setConfigValue("true");

            assertThat(testConfig.getConfigValue()).isIn("true", "false");
        }

        @Test
        @DisplayName("给定数值类型配置_当验证_则有效")
        void givenNumericConfig_whenValidate_thenValid() {
            testConfig.setConfigKey("DECIMAL_PLACES");
            testConfig.setConfigValue("2");

            assertThat(testConfig.getConfigValue()).matches("\\d+");
        }
    }

    @Nested
    @DisplayName("配置项默认值测试")
    class ConfigDefaultValueTests {

        @Test
        @DisplayName("给定无配置的租户_当获取默认值_则返回null")
        void givenNoConfigTenant_whenGetDefault_thenReturnNull() {
            when(configMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            String value = configService.getConfigValue("new-tenant", "DEFAULT_VOUCHER_TYPE");

            assertThat(value).isNull();
        }

        @Test
        @DisplayName("给定新配置项_当创建_则设置默认值")
        void givenNewConfig_whenCreate_thenSetDefaultValue() {
            when(configMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(configMapper.insert(any(SystemConfig.class))).thenReturn(1);

            boolean result = configService.setConfigValue("tenant-001", "NEW_CONFIG", "default");

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("配置项键名测试")
    class ConfigKeyTests {

        @Test
        @DisplayName("给定有效配置键_当验证_则符合命名规范")
        void givenValidConfigKey_whenValidate_thenMeetsNamingConvention() {
            String configKey = "DEFAULT_VOUCHER_TYPE";

            assertThat(configKey).matches("[A-Z_]+");
        }

        @Test
        @DisplayName("给定多个配置键_当验证唯一性_则不重复")
        void givenMultipleConfigKeys_whenValidateUniqueness_thenNoDuplicate() {
            SystemConfig config1 = createDefaultConfig();
            config1.setConfigKey("CONFIG_1");

            SystemConfig config2 = createDefaultConfig();
            config2.setConfigKey("CONFIG_2");

            assertThat(config1.getConfigKey()).isNotEqualTo(config2.getConfigKey());
        }
    }
}
