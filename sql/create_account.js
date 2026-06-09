const fs = require('fs');
const mysql = require('mysql2/promise');

(async () => {
  const c = await mysql.createConnection({
    host: '127.0.0.1', port: 3306,
    user: 'root', password: '001Sea001!',
    database: 'finance_service'
  });

  await c.query('SET FOREIGN_KEY_CHECKS = 0');
  await c.query('DROP TABLE IF EXISTS biz_account');

  const sql = `CREATE TABLE \`biz_account\` (
    \`id\` BIGINT NOT NULL COMMENT 'PK',
    \`tenant_id\` VARCHAR(32) NOT NULL COMMENT '租户ID',
    \`account_code\` VARCHAR(20) NOT NULL COMMENT '科目编码',
    \`account_name\` VARCHAR(100) NOT NULL COMMENT '科目名称',
    \`account_type\` VARCHAR(20) NOT NULL COMMENT '科目类型',
    \`parent_id\` BIGINT DEFAULT 0 COMMENT '父科目ID',
    \`level\` INT DEFAULT 1 COMMENT '级次',
    \`direction\` VARCHAR(4) DEFAULT 'debit' COMMENT '方向',
    \`initial_debit\` DECIMAL(18,2) DEFAULT 0.00,
    \`initial_credit\` DECIMAL(18,2) DEFAULT 0.00,
    \`auxiliary_flag\` INT DEFAULT 0,
    \`is_enabled\` TINYINT(1) DEFAULT 1,
    \`status\` INT DEFAULT 1,
    \`remark\` VARCHAR(200) DEFAULT NULL,
    \`create_time\` DATETIME DEFAULT CURRENT_TIMESTAMP,
    \`update_time\` DATETIME DEFAULT NULL,
    \`create_by\` VARCHAR(64) DEFAULT NULL,
    \`update_by\` VARCHAR(64) DEFAULT NULL,
    \`del_flag\` TINYINT(1) DEFAULT 0,
    PRIMARY KEY (\`id\`),
    UNIQUE KEY \`uk_tenant_code\` (\`tenant_id\`, \`account_code\`),
    KEY \`idx_tenant_type\` (\`tenant_id\`, \`account_type\`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会计科目表'`;

  try {
    await c.query(sql);
    console.log('biz_account created OK');
  } catch (e) {
    console.error('FAIL:', e.code, e.message);
  }

  const [cols] = await c.query('SHOW COLUMNS FROM biz_account');
  console.log('Columns:', cols.map(c => c.Field).join(', '));

  await c.end();
})();