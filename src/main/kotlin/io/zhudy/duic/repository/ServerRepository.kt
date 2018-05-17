package io.zhudy.duic.repository

import io.zhudy.duic.domain.Server
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
interface ServerRepository {

    companion object {

        /**
         * 服务超时时间为1分钟。
         */
        const val ACTIVE_TIMEOUT_MINUTES = 1
        /**
         * 删除2分钟未活跃的服务。
         */
        const val CLEAN_BEFORE_MINUTES = 2
    }

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