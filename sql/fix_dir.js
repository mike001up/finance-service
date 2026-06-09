const mysql = require('mysql2/promise');
(async () => {
  const c = await mysql.createConnection({host:'127.0.0.1',port:3306,user:'root',password:'001Sea001!',database:'finance_service'});
  await c.query("ALTER TABLE biz_account MODIFY COLUMN direction VARCHAR(10) DEFAULT 'debit'");
  console.log('direction column fixed');
  await c.end();
})();