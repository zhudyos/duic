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

import io.zhudy.duic.BizCodes
import io.zhudy.duic.Config
import io.zhudy.duic.annotation.NoIntegrationTest
import io.zhudy.duic.dto.UserDto
import io.zhudy.duic.repository.UserRepository
import io.zhudy.duic.vo.UserVo
import io.zhudy.kitty.core.biz.BizCode
import io.zhudy.kitty.core.biz.BizCodeException
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import javax.annotation.PostConstruct

/**
 * 用户管理。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Service
@DependsOn(Config.BEAN_NAME)
class UserService(
        private val userRepository: UserRepository,
        private val passwordEncoder: PasswordEncoder
) {

    // FIXME 完善业务，该业务应该被提取到外部
    @NoIntegrationTest
    @Configuration
    inner class Lifecycle {

        @PostConstruct
        fun init() {
            userRepository.findByEmail(Config.rootEmail)
                    .thenEmpty { insert(UserVo.NewUser(Config.rootEmail, Config.rootPassword)) }
                    .subscribe()
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
    fun login(email: String, password: String): Mono<UserDto> = userRepository.findByEmail(email).single()
            .doOnNext {
                if (!passwordEncoder.matches(password, it.password)) {
                    throw BizCodeException(BizCodes.C_2001)
                }
            }
            .onErrorResume(NoSuchElementException::class.java) {
                throw BizCodeException(BizCodes.C_2000)
            }

    /**
     * 保存用户。
     */
    @Transactional
    fun insert(vo: UserVo.NewUser): Mono<Void> = Mono.defer {
        val newUser = vo.copy(password = passwordEncoder.encode(vo.password))

        userRepository.insert(newUser)
                .doOnNext { n ->
                    if (n != 1) {
                        throw BizCodeException(BizCode.C811, "新增 [${vo.email}] 用户，预期影响行记录 1 实际影响 $n")
                    }
                }
                .then()
    }

    /**
     * 更新用户密码。
     *
     * @param vo 修改密码值对象
     */
    @Transactional
    fun updatePassword(vo: UserVo.UpdatePassword): Mono<Void> = Mono.defer {
        userRepository.findByEmail(vo.email).single()
                .onErrorMap(NoSuchElementException::class.java) { BizCodeException(BizCodes.C_2000) }
                .doOnNext {
                    if (!passwordEncoder.matches(vo.oldPassword, it.password)) {
                        throw BizCodeException(BizCodes.C_2001)
                    }
                }
                .then(userRepository.updatePassword(vo.email, passwordEncoder.encode(vo.newPassword)))
                .doOnNext { n ->
                    if (n != 1) {
                        throw BizCodeException(BizCode.C811, "修改 [${vo.email}] 密码，预期影响行记录 1 实际影响 $n")
                    }
                }
                .then()
    }

    /**
     * 删除用户。
     */
    fun delete(email: String): Mono<Void> = Mono.defer {
        if (email == Config.rootEmail) {
            throw BizCodeException(BizCodes.C_2002)
        }
        userRepository.delete(email)
                .doOnNext { n ->
                    if (n != 1) {
                        throw BizCodeException(BizCode.C811, "删除 [${email}] 用户，预期影响行记录 1 实际影响 $n")
                    }
                }
                .then()
    }

    /**
     * 重置密码。
     */
    fun resetPassword(vo: UserVo.ResetPassword): Mono<Void> = Mono.defer {
        if (vo.email == Config.rootEmail) {
            throw BizCodeException(BizCodes.C_2003)
        }
        userRepository.updatePassword(vo.email, passwordEncoder.encode(vo.password))
                .doOnNext { n ->
                    if (n != 1) {
                        throw BizCodeException(BizCode.C811, "重置 [${vo.email}] 用户密码，预期影响行记录 1 实际影响 $n")
                    }
                }
                .then()
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