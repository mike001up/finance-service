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
      if (col.Type.toLowerCase().startsWith('datetime')) {
        const nullPart = col.Null === 'YES' ? 'NULL' : 'NOT NULL';
        const defaultPart = col.Default !== null ? `DEFAULT '${col.Default}'` : (col.Null === 'YES' ? 'DEFAULT NULL' : '');
        const commentPart = col.Comment ? `COMMENT '${col.Comment.replace(/'/g, "\\'")}'` : '';
        const sql = `ALTER TABLE \`${tn}\` MODIFY COLUMN \`${col.Field}\` TIMESTAMP ${nullPart} ${defaultPart} ${commentPart}`.trim();
        try {
          await c.query(sql);
          console.log('OK:', tn + '.' + col.Field);
        } catch (e) {
          // Try without default for CURRENT_TIMESTAMP issues
          try {
            const sql2 = `ALTER TABLE \`${tn}\` MODIFY COLUMN \`${col.Field}\` TIMESTAMP ${nullPart} ${commentPart}`.trim();
            await c.query(sql2);
            console.log('OK(no-default):', tn + '.' + col.Field);
          } catch (e2) {
            console.error('FAIL:', tn + '.' + col.Field, e2.message.substring(0, 100));
          }
        }
      }
    }
  }

  await c.end();
})();