package io.zhudy.duic.repository.config

import io.zhudy.duic.repository.impl.MySQLAppRepository
import io.zhudy.duic.repository.impl.MySQLUserRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.PlatformTransactionManager

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Configuration
@ConditionalOnExpression("T(io.zhudy.duic.DBMS).forName('\${duic.dbms}') == T(io.zhudy.duic.DBMS).MySQL")
class MySQLConfiguration {

    @Bean
    fun userRepository(transactionManager: PlatformTransactionManager, jdbcTemplate: NamedParameterJdbcTemplate)
            = MySQLUserRepository(transactionManager, jdbcTemplate)

    @Bean
    fun appRepository(transactionManager: PlatformTransactionManager, jdbcTemplate: NamedParameterJdbcTemplate)
            = MySQLAppRepository(transactionManager, jdbcTemplate)
}