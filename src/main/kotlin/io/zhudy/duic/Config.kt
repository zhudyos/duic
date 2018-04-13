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

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.annotation.Order

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Order(0)
@ConfigurationProperties(prefix = "duic")
object Config {

    var address: String = "0.0.0.0"
    var port: Int = 7777

    /**
     * 超级管理员用户名.
     */
    var rootEmail: String = ""
    var rootPassword: String = ""
    var jwt = Jwt

    object Jwt {
        var secret: String = ""
        var expiresIn: Int = 12 * 60 // 默认12小时过期
    }
}