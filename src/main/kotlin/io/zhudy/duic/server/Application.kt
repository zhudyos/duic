package io.zhudy.duic.server

import io.zhudy.duic.Config
import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.dao.PersistenceExceptionTranslationAutoConfiguration
import org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan


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
class Application {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<Application>(*args) {
                addInitializers(BeansInitializer())
                setBannerMode(Banner.Mode.LOG)
            }
        }
    }
}