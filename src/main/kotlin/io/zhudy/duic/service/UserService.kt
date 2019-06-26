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
package io.zhudy.duic.service

import io.zhudy.duic.BizCodeException
import io.zhudy.duic.BizCodes
import io.zhudy.duic.Config
import io.zhudy.duic.domain.Pageable
import io.zhudy.duic.domain.User
import io.zhudy.duic.dto.ResetPasswordDto
import io.zhudy.duic.repository.UserRepository
import org.springframework.context.annotation.DependsOn
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import javax.annotation.PostConstruct

/**
 * 用户管理。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Service
@DependsOn("io.zhudy.duic.Config")
class UserService(val userRepository: UserRepository,
                  val passwordEncoder: PasswordEncoder) {

    @PostConstruct
    fun initRootUser() {
        if (!Config.enabledAutoRegRoot) {
            return
        }

        userRepository.findByEmail(Config.rootEmail).hasElement().subscribe {
            if (!it) {
                insert(User(email = Config.rootEmail, password = Config.rootPassword)).subscribe()
            }
        }
    }

    /**
     * 用户登录。
     *
     * 错误返回
     * - [BizCodes.C_2000]
     * - [BizCodes.C_2001]
     *
     * @throws BizCodeException
     */
    fun login(email: String, password: String): Mono<User> {
        return userRepository.findByEmail(email).single().map {
            if (!passwordEncoder.matches(password, it.password)) {
                throw BizCodeException(BizCodes.C_2001)
            }
            it
        }.onErrorResume(NoSuchElementException::class.java) {
            throw BizCodeException(BizCodes.C_2000)
        }.cast(User::class.java)
    }

    /**
     * 保存用户。
     */
    fun insert(user: User): Mono<*> {
        user.password = passwordEncoder.encode(user.password)
        return userRepository.insert(user)
    }

    /**
     * 更新用户密码。
     *
     * @param email 邮箱
     * @param oldPassword 原密码
     * @param newPassword 新密码
     */
    fun updatePassword(email: String, oldPassword: String, newPassword: String): Mono<Void> {
        return userRepository.findByEmail(email).single().flatMap {
            if (passwordEncoder.matches(oldPassword, it.password)) {
                return@flatMap userRepository.updatePassword(email, passwordEncoder.encode(newPassword))
            }
            throw BizCodeException(BizCodes.C_2001)
        }
    }

    /**
     * 删除用户。
     */
    fun delete(email: String): Mono<Void> = Mono.defer {
        if (email == Config.rootEmail) {
            throw BizCodeException(BizCodes.C_2002)
        }
        userRepository.delete(email)
    }

    /**
     * 重置密码。
     */
    fun resetPassword(dto: ResetPasswordDto): Mono<Void> = Mono.defer {
        if (dto.email == Config.rootEmail) {
            throw BizCodeException(BizCodes.C_2003)
        }
        userRepository.updatePassword(dto.email, passwordEncoder.encode(dto.password))
    }

    /**
     * 分页查询用户列表。
     */
    fun findPage(page: Pageable) = userRepository.findPage(page)

    /**
     * 返回所有用户的邮箱。
     */
    fun findAllEmail() = userRepository.findAllEmail()
}