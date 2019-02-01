# Changelog

本项目的所有显著变化将记录在本文件中。

## [2.3.2-beta] - 2019-02-01

### Added

- 新增限流降级功能，提高稳定性
- 新增限流警告日志

### Changed

- 修改静态资源缓存策略，提高操作体验

## [2.3.0-beta] - 2019-01-15

### Added

- 新增服务启动配置固定路径扫描 `/etc/duic/duic.yml`，更加简化部署操作。
- 新增 RESTful 接口 `/api/info` 获取服务信息。
- 新增 RESTful 接口 `/api/health` 心跳检查，用于监控服务状态。
- 引入 kotlin-coroutines 协程重写监听配置状态逻辑。
- 引入 caffeine 缓存存储配置信息。
- 服务启动时自动将进程 pid 输出至 `application.pid` 文件中。
- 新增 PrepareEnvironmentPostProcessor 校验服务启动基本参数。

### Changed

- 重构服务基本配置项使用 `duic.mongodb.url` 替换原有配置 `spring.data.mongodb.uri`，使用 `duic.mysql.url` 替换原有配置 `spring.datasource.url`，使用 `duic.postgresql.url` 替换原有配置 `spring.datasource.url`。
- 升级 kotlin 至 1.3.11。
- 升级 spring-boot 版本至 2.1.2.RELEASE。
- 使用 spring init/destroy 生命周期方法替代之前的 `@EventListener` 实现方式。

### Deprecated

- `/servers` 接口不建议继续使用。采用 `/api/servers` 代替，`/servers` 接口会在 3.0 版本移除。

### Removed

- 移除 Oracle 数据存储实现。
- 移除 Sentry 实现。