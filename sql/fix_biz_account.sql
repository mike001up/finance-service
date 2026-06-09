SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `biz_account`;
CREATE TABLE `biz_account` (
  `id`              BIGINT       NOT NULL                    COMMENT 'PK,雪花ID',
  `tenant_id`       VARCHAR(32)  NOT NULL                    COMMENT '租户ID',
  `account_code`    VARCHAR(20)  NOT NULL                    COMMENT '科目编码',
  `account_name`    VARCHAR(100) NOT NULL                    COMMENT '科目名称',
  `account_type`    VARCHAR(20)  NOT NULL                    COMMENT '科目类型',
  `parent_id`       BIGINT       DEFAULT 0                   COMMENT '父科目ID',
  `level`           INT          DEFAULT 1                   COMMENT '科目级次',
  `direction`       VARCHAR(4)   DEFAULT 'debit'             COMMENT '余额方向',
  `initial_debit`   DECIMAL(18,2) DEFAULT 0.00               COMMENT '期初借方',
  `initial_credit`  DECIMAL(18,2) DEFAULT 0.00               COMMENT '期初贷方',
  `auxiliary_flag`  INT          DEFAULT 0                   COMMENT '辅助核算标志',
  `is_enabled`      TINYINT(1)   DEFAULT 1                   COMMENT '是否启用',
  `status`          INT          DEFAULT 1                   COMMENT '状态',
  `remark`          VARCHAR(200) DEFAULT NULL                COMMENT '备注',
  `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
  `update_time`     DATETIME     DEFAULT NULL                COMMENT '更新时间',
  `create_by`       VARCHAR(64)  DEFAULT NULL                COMMENT '创建人',
  `update_by`       VARCHAR(64)  DEFAULT NULL                COMMENT '更新人',
  `del_flag`        TINYINT(1)   DEFAULT 0                   COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_code` (`tenant_id`, `account_code`),
  KEY `idx_tenant_type` (`tenant_id`, `account_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会计科目表';
SET FOREIGN_KEY_CHECKS = 1;