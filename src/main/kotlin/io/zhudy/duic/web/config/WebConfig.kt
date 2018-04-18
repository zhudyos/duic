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
package io.zhudy.duic.web.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.zhudy.duic.web.WebConstants
import io.zhudy.duic.web.admin.AdminResource
import io.zhudy.duic.web.security.AuthorizedHandlerFilter
import io.zhudy.duic.web.security.RootRoleHandler
import io.zhudy.duic.web.v1.AppResource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.CacheControl
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.server.router
import org.springframework.web.server.WebFilter
import java.util.concurrent.TimeUnit

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Configuration
@EnableWebFlux
class WebConfig(val objectMapper: ObjectMapper,
                val appResource: AppResource,
                val adminResource: AdminResource) : WebFluxConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/v1/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .maxAge(TimeUnit.DAYS.toSeconds(7))
    }

    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        val defaults = configurer.defaultCodecs()
        defaults.jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper))
        defaults.jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper))
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/public-web-resources/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
    }

    @Bean
    fun webFilter() = WebFilter { exchange, chain ->
        exchange.attributes.put(WebConstants.SERVER_WEB_EXCHANGE_ATTR, exchange)
        chain.filter(exchange)
    }

    @Bean
    fun mainRouter() = router {
        path("/api/v1").nest {
            GET("/ssc/{name}/{profile}", appResource::getSpringCloudConfig)

            path("/apps").nest {
                GET("/states/{name}/{profile}", appResource::getConfigState)
                GET("/{name}/{profile}", appResource::getConfigByNameProfile)
                GET("/{name}/{profile}/{key}", appResource::getConfigByNameProfileKey)
            }
        }
    }

    @Bean
    fun adminRouter() = router {
        path("/api/admins").nest {
            POST("/login", adminResource::login)
            GET("/user/root", adminResource::rootUser)

            path("/users").nest {
                POST("/", adminResource::insertUser)
                GET("/", adminResource::findPageUser)
                GET("/emails", adminResource::findAllEmail)
                DELETE("/{email}", RootRoleHandler(adminResource::deleteUser))
                PUT("/password", adminResource::updateUserPassword)
                PATCH("/password", RootRoleHandler(adminResource::resetUserPassword))
            }

            path("/apps").nest {
                POST("/", adminResource::insertApp)
                PUT("/", adminResource::updateApp)
                PUT("/contents", adminResource::updateAppContent)
                GET("/user", adminResource::findAppByUser)
                DELETE("/{name}/{profile}", adminResource::deleteApp)
                GET("/{name}/{profile}", adminResource::findOneApp)
                GET("/{name}/{profile}/histories", adminResource::findAppContentHistory)
                POST("/duplicates/{name}/{profile}", adminResource::insertAppForApp)
            }

            path("/tests").nest {
                GET("/apps/names", adminResource::findAllNames)
                GET("/apps/{name}/profiles", adminResource::findProfilesByName)
            }

            path("/search").nest {
                GET("/apps", adminResource::searchAppByUser)
            }
        }
    }.filter(AuthorizedHandlerFilter())!!

}