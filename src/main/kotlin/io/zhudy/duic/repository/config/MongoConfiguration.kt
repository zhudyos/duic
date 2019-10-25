package io.zhudy.duic.repository.config

import io.zhudy.duic.repository.mongo.MongoAppRepositoryImpl
import io.zhudy.duic.repository.mongo.MongoServerRepositoryImpl
import io.zhudy.duic.repository.mongo.MongoUserRepositoryImpl
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.ReactiveMongoTransactionManager
import org.springframework.data.mongodb.core.ReactiveMongoOperations

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = ["duic.dbms"], havingValue = "mongodb")
@AutoConfigureAfter(MongoReactiveDataAutoConfiguration::class)
class MongoConfiguration(
        private val ops: ReactiveMongoOperations
) {

    @Bean
    @ConditionalOnMissingBean
    fun transactionManager(databaseFactory: ReactiveMongoDatabaseFactory) = ReactiveMongoTransactionManager(databaseFactory)

    @Bean
    fun userRepository() = MongoUserRepositoryImpl(ops)

    @Bean
    fun appRepository() = MongoAppRepositoryImpl(ops)

    @Bean
    fun serverRepository() = MongoServerRepositoryImpl(ops)
}