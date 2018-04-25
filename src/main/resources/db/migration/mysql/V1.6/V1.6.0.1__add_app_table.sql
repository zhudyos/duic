CREATE TABLE `app` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR (20) NOT NULL COMMENT '应用名称',
  `profile` VARCHAR (20) NOT NULL COMMENT '应用配置',
  `description` VARCHAR (256) NOT NULL COMMENT '应用描述',
  `token` VARCHAR (32) NOT NULL COMMENT '访问令牌',
  `ipLimit` VARCHAR (15) NOT NULL COMMENT '可访问 IP',
  `v` INTEGER NOT NULL COMMENT '应用版本',
  `created_at` TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP COMMENT '修改时间',
  `content` TEXT NOT NULL COMMENT '应用配置内容',
  `users` JSON NOT NULL COMMENT '应用所属用户',
  PRIMARY KEY (`id`)
) ENGINE = INNODB CHARSET = utf8mb4 COLLATE = utf8mb4_bin;

ALTER TABLE `app` ADD UNIQUE INDEX (`name`,`profile`);