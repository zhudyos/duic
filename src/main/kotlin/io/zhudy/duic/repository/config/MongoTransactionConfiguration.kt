package io.zhudy.duic.repository.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.ReactiveMongoTransactionManager
import org.springframework.data.mongodb.core.ReactiveMongoOperations

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(ReactiveMongoDatabaseFactory::class)
class MongoTransactionConfiguration(
        ops: ReactiveMongoOperations
) {

    init {
//        ops.createCollection("user").subscribe()
    }

    @Bean
    @ConditionalOnMissingBean
    fun transactionManager(databaseFactory: ReactiveMongoDatabaseFactory) = ReactiveMongoTransactionManager(databaseFactory)

}