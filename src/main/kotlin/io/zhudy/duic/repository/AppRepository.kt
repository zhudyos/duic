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
package io.zhudy.duic.repository

import io.zhudy.duic.domain.*
import io.zhudy.duic.dto.AppQueryDto
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

/**
 * 应用配置操作。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
interface AppRepository {

    /**
     * 保存应用配置信息。
     *
     * @param app 应用配置信息
     */
    fun insert(app: App): Mono<Int>

    /**
     * 保存应用配置历史信息。
     *
     * @param appHistory 应用配置历史信息
     */
    fun insertHistory(appHistory: AppHistory): Mono<Int>

    /**
     * 删除应用配置信息，并在 `app_history` 中保存已删除的应用配置信息。
     *
     * @param name 应用名称
     * @param profile 应用配置
     */
    fun delete(name: String, profile: String): Mono<Int>

    /**
     * 返回指定的应用配置信息。
     *
     * @param name 应用名称
     * @param profile 应用配置
     */
    fun findOne(name: String, profile: String): Mono<App>

    /**
     * 更新指定的应用配置信息。
     *
     * @param app 更新的应用配置信息
     */
    fun update(app: App): Mono<Int>

    /**
     * 更新应用配置信息。
     *
     * @param name 应用名称
     * @param profile 应用配置
     * @param content 应用配置内容
     * @return 历史版本的应用配置信息
     */
    fun updateContent(name: String, profile: String, content: String): Mono<Int>

    /**
     * 返回所有应用配置信息，并且按更新时间 `updated_at` 升序排列。
     */
    fun findAll(): Flux<App>

    /**
     * 搜索应用配置并分页返回。
     *
     * @param q 搜索关键字
     * @param pageable 分页参数
     */
    fun searchPage(query: AppQueryDto, pageable: Pageable): Mono<Page<App>>

    /**
     * 返回大于指定更新时间的应用配置信息，并且按更新时间 `updated_at` 升序排列。
     *
     * @param updateAt 应用更新时间
     */
    fun findByUpdatedAt(updateAt: Instant): Flux<App>

    /**
     * 返回应用配置最新 50 条修改记录信息。
     *
     * @param name 应用名称
     * @param profile 应用配置
     */
    fun findLast50History(name: String, profile: String): Flux<AppContentHistory>

    /**
     * 返回所有的应用名称。
     */
    fun findAllNames(): Flux<String>

    /**
     * 返回应用名称下所有的应用配置名称。
     */
    fun findProfilesByName(name: String): Flux<String>

    /**
     * 返回指定时间以后删除的应用配置信息。
     *
     * @param createdAt 删除时间
     */
    fun findDeletedByCreatedAt(createdAt: Instant): Flux<AppHistory>

    /**
     * 返回数据库中最新配置信息的修改时间戳。
     */
    fun findLastDataTime(): Mono<Long>

}