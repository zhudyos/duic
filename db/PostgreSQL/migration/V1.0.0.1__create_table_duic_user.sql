create table duic_user
(
    id         serial      not null
        constraint duic_user_pk primary key,
    email      varchar(64) not null,
    password   varchar(512),
    created_at  timestamp    not null,
    updated_at  timestamp    not null
);

comment on table duic_user is '用户信息';

comment on column duic_user.id is '主键';
comment on column duic_user.email is '登录邮箱';
comment on column duic_user.password is '登录密码';
comment on column duic_user.created_at is '创建时间';
comment on column duic_user.updated_at is '修改时间';

create unique index duic_user_email_uindex on duic_user (email);