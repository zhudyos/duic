create table duic_app_his
(
	id int auto_increment comment '主键',
	name varchar(64) not null comment '应用名称',
	profile varchar(64) not null comment '应用环境',
	description varchar(512) not null comment '应用描述',
	content text null comment '配置内容',
	token varchar(512) null comment '访问 TOKEN',
	ip_limit varchar(2048) null comment '应用 IP 访问限制',
	v int not null comment '版本号',
	gv bigint not null comment '全局配置版本, 标识最新配置修改的版本号',
	users varchar(2048) null comment '编辑配置权限的用户列表, 以英文"逗号"分隔. 示例: shangsan@mail.com,lishi@mail.com',
	updated_by varchar(64) null comment '修改者',
	deleted_by varchar(64) null comment '删除者',
	created_at datetime not null comment '创建时间',
	constraint duic_app_his_pk primary key (id)
) comment '应用配置历史记录' engine = InnoDB;

create unique index duic_app_his_gv_uindex on duic_app_his (gv);