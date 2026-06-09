const mysql = require('mysql2/promise');

(async () => {
  const c = await mysql.createConnection({
    host: '127.0.0.1', port: 3306,
    user: 'root', password: '001Sea001!',
    database: 'finance_service'
  });

  const [tables] = await c.query('SHOW TABLES');
  const tNames = tables.map(t => Object.values(t)[0]);

  for (const tn of tNames) {
    const [cols] = await c.query('SHOW FULL COLUMNS FROM ' + tn);
    for (const col of cols) {
      if (col.Field === 'create_time' && col.Type.startsWith('timestamp') && col.Default === null) {
        try {
          await c.query(`ALTER TABLE \`${tn}\` MODIFY COLUMN \`${col.Field}\` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '${col.Comment.replace(/'/g, "\\'")}'`);
          console.log('OK:', tn + '.create_time');
        } catch (e) {
          console.error('FAIL:', tn + '.create_time', e.message.substring(0, 100));
        }
      }
    }
  }

  await c.end();
})();