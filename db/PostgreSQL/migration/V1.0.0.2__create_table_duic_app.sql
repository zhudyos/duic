create table duic_app
(
    id          serial       not null
        constraint duic_app_pk primary key,
    name        varchar(64)  not null,
    profile     varchar(64)  not null,
    description varchar(512) not null,
    content     text,
    token       varchar(512),
    ip_limit    varchar(2048),
    v           int          not null,
    gv          bigint       not null,
    users       varchar(2048),
    created_at  timestamp    not null,
    updated_at  timestamp    not null
);

comment on table duic_app is '应用配置';

comment on column duic_app.id is '主键';
comment on column duic_app.name is '应用名称';
comment on column duic_app.profile is '应用环境';
comment on column duic_app.description is '应用描述';
comment on column duic_app.content is '配置内容';
comment on column duic_app.token is '访问 TOKEN';
comment on column duic_app.ip_limit is '应用 IP 访问限制';
comment on column duic_app.v is '版本号';
comment on column duic_app.gv is '全局配置版本, 标识最新配置修改的版本号';
comment on column duic_app.users is '编辑配置权限的用户列表, 以英文"逗号"分隔. 示例: shangsan@mail.com,lishi@mail.com';
comment on column duic_app.created_at is '创建时间';
comment on column duic_app.updated_at is '修改时间';

create unique index duic_app_gv_uindex on duic_app (gv);
create unique index duic_app_name_profile_uindex on duic_app (name, profile);