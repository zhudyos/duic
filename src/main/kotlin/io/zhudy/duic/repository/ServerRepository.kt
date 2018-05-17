package io.zhudy.duic.repository

import io.zhudy.duic.domain.Server
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
interface ServerRepository {

    /**
     *
     */
    fun register(host: String, port: Int): Mono<*>

    /**
     *
     */
    fun unregister(host: String, port: Int): Mono<*>

    /**
     *
     */
    fun ping(host: String, port: Int): Mono<*>

    /**
     *
     */
    fun findServers(): Flux<Server>

    /**
     * 清理过期服务。
     */
    fun clean(): Mono<*>
}