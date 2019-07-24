package io.zhudy.duic.web.config

import io.github.resilience4j.ratelimiter.RateLimiter
import io.zhudy.duic.web.admin.AdminResource
import io.zhudy.duic.web.security.AuthorizedHandlerFilter
import io.zhudy.duic.web.security.RootRoleHandler
import io.zhudy.duic.web.server.ServerResource
import io.zhudy.duic.web.v1.AppResource
import io.zhudy.duic.web.v1.OAIResource
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Configuration
class Routers(private val rateLimiterProvider: ObjectProvider<RateLimiter>) {

    @ConditionalOnBean(OAIResource::class)
    @Bean
    fun oaiRouter(oaiResource: OAIResource): RouterFunction<ServerResponse> = router {
        GET("/api/oai.yml", oaiResource::index)
    }

    @ConditionalOnBean(ServerResource::class)
    @Bean
    fun serverRouter(serverResource: ServerResource): RouterFunction<ServerResponse> = router {
        path("/api").nest {
            GET("/info", serverResource::info)
            GET("/health", serverResource::health)
        }

        // Deprecated. 请采用 /api/services 路径目录替换该资源接口
        path("/servers").nest {
            POST("/apps/refresh", serverResource::refreshApp)
            GET("/last-data-time", serverResource::getLastDataTime)
        }

        path("/api/servers").nest {
            POST("/apps/refresh", serverResource::refreshApp)
            GET("/last-data-time", serverResource::getLastDataTime)
        }
    }

    @ConditionalOnBean(AppResource::class)
    @Bean
    fun appRouter(appResource: AppResource): RouterFunction<ServerResponse> {
        val rf = router {
            path("/api/v1").nest {
                GET("/ssc/{name}/{profile}", appResource::getSpringCloudConfig)

                path("/apps").nest {
                    GET("/states/{name}/{profile}", appResource::getConfigState)
                    GET("/watches/{name}/{profile}", appResource::watchConfigState)
                    GET("/{name}/{profile}", appResource::getConfigByNameProfile)
                    GET("/{name}/{profile}/{key}", appResource::getConfigByNameProfileKey)
                }
            }
        }

        // 当启用限流时，添加限流过滤器
        val rateLimiter = rateLimiterProvider.ifAvailable
        if (rateLimiter != null) {
            return rf.filter(RateLimiterHandlerFilter(rateLimiter))
        }
        return rf
    }

    @ConditionalOnBean(AdminResource::class)
    @Bean
    fun adminRouter(adminResource: AdminResource): RouterFunction<ServerResponse> = router {
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
                GET("/last-data-time", adminResource::findLastDataTime)
            }

            path("/tests").nest {
                GET("/apps/names", adminResource::findAllNames)
                GET("/apps/{name}/profiles", adminResource::findProfilesByName)
            }

            path("/search").nest {
                GET("/apps", adminResource::searchAppByUser)
            }

            path("/servers").nest {
                GET("/", adminResource::loadServerStates)
            }
        }
    }.filter(AuthorizedHandlerFilter())
}