-- 博客系统 MySQL 建表脚本 (MySQL 8+)
SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE blog;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL COMMENT '用户名',
    password VARCHAR(128) DEFAULT NULL COMMENT '密码（第三方登录可为空）',
    nickname VARCHAR(64) DEFAULT NULL,
    avatar VARCHAR(512) DEFAULT NULL,
    email VARCHAR(128) DEFAULT NULL,
    phone VARCHAR(32) DEFAULT NULL,
    oauth_source VARCHAR(32) DEFAULT NULL COMMENT '第三方来源',
    oauth_id VARCHAR(128) DEFAULT NULL,
    status TINYINT DEFAULT 1 COMMENT '0禁用 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_username (username),
    KEY idx_oauth (oauth_source, oauth_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(64) NOT NULL,
    role_name VARCHAR(64) NOT NULL,
    description VARCHAR(256) DEFAULT NULL,
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限/菜单表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    permission_code VARCHAR(128) NOT NULL,
    permission_name VARCHAR(64) NOT NULL,
    permission_type VARCHAR(16) NOT NULL COMMENT 'menu/button',
    path VARCHAR(256) DEFAULT NULL,
    component VARCHAR(256) DEFAULT NULL,
    icon VARCHAR(64) DEFAULT NULL,
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    KEY idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限菜单表';

-- 用户-角色
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联';

-- 角色-权限
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_perm (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联';

-- 分类
CREATE TABLE IF NOT EXISTS blog_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    slug VARCHAR(64) DEFAULT NULL,
    description VARCHAR(256) DEFAULT NULL,
    sort_order INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类';

-- 标签
CREATE TABLE IF NOT EXISTS blog_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    slug VARCHAR(64) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签';

-- 文章
CREATE TABLE IF NOT EXISTS blog_article (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT DEFAULT NULL,
    title VARCHAR(256) NOT NULL,
    summary VARCHAR(512) DEFAULT NULL,
    content LONGTEXT COMMENT 'Markdown',
    cover_image VARCHAR(512) DEFAULT NULL,
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    top_flag TINYINT DEFAULT 0,
    status TINYINT DEFAULT 1 COMMENT '0草稿 1发布',
    publish_time DATETIME DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    KEY idx_user (user_id),
    KEY idx_category (category_id),
    KEY idx_status_publish (status, publish_time),
    FULLTEXT KEY ft_title_content (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章';

-- 文章-标签
CREATE TABLE IF NOT EXISTS blog_article_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    UNIQUE KEY uk_article_tag (article_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签关联';

-- 评论
CREATE TABLE IF NOT EXISTS blog_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    user_id BIGINT DEFAULT NULL,
    parent_id BIGINT DEFAULT 0,
    reply_to_id BIGINT DEFAULT NULL,
    content TEXT NOT NULL,
    like_count INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    KEY idx_article (article_id),
    KEY idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论';

-- 留言（弹幕墙）
CREATE TABLE IF NOT EXISTS blog_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT DEFAULT NULL,
    content VARCHAR(512) NOT NULL,
    ip VARCHAR(64) DEFAULT NULL,
    status TINYINT DEFAULT 1 COMMENT '0待审 1通过',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='留言';

-- 博客配置
CREATE TABLE IF NOT EXISTS blog_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(64) NOT NULL,
    config_value TEXT,
    remark VARCHAR(256) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='博客配置';

-- 相册
CREATE TABLE IF NOT EXISTS blog_album (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) DEFAULT NULL,
    url VARCHAR(512) NOT NULL,
    thumbnail VARCHAR(512) DEFAULT NULL,
    user_id BIGINT DEFAULT NULL,
    sort_order INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='相册';

-- 操作日志（AOP 写入可选，也可用表存储）
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT DEFAULT NULL,
    module VARCHAR(64) DEFAULT NULL,
    operation VARCHAR(64) DEFAULT NULL,
    method VARCHAR(256) DEFAULT NULL,
    params TEXT,
    ip VARCHAR(64) DEFAULT NULL,
    duration BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user_time (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';

-- 用户钱包（风月币）
CREATE TABLE IF NOT EXISTS user_wallet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    balance DECIMAL(18,2) DEFAULT 0 COMMENT '当前风月币余额',
    total_income DECIMAL(18,2) DEFAULT 0 COMMENT '累计收入',
    total_expense DECIMAL(18,2) DEFAULT 0 COMMENT '累计支出',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户钱包（风月币）';

-- 钱包流水
CREATE TABLE IF NOT EXISTS wallet_transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(18,2) NOT NULL COMMENT '金额，收入为正，支出为负',
    type VARCHAR(32) NOT NULL COMMENT '流水类型：SIGN_IN/POST/LIKE_REWARD/TIP_IN/TIP_OUT/ADJUST 等',
    remark VARCHAR(255) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user_time (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包流水';

-- 文章打赏记录
CREATE TABLE IF NOT EXISTS article_tip (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_user_id BIGINT NOT NULL COMMENT '打赏人',
    to_user_id BIGINT NOT NULL COMMENT '作者',
    article_id BIGINT NOT NULL COMMENT '文章ID',
    amount DECIMAL(18,2) NOT NULL COMMENT '打赏金额',
    real_income DECIMAL(18,2) NOT NULL COMMENT '作者实际收入（amount * 比例）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_to_user_time (to_user_id, create_time),
    KEY idx_article_time (article_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章打赏记录';

-- 用户收藏（文章）
CREATE TABLE IF NOT EXISTS blog_user_favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    article_id BIGINT NOT NULL COMMENT '文章ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_article (user_id, article_id),
    KEY idx_user (user_id),
    KEY idx_article (article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏';

-- 初始化管理员（密码为 BCrypt 加密后的 "admin123"，可自行生成替换）
INSERT INTO sys_user (username, password, nickname, status) VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 1) ON DUPLICATE KEY UPDATE id=id;
INSERT INTO sys_role (role_code, role_name, description) VALUES ('admin', '管理员', '超级管理员') ON DUPLICATE KEY UPDATE id=id;
INSERT INTO sys_user_role (user_id, role_id) SELECT 1, 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id=1 AND role_id=1);

-- 默认配置项
INSERT INTO blog_config (config_key, config_value, remark) VALUES
('site_name', '我的博客', '站点名称'),
('site_description', '欢迎来到我的博客', '站点描述'),
('background_image', '', '背景图URL'),
('search_mode', 'mysql', '搜索模式: mysql | elasticsearch'),
('upload_mode', 'local', '上传模式: local | minio')
ON DUPLICATE KEY UPDATE config_key=config_key;

-- 演示数据：普通用户（密码同 admin，明文 admin123，可自行修改）
INSERT INTO sys_user (username, password, nickname, email, status) VALUES
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '风月一号', 'user1@example.com', 1),
('user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '风月二号', 'user2@example.com', 1),
('user3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '风月三号', 'user3@example.com', 1)
ON DUPLICATE KEY UPDATE nickname=VALUES(nickname);

-- 演示数据：分类
INSERT INTO blog_category (name, slug, description, sort_order, deleted) VALUES
('后端开发', 'backend', 'Java / Spring / MySQL 等', 1, 0),
('前端开发', 'frontend', 'Vue / UniApp 等', 2, 0),
('生活随笔', 'life', '记录生活点滴', 3, 0)
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- 演示数据：标签
INSERT INTO blog_tag (name, slug, deleted) VALUES
('Java', 'java', 0),
('SpringBoot', 'springboot', 0),
('Vue3', 'vue3', 0),
('UniApp', 'uniapp', 0)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 演示数据：文章（假定分类 ID 从 1 开始，作者为 user1/user2/user3）
INSERT INTO blog_article (user_id, category_id, title, summary, content, cover_image, view_count, like_count, comment_count, top_flag, status, publish_time, deleted)
VALUES
((SELECT id FROM sys_user WHERE username = 'user1' LIMIT 1),
 (SELECT id FROM blog_category WHERE slug = 'backend' LIMIT 1),
 'SpringBoot3 实战博客系统',
 '基于 SpringBoot3 + Vue3 + UniApp 的前后端分离博客实战',
 '# SpringBoot3 实战博客系统\n\n这是示例文章内容。',
 NULL, 120, 8, 3, 1, 1, NOW(), 0),
((SELECT id FROM sys_user WHERE username = 'user2' LIMIT 1),
 (SELECT id FROM blog_category WHERE slug = 'frontend' LIMIT 1),
 'Vue3 + Ant Design Vue 管理后台',
 '使用 Vue3 + Ant Design Vue 搭建优雅的博客管理后台',
 '# Vue3 管理后台\n\n这是示例文章内容。',
 NULL, 96, 5, 2, 0, 1, NOW(), 0),
((SELECT id FROM sys_user WHERE username = 'user3' LIMIT 1),
 (SELECT id FROM blog_category WHERE slug = 'life' LIMIT 1),
 '生活与编码的平衡',
 '记录一名 Java 程序员的日常与思考',
 '# 生活与编码\n\n这是示例文章内容。',
 NULL, 60, 2, 1, 0, 1, NOW(), 0);

-- 演示数据：留言（弹幕墙）
INSERT INTO blog_message (user_id, content, ip, status, deleted)
VALUES
((SELECT id FROM sys_user WHERE username = 'user1' LIMIT 1), '这个博客系统好炫酷！', '127.0.0.1', 1, 0),
((SELECT id FROM sys_user WHERE username = 'user2' LIMIT 1), '期待更多 SpringBoot 教程～', '127.0.0.1', 1, 0),
((SELECT id FROM sys_user WHERE username = 'user3' LIMIT 1), 'UniApp 门户很适合移动端阅读。', '127.0.0.1', 1, 0);

-- ===========================
-- RBAC 角色与权限扩展示例
-- ===========================

-- 作者角色（如已存在 author，则只更新名称等）
INSERT INTO sys_role (role_code, role_name, description, sort_order, status)
VALUES ('author', '作者', '作者角色，只能管理自己的文章和评论', 2, 1)
ON DUPLICATE KEY UPDATE
  role_name  = VALUES(role_name),
  description= VALUES(description),
  sort_order = VALUES(sort_order),
  status     = VALUES(status);

-- 文章、评论相关基础权限（若已存在相同 permission_code 则更新）
INSERT INTO sys_permission (parent_id, permission_code, permission_name, permission_type, path, component, icon, sort_order, status)
VALUES
  (0, 'article:view:all',  '查看所有文章',        'button', '/admin/article/page',  NULL, NULL, 10, 1),
  (0, 'article:view:self', '查看自己文章',        'button', '/admin/article/page',  NULL, NULL, 20, 1),
  (0, 'comment:view:all',  '查看所有评论',        'button', '/admin/comment/page', NULL, NULL, 30, 1),
  (0, 'comment:view:self', '查看自己文章评论',    'button', '/admin/comment/page', NULL, NULL, 40, 1)
ON DUPLICATE KEY UPDATE
  permission_name = VALUES(permission_name),
  permission_type = VALUES(permission_type),
  path            = VALUES(path),
  sort_order      = VALUES(sort_order),
  status          = VALUES(status);

-- 为管理员角色绑定“查看所有文章/评论”权限
INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.permission_code IN ('article:view:all', 'comment:view:all')
WHERE r.role_code = 'admin';

-- 为作者角色绑定“只看自己文章/评论”权限
INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.permission_code IN ('article:view:self', 'comment:view:self')
WHERE r.role_code = 'author';

