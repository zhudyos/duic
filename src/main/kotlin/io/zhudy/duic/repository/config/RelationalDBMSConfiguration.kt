package io.zhudy.duic.repository.config

import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Configuration
@ImportAutoConfiguration(*[DataSourceAutoConfiguration::class])
@ConditionalOnExpression("!T(io.zhudy.duic.DBMS).forName('\${duic.dbms}').isNoSQL")
class RelationalDBMSConfiguration {

    @Bean
    fun transactionTemplate(transactionManager: PlatformTransactionManager) = TransactionTemplate(transactionManager)

}