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
package io.zhudy.duic.web.server

import io.zhudy.duic.domain.ServerInfo
import io.zhudy.duic.dto.ServerRefreshDto
import io.zhudy.duic.service.AppService
import io.zhudy.duic.service.HealthCheckException
import io.zhudy.duic.service.ServerService
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.ServerResponse.status
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

/**
 *`/servers`。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Controller
class ServerResource(
        val serverService: ServerService,
        val appService: AppService
) {

    /**
     * 刷新内存配置。
     */
    fun refreshApp(request: ServerRequest): Mono<ServerResponse> = appService.refresh()
            .map { ServerRefreshDto(it) }
            .flatMap(ok()::syncBody)

    /**
     * 获取内存配置最后更新时间。
     */
    fun getLastDataTime(request: ServerRequest): Mono<ServerResponse> = ok().syncBody(ServerRefreshDto(appService.getMemoryLastDataTime()))

    /**
     * 返回服务信息。
     */
    fun info(request: ServerRequest): Mono<ServerResponse> = ok().body(serverService.info(), ServerInfo::class.java)

    /**
     * 健康检查。
     */
    fun health(request: ServerRequest): Mono<ServerResponse> = serverService.health().flatMap {
        ok().syncBody(mapOf("status" to "UP"))
    }.onErrorResume(HealthCheckException::class.java) {
        status(it.code).syncBody(mapOf(
                "status" to "DOWN",
                "description" to it.description
        )).toMono()
    }

}