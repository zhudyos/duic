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
package io.zhudy.duic.repository.impl

import io.zhudy.duic.domain.Page
import io.zhudy.duic.domain.Pageable
import io.zhudy.duic.domain.User
import io.zhudy.duic.repository.AbstractTransactionRepository
import io.zhudy.duic.repository.UserRepository
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class MySQLUserRepository(
        transactionManager: PlatformTransactionManager,
        private val jdbcTemplate: NamedParameterJdbcTemplate
) : UserRepository, AbstractTransactionRepository(transactionManager) {

    @Suppress("HasPlatformType")
    override fun insert(user: User) = Mono.create<Int> {
        val n = transactionTemplate.execute {
            jdbcTemplate.update(
                    "INSERT INTO DUIC_USER(EMAIL,PASSWORD,CREATED_AT) VALUES(:email,:password,NOW())",
                    mapOf("email" to user.email, "password" to user.password)
            )
        }
        it.success(n)
    }.subscribeOn(Schedulers.elastic())

    @Suppress("HasPlatformType")
    override fun delete(email: String) = Mono.create<Int> {
        val n = transactionTemplate.execute {
            jdbcTemplate.update(
                    "DELETE FROM DUIC_USER WHERE EMAIL=:email",
                    mapOf("email" to email)
            )
        }
        it.success(n)
    }.subscribeOn(Schedulers.elastic())

    @Suppress("HasPlatformType")
    override fun updatePassword(email: String, password: String) = Mono.create<Int> {
        val n = transactionTemplate.execute {
            jdbcTemplate.update(
                    "UPDATE DUIC_USER SET PASSWORD=:password,UPDATED_AT=now() WHERE EMAIL=:email",
                    mapOf("email" to email, "password" to password)
            )
        }
        it.success(n)
    }.subscribeOn(Schedulers.elastic())

    @Suppress("HasPlatformType")
    override fun findByEmail(email: String) = Mono.create<User> { sink ->
        roTransactionTemplate.execute {
            jdbcTemplate.query(
                    "SELECT EMAIL,PASSWORD FROM DUIC_USER WHERE EMAIL=:email",
                    mapOf("email" to email),
                    ResultSetExtractor { rs ->
                        if (rs.next()) {
                            sink.success(User(rs.getString(1), rs.getString(2)))
                        } else {
                            sink.success()
                        }
                    }
            )
        }
    }.subscribeOn(Schedulers.elastic())

    @Suppress("HasPlatformType")
    override fun findPage(pageable: Pageable) = Mono.zip(
            Flux.create<User> { sink ->
                roTransactionTemplate.execute {
                    jdbcTemplate.query(
                            "SELECT EMAIL,CREATED_AT,UPDATED_AT FROM DUIC_USER LIMIT :offset,:limit",
                            mapOf("offset" to pageable.offset, "limit" to pageable.size)
                    ) {
                        sink.next(User(
                                email = it.getString(1),
                                createdAt = it.getTimestamp(2),
                                updatedAt = it.getTimestamp(3)
                        ))
                    }
                    sink.complete()
                }
            }.subscribeOn(Schedulers.elastic()).collectList(),
            Mono.create<Int> {
                val c = roTransactionTemplate.execute {
                    jdbcTemplate.queryForObject(
                            "SELECT COUNT(1) FROM DUIC_USER",
                            EmptySqlParameterSource.INSTANCE,
                            Int::class.java
                    )
                }
                it.success(c)
            }.subscribeOn(Schedulers.elastic())
    ).map {
        Page(it.t1, it.t2, pageable)
    }

    @Suppress("HasPlatformType")
    override fun findAllEmail() = Flux.create<String> { sink ->
        roTransactionTemplate.execute {
            jdbcTemplate.query("SELECT EMAIL FROM DUIC_USER") {
                sink.next(it.getString(1))
            }
            sink.complete()
        }
    }.subscribeOn(Schedulers.elastic())

}