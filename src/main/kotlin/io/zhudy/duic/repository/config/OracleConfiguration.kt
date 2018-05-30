package io.zhudy.duic.repository.config

import io.zhudy.duic.repository.impl.OracleAppRepository
import io.zhudy.duic.repository.impl.OracleServerRepository
import io.zhudy.duic.repository.impl.OracleUserRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.PlatformTransactionManager

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Configuration
@ConditionalOnExpression("T(io.zhudy.duic.DBMS).forName('\${duic.dbms}') == T(io.zhudy.duic.DBMS).Oracle")
class OracleConfiguration {

    @Bean
    fun userRepository(transactionManager: PlatformTransactionManager, jdbcTemplate: NamedParameterJdbcTemplate)
            = OracleUserRepository(transactionManager, jdbcTemplate)

    @Bean
    fun appRepository(transactionManager: PlatformTransactionManager, jdbcTemplate: NamedParameterJdbcTemplate)
            = OracleAppRepository(transactionManager, jdbcTemplate)

    @Bean
    fun serverRepository(transactionManager: PlatformTransactionManager, jdbcTemplate: NamedParameterJdbcTemplate)
            = OracleServerRepository(transactionManager, jdbcTemplate)

}