CREATE TABLE `user` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR (64) NOT NULL COMMENT '登录邮箱',
  `password` VARCHAR (256) NOT NULL COMMENT '登录密码',
  `created_at` TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE = INNODB CHARSET = utf8mb4 COLLATE = utf8mb4_bin;

ALTER TABLE `user` ADD UNIQUE INDEX (`email`);