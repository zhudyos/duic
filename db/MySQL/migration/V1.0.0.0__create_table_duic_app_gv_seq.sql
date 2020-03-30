-- App gv 全局版本序列
create table duic_app_gv_seq
(
	id bigint not null comment '序列'
)
comment 'App gv 全局版本序列' engine = InnoDB;
insert into duic_app_gv_seq values(0);

-- 使用示例:
-- update duic_app_gv_seq set id=last_insert_id(id + 1);
-- select last_insert_id();