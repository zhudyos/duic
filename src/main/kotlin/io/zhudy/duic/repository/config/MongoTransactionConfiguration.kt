package io.zhudy.duic.repository.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.ReactiveMongoTransactionManager

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(ReactiveMongoDatabaseFactory::class)
class MongoTransactionConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun transactionManager(databaseFactory: ReactiveMongoDatabaseFactory) = ReactiveMongoTransactionManager(databaseFactory)

}