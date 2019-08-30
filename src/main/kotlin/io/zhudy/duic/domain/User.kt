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
package io.zhudy.duic.domain

import java.time.LocalDateTime

/**
 * 用户信息数据类型。
 *
 * @property email 登录邮箱
 * @property password 登录密码
 * @property createdAt 创建时间
 * @property updatedAt 修改时间
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
class User(
        var email: String = "",
        var password: String = "",
        var createdAt: LocalDateTime? = null,
        var updatedAt: LocalDateTime? = null
)