CREATE TABLE "user"
(
    id serial PRIMARY KEY NOT NULL,
    email varchar(64) NOT NULL,
    password varchar(256) NOT NULL,
    created_at timestamp NOT NULL,
    updated_at timestamp
);
CREATE UNIQUE INDEX user_email_uindex ON "user" (email);
COMMENT ON COLUMN "user".email IS '登录邮箱';
COMMENT ON COLUMN "user".password IS '登录密码';
COMMENT ON COLUMN "user".created_at IS '创建时间';
COMMENT ON COLUMN "user".updated_at IS '修改时间';