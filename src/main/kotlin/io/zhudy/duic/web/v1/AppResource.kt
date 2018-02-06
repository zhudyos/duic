package io.zhudy.duic.web.v1

import io.zhudy.duic.BizCode
import io.zhudy.duic.BizCodeException
import io.zhudy.duic.vo.RequestConfigVo
import io.zhudy.duic.service.AppService
import io.zhudy.duic.web.WebConstants
import io.zhudy.duic.web.body
import io.zhudy.duic.web.pathString
import org.springframework.stereotype.Controller
import org.springframework.util.StringUtils
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
        val vo = getRequestConfigVo(request)
        return appService.getConfigState(vo).flatMap {
            ok().body(mapOf("state" to it))
        }
    }

    /**
     *
     */
    fun getSpringCloudConfig(request: ServerRequest): Mono<ServerResponse> {
        val vo = getRequestConfigVo(request)
        return appService.loadSpringCloudConfig(vo).flatMap {
            ok().body(it)
        }
    }

    /**
     *
     */
    fun getConfigByNameProfile(request: ServerRequest): Mono<ServerResponse> {
        val vo = getRequestConfigVo(request)
        return appService.loadConfigByNameProfile(vo).flatMap {
            ok().body(it)
        }
    }

    /**
     *
     */
    fun getConfigByNameProfileKey(request: ServerRequest): Mono<ServerResponse> {
        val vo = getRequestConfigVo(request)
        vo.key = request.pathString("key")
        return appService.loadConfigByNameProfileKey(vo).flatMap {
            ok().body(it)
        }
    }

    private fun getRequestConfigVo(request: ServerRequest): RequestConfigVo {
        return RequestConfigVo(
                name = request.pathString("name"),
                profiles = getProfiles(request),
                configTokens = getConfigToken(request),
                clientIpv4 = getClientIp(request)
        )
    }

    private fun getProfiles(request: ServerRequest): List<String> {
        val profiles = request.pathString("profile").split(",").filter { it.isNotEmpty() }
        if (profiles.isEmpty()) {
            throw BizCodeException(BizCode.Classic.C_999, "缺少 profile 参数")
        }
        return profiles
    }

    private fun getConfigToken(request: ServerRequest): List<String> {
        val s = request.headers().header(WebConstants.X_CONFIG_TOKEN).firstOrNull()
        if (s.isNullOrEmpty()) {
            return emptyList()
        }
        return StringUtils.commaDelimitedListToStringArray(s).toList()
    }

    private fun getClientIp(request: ServerRequest): String {
        return request.headers().header(WebConstants.X_FORWARDED_FOR).firstOrNull()
                ?: request.attribute(WebConstants.REMOTE_HOST_ATTR).orElse("") as String
    }
}