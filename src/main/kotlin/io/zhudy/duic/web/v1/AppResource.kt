package io.zhudy.duic.web.v1

import io.zhudy.duic.BizCode
import io.zhudy.duic.BizCodeException
import io.zhudy.duic.service.AppService
import io.zhudy.duic.web.WebConstants
import io.zhudy.duic.web.body
import io.zhudy.duic.web.pathString
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Controller
class AppResource(val appService: AppService) {

    /**
     *
     */
    fun getConfigState(request: ServerRequest): Mono<ServerResponse> {
        val name = request.pathString("name")
        val profiles = getProfiles(request)
        val configTokens = request.headers().header(WebConstants.X_CONFIG_TOKEN)
        return appService.getConfigState(name, profiles, configTokens).flatMap {
            ok().body(mapOf("state" to it))
        }
    }

    /**
     *
     */
    fun getSpringCloudConfig(request: ServerRequest): Mono<ServerResponse> {
        val name = request.pathString("name")
        val profiles = getProfiles(request)
        val configTokens = request.headers().header(WebConstants.X_CONFIG_TOKEN)
        return appService.loadSpringCloudConfig(name, profiles, configTokens).flatMap {
            ok().body(it)
        }
    }

    /**
     *
     */
    fun getConfigByNameProfile(request: ServerRequest): Mono<ServerResponse> {
        val name = request.pathString("name")
        val profiles = getProfiles(request)
        val configTokens = request.headers().header(WebConstants.X_CONFIG_TOKEN)
        return appService.loadConfigByNameProfile(name, profiles, configTokens).flatMap {
            ok().body(it)
        }
    }

    /**
     *
     */
    fun getConfigByNameProfileKey(request: ServerRequest): Mono<ServerResponse> {
        val name = request.pathString("name")
        val profiles = getProfiles(request)
        val configTokens = request.headers().header(WebConstants.X_CONFIG_TOKEN)
        val key = request.pathString("key")
        return appService.loadConfigByNameProfileKey(name, profiles, configTokens, key).flatMap {
            ok().body(it)
        }
    }

    private fun getProfiles(request: ServerRequest): List<String> {
        val profiles = request.pathString("profile").split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (profiles.isEmpty()) {
            throw BizCodeException(BizCode.Classic.C_999, "缺少 profile 参数")
        }
        return profiles
    }
}