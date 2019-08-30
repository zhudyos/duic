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
package io.zhudy.duic.bak.repository

import io.zhudy.duic.domain.Server
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

/**
 * 集群服务管理。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
interface ServerRepository {

    companion object {
        /**
         * 服务超时时间为1分钟。
         */
        val ACTIVE_TIMEOUT: Duration = Duration.ofMinutes(1)

        /**
         * 删除2分钟未活跃的服务。
         */
        val CLEAN_BEFORE: Duration = Duration.ofMinutes(2)
    }

    /**
     * 注册服务。
     *
     * @param host 服务主机
     * @param port 服务端口
     */
    fun register(host: String, port: Int): Mono<Void>

    /**
     * 下线集群服务。
     *
     * @param host 服务主机
     * @param port 服务端口
     */
    fun unregister(host: String, port: Int): Mono<Void>

    /**
     * 服务心跳。
     *
     * @param host 服务主机
     * @param port 服务端口
     */
    fun ping(host: String, port: Int): Mono<Void>

    /**
     * 返回集群服务列表。
     */
    fun findServers(): Flux<Server>

    /**
     * 清理过期服务。
     */
    fun clean(): Mono<Void>

    /**
     * 查询数据库版本。
     */
    fun findDbVersion(): Mono<String>
}