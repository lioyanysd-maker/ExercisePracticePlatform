/*
 * ExercisePracticeSystem 数据库初始化脚本
 * 版本: 1.0
 * 创建日期: 2026-02-09
 * 
 * 说明：
 * 1. 此脚本用于创建完整的数据库表结构
 * 2. 不包含测试数据，仅创建表结构
 * 3. 适用于 Java Spring Boot 后端
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ==================== 创建数据库 ====================
DROP DATABASE IF EXISTS `end_of_term_revision`;
CREATE DATABASE `end_of_term_revision` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `end_of_term_revision`;

-- ==================== 用户表 ====================
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户主键 ID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名（唯一）',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码哈希值（BCrypt加密）',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '邮箱（可选）',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `username` (`username`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户表';

-- ==================== 科目表 ====================
DROP TABLE IF EXISTS `subjects`;
CREATE TABLE `subjects` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '科目ID',
  `user_id` bigint NOT NULL COMMENT '所属用户 ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '科目名称',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `name` (`name`,`user_id`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE,
  CONSTRAINT `subjects_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='科目表';

-- ==================== 题目表 ====================
DROP TABLE IF EXISTS `questions`;
CREATE TABLE `questions` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题目 ID',
  `subject_id` bigint NOT NULL COMMENT '科目 ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `type` enum('single','multiple','judge','fill','major') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题目类型：single=单选，multiple=多选，judge=判断，fill=填空，major=简答',
  `question` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题干内容',
  `options_json` json DEFAULT NULL COMMENT '选项 JSON（用于单选、多选、判断题）',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '正确答案',
  `analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '解析',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `subject_id` (`subject_id`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE,
  CONSTRAINT `questions_ibfk_1` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `questions_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='题库表';

-- ==================== 题目资源表 ====================
DROP TABLE IF EXISTS `question_resources`;
CREATE TABLE `question_resources` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `question_id` bigint NOT NULL COMMENT '关联题目ID',
  `resource_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源类型：image/table_json/diagram_desc/other',
  `resource_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源内容：图片URL或JSON数据',
  `resource_order` int DEFAULT 0 COMMENT '资源显示顺序',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_question_id` (`question_id`) USING BTREE,
  KEY `idx_resource_type` (`resource_type`) USING BTREE,
  CONSTRAINT `question_resources_ibfk_1` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目资源表';

-- ==================== 练习会话表 ====================
DROP TABLE IF EXISTS `practice_sessions`;
CREATE TABLE `practice_sessions` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '练习会话 ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `subject_id` bigint NOT NULL COMMENT '科目 ID',
  `total_count` int NOT NULL COMMENT '总题数',
  `correct_count` int NOT NULL DEFAULT 0 COMMENT '正确题数',
  `wrong_count` int NOT NULL DEFAULT 0 COMMENT '错误题数',
  `accuracy` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '正确率',
  `grade` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'F' COMMENT '成绩等级 A/B/C/D/F',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '练习时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_subject` (`user_id`,`subject_id`) USING BTREE,
  KEY `idx_created_at` (`created_at`) USING BTREE,
  KEY `practice_sessions_ibfk_2` (`subject_id`) USING BTREE,
  CONSTRAINT `practice_sessions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `practice_sessions_ibfk_2` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='练习会话表';

-- ==================== 练习记录表 ====================
DROP TABLE IF EXISTS `practice_records`;
CREATE TABLE `practice_records` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '练习记录 ID',
  `session_id` bigint DEFAULT NULL COMMENT '练习会话 ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `subject_id` bigint NOT NULL COMMENT '科目 ID',
  `question_id` bigint NOT NULL COMMENT '题目 ID',
  `user_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户作答',
  `is_correct` tinyint(1) NOT NULL COMMENT '是否正确 1=正确 0=错误',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '作答时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE,
  KEY `subject_id` (`subject_id`) USING BTREE,
  KEY `question_id` (`question_id`) USING BTREE,
  KEY `idx_session_id` (`session_id`) USING BTREE,
  CONSTRAINT `practice_records_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `practice_records_ibfk_2` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `practice_records_ibfk_3` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `practice_records_ibfk_4` FOREIGN KEY (`session_id`) REFERENCES `practice_sessions` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='练习记录表';

-- ==================== 错题本表 ====================
DROP TABLE IF EXISTS `error_book`;
CREATE TABLE `error_book` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '错题记录 ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `subject_id` bigint NOT NULL COMMENT '科目 ID',
  `question_id` bigint NOT NULL COMMENT '题目 ID',
  `wrong_count` int DEFAULT 1 COMMENT '累计错误次数',
  `last_wrong_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后错误时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `user_id` (`user_id`,`subject_id`,`question_id`) USING BTREE,
  KEY `subject_id` (`subject_id`) USING BTREE,
  KEY `question_id` (`question_id`) USING BTREE,
  CONSTRAINT `error_book_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `error_book_ibfk_2` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `error_book_ibfk_3` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='错题集表';

-- ==================== AI模型配置表 ====================
DROP TABLE IF EXISTS `llm_models`;
CREATE TABLE `llm_models` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模型配置 ID',
  `user_id` bigint NOT NULL COMMENT '所属用户 ID',
  `model_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模型名称',
  `base_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模型 API 地址',
  `api_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'API 密钥',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE,
  CONSTRAINT `llm_models_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户自定义大模型配置表';

-- ==================== 科目共享表 ====================
DROP TABLE IF EXISTS `subject_shares`;
CREATE TABLE `subject_shares` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '共享记录ID',
  `owner_user_id` bigint NOT NULL COMMENT '科目拥有者ID',
  `subject_id` bigint NOT NULL COMMENT '被共享的科目ID',
  `target_user_id` bigint DEFAULT NULL COMMENT '被共享给的用户ID（NULL表示公共共享）',
  `share_type` enum('USER','PUBLIC') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '共享类型：USER=指定用户，PUBLIC=公共',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `unique_share` (`subject_id`,`target_user_id`,`share_type`) USING BTREE,
  KEY `idx_subject_id` (`subject_id`) USING BTREE,
  KEY `idx_target_user` (`target_user_id`) USING BTREE,
  KEY `idx_owner` (`owner_user_id`) USING BTREE,
  CONSTRAINT `subject_shares_ibfk_1` FOREIGN KEY (`owner_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `subject_shares_ibfk_2` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `subject_shares_ibfk_3` FOREIGN KEY (`target_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='科目共享表';

-- ==================== 系统配置表（可选，用于存储系统级配置）====================
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键名(唯一)',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '配置值(JSON格式)',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `config_key` (`config_key`) USING BTREE,
  KEY `idx_config_key` (`config_key`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

SET FOREIGN_KEY_CHECKS = 1;

-- ==================== 初始化完成 ====================
SELECT 'Database initialization completed successfully!' AS message;
