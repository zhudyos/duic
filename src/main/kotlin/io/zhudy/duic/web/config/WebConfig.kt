package io.zhudy.duic.web.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.zhudy.duic.web.admin.AdminResource
import io.zhudy.duic.web.security.AuthorizedHandlerFilter
import io.zhudy.duic.web.security.RootRoleHandler
import io.zhudy.duic.web.v1.AppResource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.server.router
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
        registry.addMapping("/api/v1")
                .allowedOrigins("*")
                .allowedMethods("*")
                .maxAge(TimeUnit.DAYS.toSeconds(7))
    }

    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        val defaults = configurer.defaultCodecs()
        defaults.jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper))
        defaults.jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper))
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
                DELETE("/{email}", adminResource::deleteUser)
                DELETE("/{email}", RootRoleHandler(adminResource::deleteUser))
                PATCH("/password", RootRoleHandler(adminResource::resetUserPassword))
            }

            path("/apps").nest {
                POST("/", adminResource::insertApp)
                PUT("/", adminResource::updateContent)
                DELETE("/{name}/{profile}", adminResource::deleteApp)
                GET("/user", adminResource::findAppByUser)
                GET("/{name}/{profile}", adminResource::findOneApp)
            }
        }
    }.filter(AuthorizedHandlerFilter())!!

}