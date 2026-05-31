-- ============================================
-- 宿舍报修管理系统 - 数据库建表脚本
-- Database: dorm_repair
-- DBMS: MySQL 5.5+ / 8.0
-- ============================================

SET NAMES utf8;

CREATE DATABASE IF NOT EXISTS dorm_repair
    DEFAULT CHARACTER SET utf8
    DEFAULT COLLATE utf8_general_ci;

USE dorm_repair;

-- ============================================
-- 1. 用户表 (user)
-- ============================================
CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(255) NOT NULL COMMENT '密码',
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'STUDENT' COMMENT '角色: STUDENT-学生, ADMIN-管理员',
    `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP    NULL DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户表';

-- ============================================
-- 2. 宿舍表 (dormitory)
-- ============================================
CREATE TABLE IF NOT EXISTS `dormitory` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '宿舍ID',
    `building`    VARCHAR(50) NOT NULL COMMENT '楼栋号',
    `room_no`     VARCHAR(20) NOT NULL COMMENT '房间号',
    `create_time` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_building_room` (`building`, `room_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='宿舍表';

-- ============================================
-- 3. 学生表 (student)
-- ============================================
CREATE TABLE IF NOT EXISTS `student` (
    `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '学生ID',
    `user_id`      BIGINT      NOT NULL COMMENT '关联用户ID',
    `student_no`   VARCHAR(20) NOT NULL COMMENT '学号',
    `name`         VARCHAR(50) NOT NULL COMMENT '姓名',
    `phone`        VARCHAR(20) NOT NULL COMMENT '联系电话',
    `dormitory_id` BIGINT      NOT NULL COMMENT '关联宿舍ID',
    `create_time`  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  TIMESTAMP   NULL DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_no` (`student_no`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_dormitory_id` (`dormitory_id`),
    CONSTRAINT `fk_student_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_student_dormitory` FOREIGN KEY (`dormitory_id`) REFERENCES `dormitory` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='学生表';

-- ============================================
-- 4. 管理员表 (admin)
-- ============================================
CREATE TABLE IF NOT EXISTS `admin` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
    `user_id`     BIGINT      NOT NULL COMMENT '关联用户ID',
    `name`        VARCHAR(50) NOT NULL COMMENT '姓名',
    `phone`       VARCHAR(20) NOT NULL COMMENT '联系电话',
    `create_time` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP   NULL DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_admin_user_id` (`user_id`),
    CONSTRAINT `fk_admin_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='管理员表';

-- ============================================
-- 5. 报修表 (repair)
-- ============================================
CREATE TABLE IF NOT EXISTS `repair` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '报修ID',
    `student_id`   BIGINT       NOT NULL COMMENT '报修学生ID',
    `dormitory_id` BIGINT       NOT NULL COMMENT '宿舍ID',
    `title`        VARCHAR(100) NOT NULL COMMENT '报修标题',
    `description`  TEXT         NOT NULL COMMENT '报修描述',
    `status`       VARCHAR(20)  NOT NULL DEFAULT '待处理' COMMENT '状态: 待处理, 处理中, 已完成, 已取消',
    `admin_id`     BIGINT       DEFAULT NULL COMMENT '处理管理员ID',
    `handle_desc`  TEXT         DEFAULT NULL COMMENT '处理说明',
    `rating`       INT          DEFAULT NULL COMMENT '评分1-5',
    `review`       TEXT         DEFAULT NULL COMMENT '评价内容',
    `create_time`  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报修时间',
    `update_time`  TIMESTAMP    NULL DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_admin_id` (`admin_id`),
    KEY `idx_status` (`status`),
    KEY `idx_dormitory_id` (`dormitory_id`),
    CONSTRAINT `fk_repair_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_repair_admin` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_repair_dormitory` FOREIGN KEY (`dormitory_id`) REFERENCES `dormitory` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='报修表';

-- ============================================
-- 初始数据
-- ============================================

-- 插入宿舍楼数据
INSERT INTO `dormitory` (`building`, `room_no`) VALUES
('1号楼', '101'), ('1号楼', '102'), ('1号楼', '103'), ('1号楼', '104'),
('1号楼', '201'), ('1号楼', '202'), ('1号楼', '203'), ('1号楼', '204'),
('2号楼', '101'), ('2号楼', '102'), ('2号楼', '103'), ('2号楼', '104'),
('2号楼', '201'), ('2号楼', '202'), ('2号楼', '203'), ('2号楼', '204'),
('3号楼', '301'), ('3号楼', '302'), ('3号楼', '303'), ('3号楼', '304'),
('3号楼', '401'), ('3号楼', '402'), ('3号楼', '403'), ('3号楼', '404');

-- 插入管理员用户 (密码 123456)
INSERT INTO `user` (`username`, `password`, `role`) VALUES
('admin', '123456', 'ADMIN');

INSERT INTO `admin` (`user_id`, `name`, `phone`) VALUES
(1, '张管理员', '13800000000');

-- 插入测试学生用户
INSERT INTO `user` (`username`, `password`, `role`) VALUES
('student1', '123456', 'STUDENT'),
('student2', '123456', 'STUDENT');

INSERT INTO `student` (`user_id`, `student_no`, `name`, `phone`, `dormitory_id`) VALUES
(2, '2024001001', '李晓晓', '13900000001', 1),
(3, '2024001002', '王欣欣', '13900000002', 5);
