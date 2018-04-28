CREATE TABLE `app` (
<<<<<<< HEAD
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '应用名称',
  `profile` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '应用环境',
  `description` varchar(1024) COLLATE utf8mb4_bin NOT NULL COMMENT '应用描述',
  `token` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '访问令牌',
  `ip_limit` varchar(1024) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '可访问 IP',
  `v` int(11) NOT NULL COMMENT '应用版本',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp() COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '修改时间',
  `content` text COLLATE utf8mb4_bin DEFAULT NULL COMMENT '应用配置内容',
  `users` varchar(512) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '应用所属用户',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_profile` (`name`,`profile`),
  KEY `created_at` (`created_at`),
  KEY `updated_at` (`updated_at`),
  FULLTEXT KEY `name_profile_content` (`name`,`profile`,`content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;