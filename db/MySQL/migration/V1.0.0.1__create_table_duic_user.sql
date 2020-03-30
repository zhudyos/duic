create table duic_user
(
    id         int auto_increment comment '主键',
    email      varchar(64)  not null comment '登录邮箱',
    password   varchar(512) null comment '登录密码',
    created_at datetime     not null comment '创建时间',
    updated_at datetime     not null comment '修改时间',
    constraint duic_user_pk primary key (id)
) comment '用户信息' engine=InnoDB;

create unique index duic_user_email_uindex on duic_user (email);