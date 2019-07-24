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
package io.zhudy.duic.web.v1

import io.zhudy.duic.BizCode
import io.zhudy.duic.BizCodeException
import io.zhudy.duic.service.AppService
import io.zhudy.duic.vo.RequestConfigVo
import io.zhudy.duic.web.WebConstants
import io.zhudy.duic.web.body
import io.zhudy.duic.web.pathString
import org.springframework.stereotype.Controller
import org.springframework.util.StringUtils
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import java.net.Inet4Address

/**
 * `/api/v1/apps`。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Controller
class AppResource(
        private val appService: AppService
) {

    /**
     * 返回配置状态。
     */
    fun getConfigState(request: ServerRequest): Mono<ServerResponse> {
        val vo = getRequestConfigVo(request)
        return appService.getConfigState(vo).flatMap {
            ok().body(mapOf("state" to it))
        }
    }

    /**
     * 监控配置状态。
     */
    fun watchConfigState(request: ServerRequest): Mono<ServerResponse> {
        val vo = getRequestConfigVo(request)
        val oldState = request.queryParam("state").orElse("")

        return appService.watchConfigState(vo, oldState).flatMap {
            ok().body(mapOf("state" to it))
        }
    }

    /**
     * 返回适配 spring-cloud-config 的数据配置。
     */
    fun getSpringCloudConfig(request: ServerRequest): Mono<ServerResponse> {
        val vo = getRequestConfigVo(request)
        return appService.loadSpringCloudConfig(vo).flatMap {
            ok().body(it)
        }
    }

    /**
     * 根据应用名称与配置名称返回数据。
     *
     * 示例：
     * `curl https://{base_uri}/api/v1/apps/{name}/{profile}`
     */
    fun getConfigByNameProfile(request: ServerRequest): Mono<ServerResponse> {
        val vo = getRequestConfigVo(request)
        return appService.loadConfigByNameProfile(vo).flatMap {
            ok().body(it)
        }
    }

    /**
     * 根据应用名称、配置名称与配置键返回数据。
     *
     * 示例：
     * `curl https://{base_uri}/api/v1/apps/{name}/{profile}/{key}`
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
        val ip = request.headers().header(WebConstants.X_REAL_IP).firstOrNull()
        if (ip != null) {
            return ip
        }

        val swe = request.exchange()
        val address = swe.request.remoteAddress?.address
        if (address is Inet4Address) {
            return address.hostAddress
        }
        // TODO: 后期将支持 IPv6
        return ""
    }
}