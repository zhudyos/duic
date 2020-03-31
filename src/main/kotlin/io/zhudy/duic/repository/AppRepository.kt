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

import io.zhudy.duic.domain.App
import io.zhudy.duic.domain.AppContentHis
import io.zhudy.duic.domain.AppHis
import io.zhudy.duic.domain.AppPair
import io.zhudy.duic.vo.AppVo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

/**
 * 应用配置操作。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
interface AppRepository {

    /**
     * 下一个全局版本号.
     */
    fun nextGv(): Mono<Long>

    /**
     * 保存应用配置信息。
     *
     * @param vo 应用配置信息
     */
    fun insert(vo: AppVo.NewApp): Mono<Int>

    /**
     * 保存应用配置历史信息。
     *
     * @param appHis 应用配置历史信息
     */
    fun insertHis(appHis: AppHis): Mono<Int>

    /**
     * 删除应用配置信息，并在 `app_history` 中保存已删除的应用配置信息。
     *
     * @param ap 应用标识
     */
    fun delete(ap: AppPair): Mono<Int>

    /**
     * 返回指定的应用配置信息。
     *
     * @param ap 应用标识
     */
    fun findOne(ap: AppPair): Mono<App>

    /**
     * 更新指定的应用配置信息。
     *
     * @param ap 应用标识
     * @param vo 更新的应用配置信息
     */
    fun update(ap: AppPair, vo: AppVo.UpdateBasicInfo): Mono<Int>

    /**
     * 更新应用配置信息。
     *
     * @param vo 修改的应用配置信息
     * @return 返回最新的数据版本号
     */
    fun updateContent(ap: AppPair, vo: AppVo.UpdateContent): Mono<Int>

    /**
     * 返回所有应用配置信息，并且按更新时间 `updated_at` 升序排列。
     */
    fun findAll(): Flux<App>

    /**
     * 搜索应用配置并分页返回。
     *
     * @param vo 搜索条件
     * @param pageable 分页参数
     */
    fun search(vo: AppVo.UserQuery, pageable: Pageable): Mono<Page<App>>

    /**
     * 返回大于指定更新时间的应用配置信息，并且按更新时间 `updated_at` 升序排列。
     *
     * @param time 应用更新时间
     */
    fun find4UpdatedAt(time: LocalDateTime): Flux<App>

    /**
     * 返回应用配置最新 50 条修改记录信息。
     *
     * @param ap 应用标识
     * @param pageable 应用配置
     */
    fun findAppHistory(ap: AppPair, pageable: Pageable): Flux<AppContentHis>

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
     * @param time 删除时间
     */
    fun findLatestDeleted(time: LocalDateTime): Flux<AppHis>

    /**
     * 返回数据库中最新配置信息的修改时间戳。
     */
    fun findLastDataTime(): Mono<Long>

}