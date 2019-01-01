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
package io.zhudy.duic.web.server

import io.zhudy.duic.Config
import io.zhudy.duic.domain.ServerInfo
import io.zhudy.duic.dto.ServerRefreshDto
import io.zhudy.duic.service.AppService
import io.zhudy.duic.service.HealthCheckException
import io.zhudy.duic.service.ServerService
import io.zhudy.duic.web.body
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.ServerResponse.status
import reactor.core.publisher.toMono
import java.net.InetAddress

/**
 *`/servers`。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Controller
class ServerResource(
        serverProperties: ServerProperties,
        val serverService: ServerService,
        val appService: AppService
) {

    init {
        Config.server = Config.Server(
                host = InetAddress.getLocalHost().hostName,
                port = serverProperties.port,
                sslEnabled = serverProperties.ssl?.isEnabled ?: false
        )
    }

    /**
     *
     */
    fun refreshApp(request: ServerRequest) = appService.refresh().flatMap {
        ok().body(ServerRefreshDto(it))
    }

    /**
     *
     */
    fun getLastDataTime(request: ServerRequest) = ok().body(ServerRefreshDto(appService.getMemoryLastDataTime()))

    /**
     * 返回服务信息。
     */
    fun info(request: ServerRequest) = ok().body(serverService.info(), ServerInfo::class.java)

    /**
     * 健康检查。
     */
    fun health(request: ServerRequest) = serverService.health().flatMap {
        ok().body(emptyMap<String, String>())
    }.onErrorResume(HealthCheckException::class.java) {
        status(it.code).body(mapOf("description" to it.description)).toMono()
    }

}