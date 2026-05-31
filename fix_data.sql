SET NAMES utf8;
UPDATE repair SET status='待处理', handle_desc=NULL, admin_id=NULL WHERE id=1;
UPDATE repair SET status='待处理', handle_desc=NULL, admin_id=NULL WHERE id=2;
UPDATE repair SET title='水龙头漏水需要维修' WHERE id=1;
UPDATE repair SET title='空调不制冷需要检修' WHERE id=2;
SELECT id, title, status, HEX(status) FROM repair;
