package io.zhudy.duic.web.config

import io.github.resilience4j.ratelimiter.RateLimiter
import io.zhudy.duic.web.admin.AdminResource
import io.zhudy.duic.web.security.AuthorizedHandlerFilter
import io.zhudy.duic.web.security.RootRoleHandler
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
 * 资源路由器。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Configuration
class Routers(
        private val rateLimiterProvider: ObjectProvider<RateLimiter>
) {

    @Bean
    @ConditionalOnBean(OAIResource::class)
    fun oaiRouter(oaiResource: OAIResource): RouterFunction<ServerResponse> = router {
        GET("/api/oai.yml", oaiResource::index)
    }

    @Bean
    @ConditionalOnBean(AppResource::class)
    fun appRouter(appResource: AppResource): RouterFunction<ServerResponse> {
        val rf = router {
            "/api/v1".nest {
                GET("/ssc/{name}/{profile}", appResource::getSpringCloudConfig)

                GET("/apps/states/{name}/{profile}", appResource::getConfigState)
                GET("/apps/watches/{name}/{profile}", appResource::watchConfigState)
                GET("/apps/{name}/{profile}", appResource::getConfigByNameProfile)
                GET("/apps/{name}/{profile}/{key}", appResource::getConfigByNameProfileKey)
            }
        }

        // 当启用限流时，添加限流过滤器
        val rateLimiter = rateLimiterProvider.ifAvailable
        if (rateLimiter != null) {
            return rf.filter(RateLimiterHandlerFilter(rateLimiter))
        }
        return rf
    }

    @Bean
    @ConditionalOnBean(AdminResource::class)
    fun adminRouter(adminResource: AdminResource): RouterFunction<ServerResponse> = router {
        "/api/admins".nest {
            POST("/login", adminResource::login)
            GET("/user/root", adminResource::rootUser)

            POST("/users", adminResource::insertUser)
            GET("/users", adminResource::findPageUser)
            GET("/users/emails", adminResource::findAllEmail)
            DELETE("/users/{email}", RootRoleHandler(adminResource::deleteUser))
            PUT("/users/password", adminResource::updateUserPassword)
            PATCH("/users/password", RootRoleHandler(adminResource::resetUserPassword))

            POST("/apps", adminResource::insertApp)
            PUT("/apps/{name}/{profile}", adminResource::updateApp)
            PUT("/apps/contents", adminResource::updateAppContent)
            GET("/apps/user", adminResource::findAppByUser)
            DELETE("/apps/{name}/{profile}", adminResource::deleteApp)
            GET("/apps/{name}/{profile}", adminResource::findOneApp)
            GET("/apps/{name}/{profile}/histories", adminResource::findAppContentHistory)
            GET("/apps/last-data-time", adminResource::findLastDataTime)

            GET("/tests/apps/names", adminResource::findAllNames)
            GET("/tests/apps/{name}/profiles", adminResource::findProfilesByName)

            GET("/search/apps", adminResource::searchAppByUser)
        }
    }.filter(AuthorizedHandlerFilter())
}