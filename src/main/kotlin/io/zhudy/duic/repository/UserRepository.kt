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

import io.zhudy.duic.domain.Page
import io.zhudy.duic.domain.Pageable
import io.zhudy.duic.domain.User
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * 用户管理操作。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
interface UserRepository {

    /**
     * 添加一个新用户。
     */
    fun insert(user: User): Mono<Void>

    /**
     * 删除一个用户。
     */
    fun delete(email: String): Mono<Void>

    /**
     * 更新用户密码。
     */
    fun updatePassword(email: String, password: String): Mono<Void>

    /**
     * 根据邮箱查询用户信息。
     */
    fun findByEmail(email: String): Mono<User>

    /**
     * 分页查询用户列表。
     */
    fun findPage(pageable: Pageable): Mono<Page<User>>

    /**
     * 返回所有用户的邮箱。
     */
    fun findAllEmail(): Flux<String>

}