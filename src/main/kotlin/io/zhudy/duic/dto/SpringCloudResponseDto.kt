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
package io.zhudy.duic.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * `spring-cloud` 配置返回数据类型。
 *
 * @property name 应用名称
 * @property profiles 应用环境
 * @property state 配置状态
 * @property propertySources 配置源列表
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
class SpringCloudResponseDto(
        val name: String,
        val profiles: List<String>,
        val state: String,
        @JsonProperty("propertySources")
        val propertySources: List<SpringCloudPropertySource>
)

/**
 * @property name 配置源名称
 * @property source 配置源
 */
data class SpringCloudPropertySource(val name: String, val source: Any)