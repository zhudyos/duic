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

import java.time.Instant

/**
 * 应用配置修改历史信息。
 *
 * @property id 主键
 * @property name 应用名称
 * @property profile 应用配置
 * @property description 应用描述
 * @property content 应用配置内容
 * @property token 访问令牌
 * @property ipLimit 可访问 IP
 * @property v 应用版本
 * @property createdAt 应用创建时间
 * @property updatedBy 修改者
 * @property deletedBy 删除者
 * @property users 应用所属用户
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
class AppHistory(
        var id: String = "",
        var name: String = "",
        var profile: String = "",
        var description: String = "",
        var content: String = "",
        var token: String = "",
        var ipLimit: String = "",
        var v: Int = 1,
        var createdAt: Instant? = null,
        var updatedBy: String = "",
        var deletedBy: String = "",
        var users: List<String> = emptyList()
)