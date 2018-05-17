CREATE TABLE `server`(
  `id` VARCHAR(65) NOT NULL,
  `host` VARCHAR(32) NOT NULL COMMENT '主机地址',
  `port` INT(5) NOT NULL COMMENT '主机端口',
  `init_at` TIMESTAMP NOT NULL COMMENT '初始化时间',
  `active_at` TIMESTAMP COMMENT '最后活跃时间',
  PRIMARY KEY (`id`)
) ENGINE=INNODB CHARSET=utf8mb4 COLLATE=utf8mb4_bin;