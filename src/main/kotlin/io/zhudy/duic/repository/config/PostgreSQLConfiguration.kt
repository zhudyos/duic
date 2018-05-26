package io.zhudy.duic.repository.config

import io.zhudy.duic.repository.impl.PostgreSQLUserRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.PlatformTransactionManager

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Configuration
@ConditionalOnExpression("T(io.zhudy.duic.DBMS).forName('\${duic.dbms}') == T(io.zhudy.duic.DBMS).PostgreSQL")
class PostgreSQLConfiguration {

    @Bean
    fun userRepository(transactionManager: PlatformTransactionManager, jdbcTemplate: NamedParameterJdbcTemplate)
            = PostgreSQLUserRepository(transactionManager, jdbcTemplate)

}