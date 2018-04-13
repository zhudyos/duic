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
package io.zhudy.duic.server

import com.auth0.jwt.algorithms.Algorithm
import com.mongodb.ConnectionString
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoDatabase
import io.zhudy.duic.Config
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.support.beans
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
fun beans() = beans {

    bean<Algorithm> {
        Algorithm.HMAC256(ref<Config>().jwt.secret)
    }

    bean {
        Jackson2ObjectMapperBuilderCustomizer {
            it.timeZone(TimeZone.getDefault())
        }
    }

    bean<MongoDatabase>("duicMongoDatabase") {
        val props = ref<MongoProperties>()
        val database = if (props.database != null) props.database else ConnectionString(props.uri).database
        ref<MongoClient>().getDatabase(database)
    }

    bean<BCryptPasswordEncoder>()

    bean<ConcurrentMapCacheManager>()
}