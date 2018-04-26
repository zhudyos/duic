CREATE TABLE `app_history` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '应用名称',
  `profile` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '应用环境',
  `description` varchar(1024) COLLATE utf8mb4_bin NOT NULL COMMENT '应用描述',
  `token` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '访问令牌',
  `ip_limit` varchar(1024) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '可访问 IP',
  `v` int(11) NOT NULL COMMENT '应用版本',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp() COMMENT '创建时间',
  `content` text COLLATE utf8mb4_bin DEFAULT NULL COMMENT '应用配置内容',
  `users` varchar(512) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '应用所属用户',
  `updated_by` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '修改者',
  `deleted_by` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '删除者',
  PRIMARY KEY (`id`),
  KEY `name` (`name`),
  KEY `profile` (`profile`),
  KEY `created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;