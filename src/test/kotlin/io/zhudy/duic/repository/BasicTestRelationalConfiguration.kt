package io.zhudy.duic.repository

import io.zhudy.duic.config.transaction.TestReactiveTransactionAutoConfiguration
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.r2dbc.ConnectionFactoryAutoConfiguration
import org.springframework.context.annotation.Import

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Import(value = [
    ConnectionFactoryAutoConfiguration::class,
    R2dbcDataAutoConfiguration::class,
    R2dbcTransactionManagerAutoConfiguration::class,
    TestReactiveTransactionAutoConfiguration::class
])
class BasicTestRelationalConfiguration