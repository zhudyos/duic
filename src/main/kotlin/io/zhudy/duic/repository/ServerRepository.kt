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