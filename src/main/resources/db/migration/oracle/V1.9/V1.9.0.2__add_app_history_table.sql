CREATE TABLE APP_HISTORY
(
    ID INT PRIMARY KEY,
    NAME VARCHAR(64) NOT NULL,
    PROFILE VARCHAR(64) NOT NULL,
    DESCRIPTION VARCHAR(1024) NOT NULL,
    TOKEN VARCHAR(64),
    IP_LIMIT VARCHAR(1024),
    V INT NOT NULL,
    CREATED_AT TIMESTAMP NOT NULL,
    CONTENT CLOB,
    USERS VARCHAR(512),
    UPDATED_BY VARCHAR(64),
    DELETED_BY VARCHAR(64)
);
CREATE INDEX APP_HISTORY_NAME_INDEX ON APP_HISTORY (NAME);
CREATE INDEX APP_HISTORY_PROFILE_INDEX ON APP_HISTORY (PROFILE);
CREATE INDEX APP_HISTORY_CREATED_AT_INDEX ON APP_HISTORY (CREATED_AT);
COMMENT ON COLUMN APP_HISTORY.NAME IS '应用名称';
COMMENT ON COLUMN APP_HISTORY.PROFILE IS '应用环境';
COMMENT ON COLUMN APP_HISTORY.DESCRIPTION IS '应用描述';
COMMENT ON COLUMN APP_HISTORY.TOKEN IS '访问令牌';
COMMENT ON COLUMN APP_HISTORY.IP_LIMIT IS '可访问 IP';
COMMENT ON COLUMN APP_HISTORY.V IS '应用版本';
COMMENT ON COLUMN APP_HISTORY.CREATED_AT IS '创建时间';
COMMENT ON COLUMN APP_HISTORY.CONTENT IS '应用配置内容';
COMMENT ON COLUMN APP_HISTORY.USERS IS '应用所属用户';
COMMENT ON COLUMN APP_HISTORY.UPDATED_BY IS '修改者';
COMMENT ON COLUMN APP_HISTORY.DELETED_BY IS '删除者';

CREATE SEQUENCE SEQ_APP_HISTORY
  MINVALUE 1
  NOMAXVALUE
  INCREMENT BY 1
  START WITH 1
  NOCACHE;