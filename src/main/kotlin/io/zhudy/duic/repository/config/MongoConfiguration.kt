package io.zhudy.duic.repository.config

import com.mongodb.ConnectionString
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoDatabase
import io.zhudy.duic.repository.impl.MongoAppRepository
import io.zhudy.duic.repository.impl.MongoUserRepository
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Configuration
@ImportAutoConfiguration(*[MongoReactiveAutoConfiguration::class])
@ConditionalOnExpression("T(io.zhudy.duic.DBMS).forName('\${duic.dbms}') == T(io.zhudy.duic.DBMS).MongoDB")
class MongoConfiguration {

    @Bean
    fun duicMongoDatabase(mongoProperties: MongoProperties, mongoClient: MongoClient): MongoDatabase {
        val dbName = if (mongoProperties.database != null)
            mongoProperties.database
        else
            ConnectionString(mongoProperties.uri).database
        return mongoClient.getDatabase(dbName)
    }

    @Bean
    fun userRepository(mongo: MongoDatabase) = MongoUserRepository(mongo)

    @Bean
    fun appRepository(mongo: MongoDatabase) = MongoAppRepository(mongo)

}