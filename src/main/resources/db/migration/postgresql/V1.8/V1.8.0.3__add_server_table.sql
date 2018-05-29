CREATE TABLE server
(
    id varchar(64) PRIMARY KEY NOT NULL,
    host varchar(32) NOT NULL,
    port int NOT NULL,
    init_at timestamp NOT NULL,
    active_at timestamp NOT NULL
);
COMMENT ON COLUMN server.host IS '主机地址';
COMMENT ON COLUMN server.port IS '主机端口';
COMMENT ON COLUMN server.init_at IS '初始化时间';
COMMENT ON COLUMN server.active_at IS '最后活跃时间';