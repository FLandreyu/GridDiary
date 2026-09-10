-- =============================================================
-- 九宫格记忆网 GridDiary 建库建表脚本 (MySQL 8.x)
-- 使用：mysql -u root -p < schema.sql
-- =============================================================

CREATE DATABASE IF NOT EXISTS `grid_diary`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `grid_diary`;

-- -------------------------------------------------------------
-- 1. 用户表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`   VARCHAR(50)     NOT NULL                COMMENT '用户名（唯一）',
  `password`   VARCHAR(100)    NOT NULL                COMMENT 'BCrypt 散列后的密码',
  `email`      VARCHAR(128)    NOT NULL                COMMENT '邮箱（唯一）',
  `nickname`   VARCHAR(64)     NOT NULL                COMMENT '昵称',
  `avatar`     VARCHAR(255)             DEFAULT NULL   COMMENT '头像地址',
  `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- -------------------------------------------------------------
-- 2. 日记表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `diary`;
CREATE TABLE `diary` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日记ID',
  `user_id`    BIGINT UNSIGNED NOT NULL                COMMENT '作者ID',
  `title`      VARCHAR(200)    NOT NULL                COMMENT '标题',
  `content`    TEXT                                    COMMENT '正文',
  `cover`      VARCHAR(255)             DEFAULT NULL   COMMENT '封面缩略图（取第一张图）',
  `is_public`  TINYINT(1)      NOT NULL DEFAULT 1      COMMENT '是否公开：1公开 0仅自己',
  `like_count` INT UNSIGNED    NOT NULL DEFAULT 0      COMMENT '点赞数（冗余计数）',
  `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `updated_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                              ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_diary_user` (`user_id`),
  KEY `idx_diary_created` (`created_at`),
  CONSTRAINT `fk_diary_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '日记表';

-- -------------------------------------------------------------
-- 3. 日记图片表（一篇日记多张图）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `diary_image`;
CREATE TABLE `diary_image` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `diary_id`     BIGINT UNSIGNED NOT NULL                COMMENT '所属日记ID',
  `original_url` VARCHAR(255)    NOT NULL                COMMENT '原图地址',
  `thumb_url`    VARCHAR(255)    NOT NULL                COMMENT '缩略图地址',
  `sort_order`   INT             NOT NULL DEFAULT 0      COMMENT '排序（越小越靠前）',
  `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`id`),
  KEY `idx_image_diary` (`diary_id`),
  CONSTRAINT `fk_image_diary` FOREIGN KEY (`diary_id`) REFERENCES `diary` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '日记图片表';

-- -------------------------------------------------------------
-- 4. 评论表（支持两级：parent_id 为空为一级评论）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `diary_id`   BIGINT UNSIGNED NOT NULL                COMMENT '日记ID',
  `user_id`    BIGINT UNSIGNED NOT NULL                COMMENT '评论人ID',
  `parent_id`  BIGINT UNSIGNED          DEFAULT NULL   COMMENT '父评论ID（NULL为一级）',
  `content`    VARCHAR(500)    NOT NULL                COMMENT '评论内容',
  `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`id`),
  KEY `idx_comment_diary` (`diary_id`),
  KEY `idx_comment_user` (`user_id`),
  CONSTRAINT `fk_comment_diary` FOREIGN KEY (`diary_id`) REFERENCES `diary` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_user`  FOREIGN KEY (`user_id`)  REFERENCES `user`  (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '评论表';

-- -------------------------------------------------------------
-- 5. 点赞表（diary_id + user_id 唯一，防重复点赞）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `diary_like`;
CREATE TABLE `diary_like` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `diary_id`   BIGINT UNSIGNED NOT NULL                COMMENT '日记ID',
  `user_id`    BIGINT UNSIGNED NOT NULL                COMMENT '点赞人ID',
  `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_like_diary_user` (`diary_id`, `user_id`),
  KEY `idx_like_user` (`user_id`),
  CONSTRAINT `fk_like_diary` FOREIGN KEY (`diary_id`) REFERENCES `diary` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_like_user`  FOREIGN KEY (`user_id`)  REFERENCES `user`  (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '点赞表';

-- -------------------------------------------------------------
-- 6. 私信表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '私信ID',
  `from_user_id` BIGINT UNSIGNED NOT NULL                COMMENT '发送人ID',
  `to_user_id`   BIGINT UNSIGNED NOT NULL                COMMENT '接收人ID',
  `content`      VARCHAR(1000)   NOT NULL                COMMENT '内容',
  `is_read`      TINYINT(1)      NOT NULL DEFAULT 0      COMMENT '是否已读：0未读 1已读',
  `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_message_to` (`to_user_id`, `is_read`),
  KEY `idx_message_from` (`from_user_id`),
  CONSTRAINT `fk_message_from` FOREIGN KEY (`from_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_message_to`   FOREIGN KEY (`to_user_id`)   REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '私信表';

-- -------------------------------------------------------------
-- 7. 每日打卡表（user_id + checkin_date 唯一，一天一次；打卡时生成今日运势）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `checkin`;
CREATE TABLE `checkin` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '打卡ID',
  `user_id`         BIGINT UNSIGNED NOT NULL                COMMENT '用户ID',
  `checkin_date`    DATE            NOT NULL                COMMENT '打卡日期',
  `fortune_level`   VARCHAR(8)      NOT NULL                COMMENT '运势等级：大吉/中吉/小吉/平',
  `fortune_stars`   TINYINT         NOT NULL DEFAULT 3      COMMENT '运势星级 1~5',
  `lucky_color`     VARCHAR(16)     NOT NULL                COMMENT '幸运色名称',
  `lucky_color_hex` VARCHAR(16)     NOT NULL                COMMENT '幸运色色值',
  `lucky_number`    TINYINT         NOT NULL                COMMENT '幸运数字 1~9',
  `suit`            VARCHAR(200)    NOT NULL                COMMENT '宜（多个用英文逗号分隔）',
  `avoid`           VARCHAR(200)    NOT NULL                COMMENT '忌（多个用英文逗号分隔）',
  `fortune_text`    VARCHAR(200)    NOT NULL                COMMENT '运势签文',
  `created_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '打卡时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_checkin_user_date` (`user_id`, `checkin_date`),
  CONSTRAINT `fk_checkin_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '每日打卡表';
