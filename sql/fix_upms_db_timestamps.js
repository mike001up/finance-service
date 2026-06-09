const mysql = require('mysql2/promise');

(async () => {
  const c = await mysql.createConnection({
    host: '127.0.0.1', port: 3306,
    user: 'root', password: '001Sea001!',
    database: 'pig_upms'
  });

  const [tables] = await c.query('SHOW TABLES');
  const tNames = tables.map(t => Object.values(t)[0]);

  for (const tn of tNames) {
    const [cols] = await c.query('SHOW FULL COLUMNS FROM ' + tn);
    for (const col of cols) {
      const type = col.Type.toLowerCase();
      if (type.startsWith('datetime') || type.startsWith('timestamp')) {
        // Skip if already TIMESTAMP with correct default
        if (type.startsWith('timestamp')) {
          // Fix create_time defaults if missing
          if (col.Field === 'create_time' && col.Default === null && col.Null === 'NO') {
            try {
              const comment = col.Comment ? `COMMENT '${col.Comment.replace(/'/g, "\\'")}'` : '';
              await c.query(`ALTER TABLE \`${tn}\` MODIFY COLUMN \`${col.Field}\` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ${comment}`);
              console.log('FIX-DEFAULT:', tn + '.' + col.Field);
            } catch (e) { /* skip */ }
          }
          continue;
        }
        const nullPart = col.Null === 'YES' ? 'NULL' : 'NOT NULL';
        const comment = col.Comment ? `COMMENT '${col.Comment.replace(/'/g, "\\'")}'` : '';
        let defaultPart = '';
        if (col.Default !== null) {
          defaultPart = `DEFAULT '${col.Default}'`;
        } else if (col.Null === 'YES') {
          defaultPart = 'DEFAULT NULL';
        } else if (col.Field === 'create_time') {
          defaultPart = 'DEFAULT CURRENT_TIMESTAMP';
        }

        const sql = `ALTER TABLE \`${tn}\` MODIFY COLUMN \`${col.Field}\` TIMESTAMP ${nullPart} ${defaultPart} ${comment}`.trim();
        try {
          await c.query(sql);
          console.log('OK:', tn + '.' + col.Field);
        } catch (e) {
          // Retry without default
          try {
            const sql2 = `ALTER TABLE \`${tn}\` MODIFY COLUMN \`${col.Field}\` TIMESTAMP ${nullPart} ${comment}`.trim();
            await c.query(sql2);
            console.log('OK(no-def):', tn + '.' + col.Field);
          } catch (e2) {
            console.error('FAIL:', tn + '.' + col.Field, e2.message.substring(0, 120));
          }
        }
      }
    }
  }

  await c.end();
})();