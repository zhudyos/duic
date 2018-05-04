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
package io.zhudy.duic

/**
 * `duic` 基础配置。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
object Config {

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
    var jwt = Jwt

    object Jwt {
        /**
         * JWT 生成密钥。
         */
        var secret: String = ""
        /**
         * JWT 过期时间。
         */
        var expiresIn: Int = 12 * 60 // 默认2小时过期
    }
}