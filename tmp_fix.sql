SET NAMES utf8;
UPDATE repair SET status='待处理', handle_desc=NULL, admin_id=NULL WHERE id=3;
UPDATE repair SET status=UNHEX('E5B7B2E5AE8CE68890'), handle_desc=UNHEX('E7BBB4E4BFAEE5A484E79086E5AE8CE6AF95') WHERE id=3;
SELECT id, status, HEX(status), handle_desc FROM repair WHERE id=3;
