/**
 * Copyright 2017-2019 the original author or authors
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
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import io.zhudy.duic.ApplicationUnusableEvent
import io.zhudy.duic.ApplicationUsableEvent
import io.zhudy.duic.Config
import org.simplejavamail.mailer.Mailer
import org.simplejavamail.mailer.MailerBuilder
import org.springframework.boot.ExitCodeGenerator
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.dao.PersistenceExceptionTranslationAutoConfiguration
import org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.net.InetAddress
import java.time.Duration
import java.util.*


/**
 * 程序入口。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
@SpringBootApplication(
        scanBasePackages = [
            "io.zhudy.duic"
        ],
        exclude = [
            MongoReactiveAutoConfiguration::class,
            RestTemplateAutoConfiguration::class,
            ErrorWebFluxAutoConfiguration::class,
            CodecsAutoConfiguration::class,
            PersistenceExceptionTranslationAutoConfiguration::class,
            TransactionAutoConfiguration::class,
            ValidationAutoConfiguration::class
        ]
)
class Application {

    @Bean
    fun kotlinPropertyConfigurer() = PropertySourcesPlaceholderConfigurer().apply {
        setPlaceholderPrefix("%{")
        setTrimValues(true)
        setIgnoreUnresolvablePlaceholders(true)
    }

    @Bean("io.zhudy.duic.Config")
    @ConfigurationProperties(prefix = "duic")
    fun config(serverProperties: ServerProperties): Config {
        val c = Config
        c.server = Config.Server(
                host = InetAddress.getLocalHost().hostName,
                port = serverProperties.port,
                sslEnabled = serverProperties.ssl?.isEnabled ?: false
        )
        return c
    }

    @Bean
    fun jwtAlgorithm(config: Config) = Algorithm.HMAC256(config.jwt.secret)!!

    @Bean
    fun jackson2ObjectMapperBuilderCustomizer() = Jackson2ObjectMapperBuilderCustomizer {
        it.timeZone(TimeZone.getDefault())
    }

    @Bean
    fun bCryptPasswordEncoder() = BCryptPasswordEncoder()

    @Bean
    @ConditionalOnExpression("\${duic.concurrent.request-limit-for-period:-1} >= 1")
    fun rateLimiter(config: Config): RateLimiter {
        val c = RateLimiterConfig.custom()
                .timeoutDuration(Duration.ofMillis(100))
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .limitForPeriod(config.concurrent.requestLimitForPeriod)
                .build()
        return RateLimiter.of("web-rate-limiter", c)
    }

    @Bean
    @ConditionalOnProperty("duic.notify-email.enabled")
    fun mailer(config: Config): Mailer {
        return MailerBuilder
                .withSMTPServer(config.notifyEmail.smtpHost, config.notifyEmail.smtpPort, config.notifyEmail.fromEmail, config.notifyEmail.fromPassword)
                .buildMailer()
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            // spring-boot
            // https://github.com/zhudyos/duic/issues/17
            // 搜索 /etc/duic 目录中的配置文件
            runApplication<Application>("--spring.config.additional-location=/etc/duic/") {
                setBanner(DuicBanner())

                val usable = ApplicationListener<ApplicationUsableEvent> {
                    println("""
====================================================================================
//                           duic start successful                                //
====================================================================================
""")
                }


                val unusable = ApplicationListener<ApplicationUnusableEvent> { event ->
                    SpringApplication.exit(event.applicationContext, ExitCodeGenerator { 1 })
                }

                addListeners(usable, unusable)
            }
        }

    }
}