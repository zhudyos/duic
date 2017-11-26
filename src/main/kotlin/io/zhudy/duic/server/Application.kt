package io.zhudy.duic.server

import io.zhudy.duic.Config
import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.data.mongodb.MongoDbFactory
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.mapping.MongoMappingContext

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@SpringBootApplication
@ComponentScan("io.zhudy.duic")
class Application {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConfigurationProperties("app")
    fun config() = Config

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

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplicationBuilder(Application::class.java)
                    .bannerMode(Banner.Mode.LOG)
                    .run(*args)
        }
    }
}