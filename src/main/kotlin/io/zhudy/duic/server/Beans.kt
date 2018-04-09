package io.zhudy.duic.server

import com.auth0.jwt.algorithms.Algorithm
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
        ref<MongoClient>().getDatabase(ref<MongoProperties>().database)
    }

    bean<BCryptPasswordEncoder>()

    bean<ConcurrentMapCacheManager>()
}