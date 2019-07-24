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

/**
 * 服务信息实体。
 *
 * @property name 应用名称
 * @property version 应用版本
 * @property dbName 数据库名称
 * @property dbVersion 数据库版本
 * @property osName 操作系统名称
 * @property osVersion 操作系统版本
 * @property osArch 操作系统位
 * @property javaVersion Java版本
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
data class ServerInfo(
        val name: String,
        val version: String,
        val dbName: String,
        val dbVersion: String,
        val osName: String,
        val osVersion: String,
        val osArch: String,
        val javaVersion: String
)