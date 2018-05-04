CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '登录邮箱',
  `password` varchar(256) COLLATE utf8mb4_bin NOT NULL COMMENT '登录密码',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp() COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;