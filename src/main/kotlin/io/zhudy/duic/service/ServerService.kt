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
package io.zhudy.duic.service

import io.zhudy.duic.Config
import io.zhudy.duic.dto.ServerRefreshDto
import io.zhudy.duic.dto.ServerStateDto
import io.zhudy.duic.repository.ServerRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Service
class ServerService(
        val serverRepository: ServerRepository,
        val webClientBuilder: WebClient.Builder
) {

    private val log = LoggerFactory.getLogger(ServerService::class.java)

    companion object {
        private const val PING_DELAY = 30 * 1000L
    }

    private val failServerRefreshDto = ServerRefreshDto(lastDataTime = -1)

    /**
     *
     */
    @EventListener(classes = [ApplicationReadyEvent::class])
    fun register() {
        serverRepository.register(Config.server.host, Config.server.port).doOnError {
            log.error("register 服务失败: ${Config.server.host}:${Config.server.port}", it)
        }.subscribe()
    }

    /**
     *
     */
    @EventListener(classes = [ContextClosedEvent::class])
    fun unregister() {
        serverRepository.register(Config.server.host, Config.server.port).doOnError {
            log.error("unregister 服务失败: ${Config.server.host}:${Config.server.port}", it)
        }.subscribe()
    }

    @Scheduled(initialDelay = PING_DELAY, fixedDelay = PING_DELAY)
    fun clockPing() {
        serverRepository.clean()
                .subscribeOn(Schedulers.parallel())
                .subscribe()

        serverRepository.ping(Config.server.host, Config.server.port)
                .subscribe()
    }

    /**
     * 加载集群服务中配置最后更新的状态。
     */
    fun loadServerStates() = serverRepository.findServers().flatMapSequential { s ->
        val server = Config.server
        val schema = if (server.sslEnabled) "https" else "http"
        webClientBuilder.baseUrl("$schema://${s.host}:${s.port}")
                .build()
                .get()
                .uri("/servers/last-data-time")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus({ !it.is2xxSuccessful }, {
                    Mono.error(IllegalStateException("http status: ${it.statusCode().value()}"))
                })
                .bodyToMono(ServerRefreshDto::class.java)
                .subscribeOn(Schedulers.elastic())
                .onErrorReturn(failServerRefreshDto)
                .map {
                    ServerStateDto(
                            host = s.host,
                            port = s.port,
                            initAt = s.initAt,
                            activeAt = s.activeAt,
                            lastDataTime = it.lastDataTime
                    )
                }
    }

}