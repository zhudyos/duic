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
package io.zhudy.duic

/**
 * `duic` 基础配置。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
object Config {

    /**
     * 服务信息。
     */
    lateinit var server: Server
    /**
     * 超级管理员用户名。
     */
    var rootEmail: String = ""
    /**
     * 超级管理员默认密码。
     */
    var rootPassword: String = ""
    /**
     * 管理控制台 JWT 生成、管理信息。
     */
    var jwt = Jwt()
    /**
     * 服务并发限制。
     */
    val concurrent = Concurrent()

    class Jwt {
        /**
         * JWT 生成密钥。
         */
        var secret: String = ""
        /**
         * JWT 过期时间。
         */
        var expiresIn: Long = 12 * 60 // 默认2小时过期
    }

    /**
     * 服务信息。
     *
     * @property host 主机地址
     * @property port 服务端口
     * @property sslEnabled 是否启用 SSL
     */
    class Server(
            val host: String,
            val port: Int,
            val sslEnabled: Boolean
    )

    /**
     * 服务并发限制。
     */
    class Concurrent {
        /**
         * 服务在一个周期内最大支持的并发请求。
         */
        var requestLimitForPeriod: Int = -1
        /**
         * 服务最大支持的并发监控请求。
         */
        var watchRequestLimit: Int = -1
        /**
         * 请求达到一定的比例时进行告警。
         */
        var warnRateThreshold: Float = 0.8f
    }
}