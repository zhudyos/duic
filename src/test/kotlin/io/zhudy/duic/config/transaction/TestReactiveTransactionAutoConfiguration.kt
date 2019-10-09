package io.zhudy.duic.config.transaction

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.TransactionCallback
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ReactiveTransactionManager::class)
class TestReactiveTransactionAutoConfiguration {

    @Bean
    fun transactionalOperator(transactionManager: ReactiveTransactionManager): TransactionalOperator {
        val operator = TransactionalOperator.create(transactionManager)
        return object : TransactionalOperator {
            override fun <T : Any?> execute(action: TransactionCallback<T>): Flux<T> {
                return operator.execute { t ->
                    // 单元测试环境事务自动回滚
                    t.setRollbackOnly()
                    action.doInTransaction(t)
                }
            }
        }
    }
}