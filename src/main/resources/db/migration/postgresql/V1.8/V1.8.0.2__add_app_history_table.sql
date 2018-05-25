CREATE TABLE app_history
(
    id serial PRIMARY KEY NOT NULL,
    name varchar(64) NOT NULL,
    profile varchar(64) NOT NULL,
    description varchar(1024) NOT NULL,
    token varchar(64),
    ip_limit varchar(1024),
    v int NOT NULL,
    created_at timestamp NOT NULL,
    content text,
    users varchar(512),
    updated_by varchar(64),
    deleted_by varchar(64)
);
CREATE INDEX app_history_name_index ON app_history (name);
CREATE INDEX app_history_profile_index ON app_history (profile);
CREATE INDEX app_history_created_at_index ON app_history (created_at);
COMMENT ON COLUMN app_history.name IS '应用名称';
COMMENT ON COLUMN app_history.profile IS '应用环境';
COMMENT ON COLUMN app_history.description IS '应用描述';
COMMENT ON COLUMN app_history.token IS '访问令牌';
COMMENT ON COLUMN app_history.ip_limit IS '可访问 IP';
COMMENT ON COLUMN app_history.v IS '应用版本';
COMMENT ON COLUMN app_history.created_at IS '创建时间';
COMMENT ON COLUMN app_history.content IS '应用配置内容';
COMMENT ON COLUMN app_history.users IS '应用所属用户';
COMMENT ON COLUMN app_history.updated_by IS '修改者';
COMMENT ON COLUMN app_history.deleted_by IS '删除者';