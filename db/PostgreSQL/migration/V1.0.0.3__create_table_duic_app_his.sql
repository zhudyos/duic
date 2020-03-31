create table duic_app_his
(
    id          serial       not null
        constraint duic_app_his_pk
            primary key,
    name        varchar(64)  not null,
    profile     varchar(64)  not null,
    description varchar(512) not null,
    content     text,
    token       varchar(512),
    ip_limit    varchar(2048),
    v           int          not null,
    gv          bigint       not null,
    users       varchar(2048),
    updated_by  varchar(64),
    deleted_by  varchar(64),
    created_at  timestamp    not null
);

comment on table duic_app_his is '应用配置历史记录';

comment on column duic_app_his.id is '主键';
comment on column duic_app_his.name is '应用名称';
comment on column duic_app_his.profile is '应用环境';
comment on column duic_app_his.description is '应用描述';
comment on column duic_app_his.content is '配置内容';
comment on column duic_app_his.token is '访问 TOKEN';
comment on column duic_app_his.ip_limit is '应用 IP 访问限制';
comment on column duic_app_his.v is '版本号';
comment on column duic_app_his.gv is '全局配置版本, 标识最新配置修改的版本号';
comment on column duic_app_his.users is '编辑配置权限的用户列表, 以英文"逗号"分隔. 示例: shangsan@mail.com,lishi@mail.com';
comment on column duic_app_his.updated_by is '修改者';
comment on column duic_app_his.deleted_by is '删除者';
comment on column duic_app_his.created_at is '创建时间';

create unique index duic_app_his_gv_uindex on duic_app_his (gv);