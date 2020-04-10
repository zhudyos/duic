package io.zhudy.duic.repository.config

import io.zhudy.duic.repository.postgresql.PostgreSqlAppRepository
import io.zhudy.duic.repository.postgresql.PostgreSqlUserRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.core.DatabaseClient

/**
 * `PostgreSQL` repository 配置.
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnExpression("T(io.zhudy.duic.DBMS).forName('\${duic.dbms}') == T(io.zhudy.duic.DBMS).PostgreSQL")
class PostgreSqlConfiguration {

    @Bean
    fun userRepository(dc: DatabaseClient) = PostgreSqlUserRepository(dc)

    @Bean
    fun appRepository(dc: DatabaseClient) = PostgreSqlAppRepository(dc)

}