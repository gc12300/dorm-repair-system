UPDATE dorm_repair.repair SET status='待处理', handle_desc=NULL WHERE id=2;
SELECT id, title, status, handle_desc FROM dorm_repair.repair;
