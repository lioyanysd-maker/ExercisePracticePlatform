/*
 * 清空系统所有业务数据，用于从头开始测试
 * 执行后：表结构保留，所有表数据被清空，需重新注册用户并重新导入题目
 *
 * 使用方式（任选其一）：
 * 1. MySQL 命令行：mysql -u 用户名 -p end_of_term_revision < clear_all_data.sql
 * 2. Navicat / DBeaver 等：打开此文件，选择数据库 end_of_term_revision 后执行
 * 3. 若希望连表结构都重建：直接执行 init_database.sql（会 DROP 并重建整个库）
 */

USE `end_of_term_revision`;

SET FOREIGN_KEY_CHECKS = 0;

-- 按依赖关系从子表到父表清空（避免外键报错；关闭外键检查后顺序可任意）
TRUNCATE TABLE `practice_records`;
TRUNCATE TABLE `practice_sessions`;
TRUNCATE TABLE `error_book`;
TRUNCATE TABLE `question_resources`;
TRUNCATE TABLE `subject_shares`;
TRUNCATE TABLE `llm_models`;
TRUNCATE TABLE `questions`;
TRUNCATE TABLE `subjects`;
TRUNCATE TABLE `users`;

-- 系统配置表（由 init_database.sql 创建，若未创建可注释本行）
TRUNCATE TABLE `system_config`;

SET FOREIGN_KEY_CHECKS = 1;

SELECT 'All business data cleared. You can re-register and test from scratch.' AS message;
