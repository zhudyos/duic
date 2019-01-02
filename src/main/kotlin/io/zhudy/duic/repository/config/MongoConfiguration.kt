/**
 * Copyright 2017-2018 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zhudy.duic.repository.config

import com.mongodb.ConnectionString
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoDatabase
import io.zhudy.duic.repository.impl.MongoAppRepository
import io.zhudy.duic.repository.impl.MongoServerRepository
import io.zhudy.duic.repository.impl.MongoUserRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.autoconfigure.mongo.ReactiveMongoClientFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import javax.annotation.PreDestroy

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Configuration
@ConditionalOnExpression("T(io.zhudy.duic.DBMS).forName('\${duic.dbms}') == T(io.zhudy.duic.DBMS).MongoDB")
@EnableConfigurationProperties(MongoProperties::class)
class MongoConfiguration {

    private var mongo: MongoClient? = null

    @PreDestroy
    fun destroy() {
        mongo?.apply {
            close()
        }
    }

    @Bean
    fun mongoClient(properties: MongoProperties, environment: Environment): MongoClient {
        val factory = ReactiveMongoClientFactory(properties, environment, null)
        mongo = factory.createMongoClient(null)
        return mongo!!
    }

    @Bean
    fun duicMongoDatabase(properties: MongoProperties, mongoClient: MongoClient): MongoDatabase {
        val dbName = if (properties.database != null)
            properties.database
        else
            ConnectionString(properties.uri).database
        return mongoClient.getDatabase(dbName)
    }

    @Bean
    fun userRepository(mongo: MongoDatabase) = MongoUserRepository(mongo)

    @Bean
    fun appRepository(mongo: MongoDatabase) = MongoAppRepository(mongo)

    @Bean
    fun serverRepository(mongo: MongoDatabase) = MongoServerRepository(mongo)

}