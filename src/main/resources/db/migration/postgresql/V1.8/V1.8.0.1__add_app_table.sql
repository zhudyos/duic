CREATE TABLE app
(
    id serial PRIMARY KEY NOT NULL,
    name varchar(64) NOT NULL,
    profile varchar(64) NOT NULL,
    description varchar(1024) NOT NULL,
    token varchar(64),
    ip_limit varchar(1024),
    v int NOT NULL,
    content text,
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL,
    users varchar(512)
);
CREATE UNIQUE INDEX app_name_profile_uindex ON app (name, profile);
CREATE INDEX app_created_at_index ON app (created_at);
CREATE INDEX app_updated_at_index ON app (updated_at);

COMMENT ON COLUMN app.name IS '应用名称';
COMMENT ON COLUMN app.profile IS '应用环境';
COMMENT ON COLUMN app.description IS '应用描述';
COMMENT ON COLUMN app.token IS '访问令牌';
COMMENT ON COLUMN app.ip_limit IS '可访问 IP';
COMMENT ON COLUMN app.v IS '应用版本';
COMMENT ON COLUMN app.content IS '应用配置内容';
COMMENT ON COLUMN app.created_at IS '创建时间';
COMMENT ON COLUMN app.updated_at IS '修改时间';
COMMENT ON COLUMN app.users IS '应用所属用户';