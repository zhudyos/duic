package io.zhudy.duic.repository.config

import io.zhudy.duic.repository.mysql.MySqlAppRepository
import io.zhudy.duic.repository.mysql.MySqlUserRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.core.DatabaseClient

/**
 * `MySQL` repository 配置.
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnExpression("T(io.zhudy.duic.DBMS).forName('\${duic.dbms}') == T(io.zhudy.duic.DBMS).MySQL")
class MySqlConfiguration {

    @Bean
    fun userRepository(dc: DatabaseClient) = MySqlUserRepository(dc)

    @Bean
    fun appRepository(dc: DatabaseClient) = MySqlAppRepository(dc)

}