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

import io.zhudy.duic.dto.NewUserDto
import io.zhudy.duic.dto.UserDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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
    fun insert(user: NewUserDto): Mono<Int>

    /**
     * 删除一个用户。
     */
    fun delete(email: String): Mono<Int>

    /**
     * 更新用户密码。
     */
    fun updatePassword(email: String, password: String): Mono<Int>

    /**
     * 根据邮箱查询用户信息。
     */
    fun findByEmail(email: String): Mono<UserDto>

    /**
     * 分页查询用户列表。
     */
    fun findPage(pageable: Pageable): Mono<Page<UserDto>>

    /**
     * 返回所有用户的邮箱。
     */
    fun findAllEmail(): Flux<String>

}