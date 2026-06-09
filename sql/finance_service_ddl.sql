-- ============================================================
-- finance-service DDL - 与实体类完全对齐
-- ID策略: 雪花算法(ASSIGN_ID), BIGINT非自增
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. biz_account 会计科目表
-- ----------------------------
DROP TABLE IF EXISTS `biz_account`;
CREATE TABLE `biz_account` (
  `id`              BIGINT       NOT NULL                    COMMENT 'PK,雪花ID',
  `tenant_id`       VARCHAR(32)  NOT NULL                    COMMENT '租户ID',
  `account_code`    VARCHAR(20)  NOT NULL                    COMMENT '科目编码',
  `account_name`    VARCHAR(100) NOT NULL                    COMMENT '科目名称',
  `account_type`    VARCHAR(20)  NOT NULL                    COMMENT '科目类型(asset/liability/equity/revenue/expense/profit_loss/retained_earnings/cash_operating/cash_investing/cash_financing)',
  `parent_id`       BIGINT       DEFAULT 0                   COMMENT '父科目ID',
  `level`           INT          DEFAULT 1                   COMMENT '科目级次',
  `direction`       VARCHAR(10)  DEFAULT 'debit'             COMMENT '余额方向(debit/credit)',
  `initial_debit`   DECIMAL(18,2) DEFAULT 0.00               COMMENT '期初借方',
  `initial_credit`  DECIMAL(18,2) DEFAULT 0.00               COMMENT '期初贷方',
  `auxiliary_flag`  INT          DEFAULT 0                   COMMENT '辅助核算标志(0-无)',
  `is_enabled`      TINYINT(1)   DEFAULT 1                   COMMENT '是否启用(0-禁用 1-启用)',
  `status`          INT          DEFAULT 1                   COMMENT '状态',
  `remark`          VARCHAR(200) DEFAULT NULL                COMMENT '备注',
  `create_time`     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
  `update_time`     TIMESTAMP     DEFAULT NULL                COMMENT '更新时间',
  `create_by`       VARCHAR(64)  DEFAULT NULL                COMMENT '创建人',
  `update_by`       VARCHAR(64)  DEFAULT NULL                COMMENT '更新人',
  `is_del` ENUM('NO','YES') NOT NULL DEFAULT 'NO' COMMENT '删除标记,YES:已删除,NO:正常',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_code` (`tenant_id`, `account_code`),
  KEY `idx_tenant_type` (`tenant_id`, `account_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会计科目表';

-- ----------------------------
-- 2. biz_account_auxiliary_config 科目辅助核算配置表
-- ----------------------------
DROP TABLE IF EXISTS `biz_account_auxiliary_config`;
CREATE TABLE `biz_account_auxiliary_config` (
  `id`              BIGINT       NOT NULL                    COMMENT 'PK,雪花ID',
  `tenant_id`       VARCHAR(32)  NOT NULL                    COMMENT '租户ID',
  `account_id`      BIGINT       NOT NULL                    COMMENT '科目ID',
  `auxiliary_type`  VARCHAR(20)  NOT NULL                    COMMENT '辅助核算类型',
  `required_flag`   INT          DEFAULT 1                   COMMENT '是否必填(0-否 1-是)',
  `remark`          VARCHAR(200) DEFAULT NULL                COMMENT '备注',
  `create_time`     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
  `update_time`     TIMESTAMP     DEFAULT NULL                COMMENT '更新时间',
  `create_by`       VARCHAR(64)  DEFAULT NULL                COMMENT '创建人',
  `update_by`       VARCHAR(64)  DEFAULT NULL                COMMENT '更新人',
  `is_del` ENUM('NO','YES') NOT NULL DEFAULT 'NO' COMMENT '删除标记,YES:已删除,NO:正常',
  PRIMARY KEY (`id`),
  KEY `idx_account` (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科目辅助核算配置表';

-- ----------------------------
-- 3. biz_account_balance 科目余额表
-- ----------------------------
DROP TABLE IF EXISTS `biz_account_balance`;
CREATE TABLE `biz_account_balance` (
  `id`                BIGINT       NOT NULL                    COMMENT 'PK,雪花ID',
  `tenant_id`         VARCHAR(32)  NOT NULL                    COMMENT '租户ID',
  `account_id`        BIGINT       NOT NULL                    COMMENT '科目ID',
  `period_code`       VARCHAR(6)   NOT NULL                    COMMENT '会计期间(YYYYMM)',
  `beginning_balance` DECIMAL(18,2) DEFAULT 0.00               COMMENT '期初余额',
  `period_debit`      DECIMAL(18,2) DEFAULT 0.00               COMMENT '本期借方发生额',
  `period_credit`     DECIMAL(18,2) DEFAULT 0.00               COMMENT '本期贷方发生额',
  `year_debit`        DECIMAL(18,2) DEFAULT 0.00               COMMENT '本年借方累计',
  `year_credit`       DECIMAL(18,2) DEFAULT 0.00               COMMENT '本年贷方累计',
  `ending_balance`    DECIMAL(18,2) DEFAULT 0.00               COMMENT '期末余额',
  `create_time`       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
  `update_time`       TIMESTAMP     DEFAULT NULL                COMMENT '更新时间',
  `is_del` ENUM('NO','YES') NOT NULL DEFAULT 'NO' COMMENT '删除标记,YES:已删除,NO:正常',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_period` (`account_id`, `period_code`),
  KEY `idx_tenant_period` (`tenant_id`, `period_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科目余额表';

-- ----------------------------
-- 4. biz_accounting_period 会计期间表
-- ----------------------------
DROP TABLE IF EXISTS `biz_accounting_period`;
CREATE TABLE `biz_accounting_period` (
  `id`            BIGINT       NOT NULL                    COMMENT 'PK,雪花ID',
  `tenant_id`     VARCHAR(32)  NOT NULL                    COMMENT '租户ID',
  `period_code`   VARCHAR(6)   NOT NULL                    COMMENT '期间(YYYYMM)',
  `period_name`   VARCHAR(50)  DEFAULT NULL                COMMENT '期间名称',
  `year`          INT          NOT NULL                    COMMENT '年度',
  `month`         INT          NOT NULL                    COMMENT '月份',
  `start_date`    TIMESTAMP     NOT NULL                    COMMENT '开始日期',
  `end_date`      TIMESTAMP     NOT NULL                    COMMENT '结束日期',
  `status`        INT          DEFAULT 0                   COMMENT '状态(0-未开启 1-已开启 2-已关闭)',
  `is_adjust`     INT          DEFAULT 0                   COMMENT '是否调整期(0-否 1-是)',
  `closed_at`     TIMESTAMP     DEFAULT NULL                COMMENT '关账时间',
  `closed_by`     VARCHAR(64)  DEFAULT NULL                COMMENT '关账人',
  `create_time`   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
  `update_time`   TIMESTAMP     DEFAULT NULL                COMMENT '更新时间',
  `create_by`     VARCHAR(64)  DEFAULT NULL                COMMENT '创建人',
  `update_by`     VARCHAR(64)  DEFAULT NULL                COMMENT '更新人',
  `is_del` ENUM('NO','YES') NOT NULL DEFAULT 'NO' COMMENT '删除标记,YES:已删除,NO:正常',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_period` (`tenant_id`, `period_code`),
  KEY `idx_year` (`year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会计期间表';

-- ----------------------------
-- 5. biz_accrual_expense_detail 预提费用明细表
-- ----------------------------
DROP TABLE IF EXISTS `biz_accrual_expense_detail`;
CREATE TABLE `biz_accrual_expense_detail` (
  `id`              BIGINT       NOT NULL                    COMMENT 'PK,雪花ID',
  `tenant_id`       VARCHAR(32)  NOT NULL                    COMMENT '租户ID',
  `accrual_no`      VARCHAR(50)  DEFAULT NULL                COMMENT '预提编号',
  `period_code`     VARCHAR(6)   NOT NULL                    COMMENT '计提期间(YYYYMM)',
  `accrual_type`    VARCHAR(50)  DEFAULT NULL                COMMENT '费用类型',
  `account_id`      BIGINT       DEFAULT NULL                COMMENT '科目ID',
  `accrual_amount`  DECIMAL(18,2) DEFAULT 0.00               COMMENT '本期计提金额',
  `accrual_rule_id` BIGINT       DEFAULT NULL                COMMENT '关联预提规则ID',
  `department`      VARCHAR(50)  DEFAULT NULL                COMMENT '所属部门',
  `supplier_id`     BIGINT       DEFAULT NULL                COMMENT '供应商ID',
  `voucher_id`      BIGINT       DEFAULT NULL                COMMENT '关联凭证ID',
  `remark`          VARCHAR(200) DEFAULT NULL                COMMENT '备注',
  `create_time`     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
  `update_time`     TIMESTAMP     DEFAULT NULL                COMMENT '更新时间',
  `is_del` ENUM('NO','YES') NOT NULL DEFAULT 'NO' COMMENT '删除标记,YES:已删除,NO:正常',
  PRIMARY KEY (`id`),
  KEY `idx_period` (`period_code`),
  KEY `idx_voucher` (`voucher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预提费用明细表';

-- ----------------------------
-- 6. biz_auxiliary_item 辅助核算档案表
-- ----------------------------
DROP TABLE IF EXISTS `biz_auxiliary_item`;
CREATE TABLE `biz_auxiliary_item` (
  `id`              BIGINT       NOT NULL                    COMMENT 'PK,雪花ID',
  `tenant_id`       VARCHAR(32)  NOT NULL                    COMMENT '租户ID',
  `auxiliary_type`  VARCHAR(20)  NOT NULL                    COMMENT '辅助核算类型',
  `item_code`       VARCHAR(50)  NOT NULL                    COMMENT '编码',
  `item_name`       VARCHAR(100) NOT NULL                    COMMENT '名称',
  `parent_code`     VARCHAR(50)  DEFAULT NULL                COMMENT '父级编码',
  `level`           INT          DEFAULT 1                   COMMENT '级次',
  `remark`          VARCHAR(200) DEFAULT NULL                COMMENT '备注',
  `status`          INT          DEFAULT 1                   COMMENT '状态',
  `create_time`     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
  `update_time`     TIMESTAMP     DEFAULT NULL                COMMENT '更新时间',
  `create_by`       VARCHAR(64)  DEFAULT NULL                COMMENT '创建人',
  `update_by`       VARCHAR(64)  DEFAULT NULL                COMMENT '更新人',
  `is_del` ENUM('NO','YES') NOT NULL DEFAULT 'NO' COMMENT '删除标记,YES:已删除,NO:正常',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_type_code` (`tenant_id`, `auxiliary_type`, `item_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='辅助核算档案表';

-- ----------------------------
-- 7. biz_credit_loss_detail 贷款损失准备计提明细表
-- ----------------------------
DROP TABLE IF EXISTS `biz_credit_loss_detail`;
CREATE TABLE `biz_credit_loss_detail` (
  `id`                   BIGINT       NOT NULL                    COMMENT 'PK,雪花ID',
  `tenant_id`            VARCHAR(32)  NOT NULL                    COMMENT '租户ID',
  `loan_no`              VARCHAR(50)  NOT NULL                    COMMENT '贷款合同号',
  `period_code`          VARCHAR(6)   NOT NULL                    COMMENT '计提期间(YYYYMM)',
  `customer_id`          BIGINT       DEFAULT NULL                COMMENT '客户ID',
  `risk_classification`  VARCHAR(20)  DEFAULT NULL                COMMENT '五级分类',
  `loan_balance`         DECIMAL(18,2) DEFAULT 0.00               COMMENT '贷款本金余额',
  `provision_rate`       DECIMAL(5,2) DEFAULT 0.00               COMMENT '计提比例(%)',
  `calculated_provision` DECIMAL(18,2) DEFAULT 0.00               COMMENT '应提准备金额',
  `current_provision`    DECIMAL(18,2) DEFAULT 0.00               COMMENT '当前已提准备余额',
  `adjustment_amount`    DECIMAL(18,2) DEFAULT 0.00               COMMENT '本期补提/冲回金额',
  `provision_amount`     DECIMAL(18,2) DEFAULT 0.00               COMMENT '计提金额',
  `voucher_id`           BIGINT       DEFAULT NULL                COMMENT '关联凭证ID',
  `remark`               VARCHAR(200) DEFAULT NULL                COMMENT '备注',
  `create_time`          TIMESTAMP     DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
  `update_time`          TIMESTAMP     DEFAULT NULL                COMMENT '更新时间',
  `is_del` ENUM('NO','YES') NOT NULL DEFAULT 'NO' COMMENT '删除标记,YES:已删除,NO:正常',
  PRIMARY KEY (`id`),
  KEY `idx_period` (`period_code`),
  KEY `idx_loan_no` (`loan_no`),
  KEY `idx_voucher` (`voucher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='贷款损失准备计提明细表';

-- ----------------------------
-- 8. biz_depreciation_detail 折旧明细表
-- ----------------------------
DROP TABLE IF EXISTS `biz_depreciation_detail`;
CREATE TABLE `biz_depreciation_detail` (
  `id`                       BIGINT       NOT NULL                    COMMENT 'PK,雪花ID',
  `tenant_id`                VARCHAR(32)  NOT NULL                    COMMENT '租户ID',
  `asset_id`                 BIGINT       NOT NULL                    COMMENT '固定资产ID',
  `period_code`              VARCHAR(6)   NOT NULL                    COMMENT '会计期间(YYYYMM)',
  `depreciation_amount`      DECIMAL(18,2) DEFAULT 0.00               COMMENT '本期折旧额',
  `accumulated_depreciation` DECIMAL(18,2) DEFAULT 0.00               COMMENT '累计折旧额',
  `net_value`                DECIMAL(18,2) DEFAULT 0.00               COMMENT '账面净值',
  `department`               VARCHAR(50)  DEFAULT NULL                COMMENT '使用部门',
  `expense_account_id`       BIGINT       DEFAULT NULL                COMMENT '折旧费用科目ID',
  `voucher_id`               BIGINT       DEFAULT NULL                COMMENT '关联凭证ID',
  `remark`                   VARCHAR(200) DEFAULT NULL                COMMENT '备注',
  `create_time`              TIMESTAMP     DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
  `update_time`              TIMESTAMP     DEFAULT NULL                COMMENT '更新时间',
  `is_del` ENUM('NO','YES') NOT NULL DEFAULT 'NO' COMMENT '删除标记,YES:已删除,NO:正常',
  PRIMARY KEY (`id`),
  KEY `idx_asset_period` (`asset_id`, `period_code`),
  KEY `idx_voucher` (`voucher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='折旧明细表';

-- ----------------------------
-- 9. biz_fixed_asset 固定资产卡片表
-- ----------------------------
DROP TABLE IF EXISTS `biz_fixed_asset`;
CREATE TABLE `biz_fixed_asset` (
  `id`                     BIGINT       NOT NULL                    COMMENT 'PK,雪花ID',
  `tenant_id`              VARCHAR(32)  NOT NULL                    COMMENT '租户ID',
  `asset_code`             VARCHAR(50)  DEFAULT NULL                COMMENT '资产编号',
  `asset_name`             VARCHAR(100) NOT NULL                    COMMENT '资产名称',
  `asset_category`         VARCHAR(50)  DEFAULT NULL                COMMENT '资产类别',
  `account_debit_id`       BIGINT       DEFAULT NULL                COMMENT '借方科目ID',
  `account_credit_id`      BIGINT       DEFAULT NULL                COMMENT '贷方科目ID',
  `original_value`         DECIMAL(18,2) DEFAULT 0.00               COMMENT '原值',
  `salvage_value`          DECIMAL(18,2) DEFAULT 0.00               COMMENT '残值',
  `residual_rate`          DECIMAL(5,2) DEFAULT 0.00               COMMENT '残值率(%)',
  `depreciation_value`     DECIMAL(18,2) DEFAULT 0.00               COMMENT '累计折旧',
  `net_value`              DECIMAL(18,2) DEFAULT 0.00               COMMENT '账面净值',
  `useful_life`            INT          NOT NULL                    COMMENT '使用年限(月)',
  `depreciation_method`    VARCHAR(20)  DEFAULT 'straight_line'      COMMENT '折旧方法',
  `acquisition_date`       TIMESTAMP     DEFAULT NULL                COMMENT '取得日期',
  `depreciation_start_date` TIMESTAMP    DEFAULT NULL                COMMENT '折旧开始日期',
  `department`             VARCHAR(50)  DEFAULT NULL                COMMENT '使用部门',
  `custodian`              VARCHAR(50)  DEFAULT NULL                COMMENT '保管人',
  `location`               VARCHAR(100) DEFAULT NULL                COMMENT '存放地点',
  `remark`                 VARCHAR(200) DEFAULT NULL                COMMENT '备注',
  `status`                 INT          DEFAULT 1                   COMMENT '状态(1-在役 2-已处置)',
  `create_time`            TIMESTAMP     DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
  `update_time`            TIMESTAMP     DEFAULT NULL                COMMENT '更新时间',
  `create_by`              VARCHAR(64)  DEFAULT NULL                COMMENT '创建人',
  `update_by`              VARCHAR(64)  DEFAULT NULL                COMMENT '更新人',
  `is_del` ENUM('NO','YES') NOT NULL DEFAULT 'NO' COMMENT '删除标记,YES:已删除,NO:正常',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='固定资产卡片表';

-- ----------------------------
-- 10. biz_interest_accrual_detail 应收利息计提明细表
-- ----------------------------
DROP TABLE IF EXISTS `biz_interest_accrual_detail`;
CREATE TABLE `biz_interest_accrual_detail` (
  `id`               BIGINT       NOT NULL                    COMMENT 'PK,雪花ID',
  `tenant_id`        VARCHAR(32)  NOT NULL                    COMMENT '租户ID',
  `loan_no`          VARCHAR(50)  NOT NULL                    COMMENT '贷款合同号',
  `period_code`      VARCHAR(6)   NOT NULL                    COMMENT '计提期间(YYYYMM)',
  `principal_balance` DECIMAL(18,2) DEFAULT 0.00               COMMENT '本金余额',
  `interest_rate`    DECIMAL(10,6) DEFAULT 0.000000            COMMENT '利率',
  `accrual_days`     INT          DEFAULT 0                   COMMENT '计息天数',
  `interest_amount`  DECIMAL(18,2) DEFAULT 0.00               COMMENT '本期应计利息',
  `voucher_id`       BIGINT       DEFAULT NULL                COMMENT '关联凭证ID',
  `remark`           VARCHAR(200) DEFAULT NULL                COMMENT '备注',
  `create_time`      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
  `update_time`      TIMESTAMP     DEFAULT NULL                COMMENT '更新时间',
  `is_del` ENUM('NO','YES') NOT NULL DEFAULT 'NO' COMMENT '删除标记,YES:已删除,NO:正常',
  PRIMARY KEY (`id`),
  KEY `idx_period` (`period_code`),
  KEY `idx_loan_no` (`loan_no`),
  KEY `idx_voucher` (`voucher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应收利息计提明细表';

-- ----------------------------
-- 11. biz_prepaid_amortization_detail 预付费用摊销明细表
-- ----------------------------
DROP TABLE IF EXISTS `biz_prepaid_amortization_detail`;
CREATE TABLE `biz_prepaid_amortization_detail` (
  `id`                  BIGINT       NOT NULL                    COMMENT 'PK,雪花ID',
  `tenant_id`           VARCHAR(32)  NOT NULL                    COMMENT '租户ID',
  `amortization_id`     BIGINT       NOT NULL                    COMMENT '关联摊销台账ID',
  `period_code`         VARCHAR(6)   NOT NULL                    COMMENT '摊销期间(YYYYMM)',
  `amortization_amount` DECIMAL(18,2) DEFAULT 0.00               COMMENT '本期摊销金额',
  `amortization_desc`   VARCHAR(200) DEFAULT NULL                COMMENT '摊销摘要',
  `voucher_id`          BIGINT       DEFAULT NULL                COMMENT '关联凭证ID',
  `remark`              VARCHAR(200) DEFAULT NULL                COMMENT '备注',
  `create_time`         TIMESTAMP     DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
  `update_time`         TIMESTAMP     DEFAULT NULL                COMMENT '更新时间',
  `is_del` ENUM('NO','YES') NOT NULL DEFAULT 'NO' COMMENT '删除标记,YES:已删除,NO:正常',
  PRIMARY KEY (`id`),
  KEY `idx_amortization` (`amortization_id`),
  KEY `idx_period` (`period_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预付费用摊销明细表';

-- ----------------------------
-- 12. biz_prepaid_expense_amortization 预付费用摊销台账表
-- ----------------------------
DROP TABLE IF EXISTS `biz_prepaid_expense_amortization`;
CREATE TABLE `biz_prepaid_expense_amortization` (
  `id`                   BIGINT       NOT NULL                    COMMENT 'PK,雪花ID',
  `tenant_id`            VARCHAR(32)  NOT NULL                    COMMENT '租户ID',
  `prepaid_voucher_id`   BIGINT       DEFAULT NULL                COMMENT '关联原始凭证ID',
  `amortization_no`      VARCHAR(50)  DEFAULT NULL                COMMENT '摊销编号',
  `amortization_name`    VARCHAR(100) DEFAULT NULL                COMMENT '费用描述',
  `account_id`           BIGINT       DEFAULT NULL                COMMENT '科目ID',
  `expense_account_id`   BIGINT       DEFAULT NULL                COMMENT '摊销费用科目ID',
  `total_amount`         DECIMAL(18,2) DEFAULT 0.00               COMMENT '预付总金额',
  `amortized_amount`     DECIMAL(18,2) DEFAULT 0.00               COMMENT '累计已摊销金额',
  `remaining_amount`     DECIMAL(18,2) DEFAULT 0.00               COMMENT '剩余待摊销金额',
  `total_periods`        INT          DEFAULT 0                   COMMENT '总摊销期数',
  `amortized_periods`    INT          DEFAULT 0                   COMMENT '已摊销期数',
  `start_period`         VARCHAR(6)   DEFAULT NULL                COMMENT '摊销开始期间(YYYYMM)',
  `end_period`           VARCHAR(6)   DEFAULT NULL                COMMENT '摊销结束期间(YYYYMM)',
  `start_date`           TIMESTAMP     DEFAULT NULL                COMMENT '开始日期',
  `end_date`             TIMESTAMP     DEFAULT NULL                COMMENT '结束日期',
  `amortization_method`  VARCHAR(20)  DEFAULT 'AVERAGE_MONTH'     COMMENT '摊销方式',
  `remark`               VARCHAR(200) DEFAULT NULL                COMMENT '备注',
  `status`               INT          DEFAULT 0                   COMMENT '状态(0-未开始 1-进行中 2-已完成)',
  `create_time`          TIMESTAMP     DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
  `update_time`          TIMESTAMP     DEFAULT NULL                COMMENT '更新时间',
  `create_by`            VARCHAR(64)  DEFAULT NULL                COMMENT '创建人',
  `update_by`            VARCHAR(64)  DEFAULT NULL                COMMENT '更新人',
  `is_del` ENUM('NO','YES') NOT NULL DEFAULT 'NO' COMMENT '删除标记,YES:已删除,NO:正常',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`, `status`),
  KEY `idx_prepaid_voucher` (`prepaid_voucher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预付费用摊销台账表';

-- ----------------------------
-- 13. biz_report_history 报表历史表
-- ----------------------------
DROP TABLE IF EXISTS `biz_report_history`;
CREATE TABLE `biz_report_history` (
  `id`             BIGINT       NOT NULL                    COMMENT 'PK,雪花ID',
  `tenant_id`      VARCHAR(32)  NOT NULL                    COMMENT '租户ID',
  `report_type`    VARCHAR(20)  NOT NULL                    COMMENT '报表类型',
  `period_code`    VARCHAR(6)   NOT NULL                    COMMENT '会计期间(YYYYMM)',
  `report_name`    VARCHAR(100) DEFAULT NULL                COMMENT '报表名称',
  `report_content` LONGTEXT     DEFAULT NULL                COMMENT '报表内容(JSON)',
  `report_path`    VARCHAR(200) DEFAULT NULL                COMMENT '报表文件路径',
  `format`         VARCHAR(10)  DEFAULT NULL                COMMENT '导出格式',
  `file_size`      BIGINT       DEFAULT NULL                COMMENT '文件大小',
  `remark`         VARCHAR(200) DEFAULT NULL                COMMENT '备注',
  `create_time`    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
  `update_time`    TIMESTAMP     DEFAULT NULL                COMMENT '更新时间',
  `create_by`      VARCHAR(64)  DEFAULT NULL                COMMENT '创建人',
  `is_del` ENUM('NO','YES') NOT NULL DEFAULT 'NO' COMMENT '删除标记,YES:已删除,NO:正常',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_type_period` (`tenant_id`, `report_type`, `period_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表历史表';

-- ----------------------------
-- 14. biz_voucher 凭证表
-- ----------------------------
DROP TABLE IF EXISTS `biz_voucher`;
CREATE TABLE `biz_voucher` (
  `id`               BIGINT       NOT NULL                    COMMENT 'PK,雪花ID',
  `tenant_id`        VARCHAR(32)  NOT NULL                    COMMENT '租户ID',
  `voucher_no`       VARCHAR(50)  DEFAULT NULL                COMMENT '凭证编号',
  `period_code`      VARCHAR(6)   NOT NULL                    COMMENT '会计期间(YYYYMM)',
  `voucher_type`     VARCHAR(10)  DEFAULT NULL                COMMENT '凭证类型',
  `voucher_date`     TIMESTAMP     DEFAULT NULL                COMMENT '凭证日期',
  `voucher_count`    INT          DEFAULT 0                   COMMENT '分录数',
  `attachment_count` INT          DEFAULT 0                   COMMENT '附件张数',
  `attachment_url`   VARCHAR(200) DEFAULT NULL                COMMENT '附件URL',
  `maker`            VARCHAR(64)  DEFAULT NULL                COMMENT '制单人',
  `reviewer`         VARCHAR(64)  DEFAULT NULL                COMMENT '审核人',
  `review_time`      TIMESTAMP     DEFAULT NULL                COMMENT '审核时间',
  `status`           INT          DEFAULT 0                   COMMENT '状态(0-草稿 1-已审核)',
  `remark`           VARCHAR(200) DEFAULT NULL                COMMENT '备注',
  `create_time`      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
  `update_time`      TIMESTAMP     DEFAULT NULL                COMMENT '更新时间',
  `create_by`        VARCHAR(64)  DEFAULT NULL                COMMENT '创建人',
  `update_by`        VARCHAR(64)  DEFAULT NULL                COMMENT '更新人',
  `is_del` ENUM('NO','YES') NOT NULL DEFAULT 'NO' COMMENT '删除标记,YES:已删除,NO:正常',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_period` (`tenant_id`, `period_code`),
  KEY `idx_period_status` (`period_code`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='凭证表';

-- ----------------------------
-- 15. biz_voucher_entry 凭证分录表
-- ----------------------------
DROP TABLE IF EXISTS `biz_voucher_entry`;
CREATE TABLE `biz_voucher_entry` (
  `id`               BIGINT       NOT NULL                    COMMENT 'PK,雪花ID',
  `tenant_id`        VARCHAR(32)  NOT NULL                    COMMENT '租户ID',
  `voucher_id`       BIGINT       NOT NULL                    COMMENT '关联凭证ID',
  `entry_seq`        INT          DEFAULT 0                   COMMENT '分录序号',
  `summary`          VARCHAR(200) DEFAULT NULL                COMMENT '摘要',
  `account_id`       BIGINT       NOT NULL                    COMMENT '科目ID',
  `debit_amount`     DECIMAL(18,2) DEFAULT 0.00               COMMENT '借方金额',
  `credit_amount`    DECIMAL(18,2) DEFAULT 0.00               COMMENT '贷方金额',
  `auxiliary1_type`  VARCHAR(20)  DEFAULT NULL                COMMENT '辅助核算1类型',
  `auxiliary1_value` VARCHAR(50)  DEFAULT NULL                COMMENT '辅助核算1值',
  `auxiliary2_type`  VARCHAR(20)  DEFAULT NULL                COMMENT '辅助核算2类型',
  `auxiliary2_value` VARCHAR(50)  DEFAULT NULL                COMMENT '辅助核算2值',
  `auxiliary3_type`  VARCHAR(20)  DEFAULT NULL                COMMENT '辅助核算3类型',
  `auxiliary3_value` VARCHAR(50)  DEFAULT NULL                COMMENT '辅助核算3值',
  `auxiliary4_type`  VARCHAR(20)  DEFAULT NULL                COMMENT '辅助核算4类型',
  `auxiliary4_value` VARCHAR(50)  DEFAULT NULL                COMMENT '辅助核算4值',
  `create_time`      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
  `update_time`      TIMESTAMP     DEFAULT NULL                COMMENT '更新时间',
  `is_del` ENUM('NO','YES') NOT NULL DEFAULT 'NO' COMMENT '删除标记,YES:已删除,NO:正常',
  PRIMARY KEY (`id`),
  KEY `idx_voucher` (`voucher_id`),
  KEY `idx_account` (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='凭证分录表';

-- ----------------------------
-- 16. system_config 系统配置表
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `id`           BIGINT       NOT NULL                    COMMENT 'PK,雪花ID',
  `tenant_id`    VARCHAR(32)  NOT NULL                    COMMENT '租户ID',
  `config_key`   VARCHAR(50)  NOT NULL                    COMMENT '参数键',
  `config_value` VARCHAR(200) NOT NULL                    COMMENT '参数值',
  `config_name`  VARCHAR(100) DEFAULT NULL                COMMENT '参数名称',
  `remark`       VARCHAR(200) DEFAULT NULL                COMMENT '备注',
  `create_time`  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
  `update_time`  TIMESTAMP     DEFAULT NULL                COMMENT '更新时间',
  `create_by`    VARCHAR(64)  DEFAULT NULL                COMMENT '创建人',
  `update_by`    VARCHAR(64)  DEFAULT NULL                COMMENT '更新人',
  `is_del` ENUM('NO','YES') NOT NULL DEFAULT 'NO' COMMENT '删除标记,YES:已删除,NO:正常',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_key` (`tenant_id`, `config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

SET FOREIGN_KEY_CHECKS = 1;