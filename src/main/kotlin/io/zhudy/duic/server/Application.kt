package io.zhudy.duic.server

import com.auth0.jwt.algorithms.Algorithm
import io.zhudy.duic.Config
import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.dao.PersistenceExceptionTranslationAutoConfiguration
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
import org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.mongodb.MongoDbFactory
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import reactor.core.publisher.Hooks

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@SpringBootApplication(exclude = [
RestTemplateAutoConfiguration::class,
ErrorWebFluxAutoConfiguration::class,
CodecsAutoConfiguration::class,
PersistenceExceptionTranslationAutoConfiguration::class,
TransactionAutoConfiguration::class,
ValidationAutoConfiguration::class])
@ComponentScan("io.zhudy.duic")
@EnableConfigurationProperties(Config::class)
@EnableReactiveMongoRepositories
class Application {

    @Bean
    fun mongoConverter(factory: MongoDbFactory): MappingMongoConverter {
        val dbRefResolver = DefaultDbRefResolver(factory)
        val conversions = MongoCustomConversions(emptyList<Any>())

        val mappingContext = MongoMappingContext()
        mappingContext.setSimpleTypeHolder(conversions.simpleTypeHolder)
        mappingContext.afterPropertiesSet()

        val converter = MappingMongoConverter(dbRefResolver, mappingContext)
        converter.setCustomConversions(conversions)
        converter.typeMapper = DefaultMongoTypeMapper(null)
        converter.afterPropertiesSet()
        return converter
    }

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun jwtAlgorithm(config: Config): Algorithm = Algorithm.HMAC256(Config.jwt.secret)

    @Bean
    fun cacheManager() = ConcurrentMapCacheManager()

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplicationBuilder(Application::class.java)
                    .bannerMode(Banner.Mode.LOG)
                    .run(*args)
        }
    }
}