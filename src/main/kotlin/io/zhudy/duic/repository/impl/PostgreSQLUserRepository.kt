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
class PostgreSQLUserRepository(
        transactionManager: PlatformTransactionManager,
        private val jdbcTemplate: NamedParameterJdbcTemplate
) : UserRepository, AbstractTransactionRepository(transactionManager) {

    override fun insert(user: User) = Mono.create<Int> {
        val n = transactionTemplate.execute {
            jdbcTemplate.update(
                    """INSERT INTO "user"(email,password,created_at) VALUES(:email,:password,now())""",
                    mapOf(
                            "email" to user.email,
                            "password" to user.password
                    )
            )
        }
        it.success(n)
    }.subscribeOn(Schedulers.elastic())

    override fun delete(email: String) = Mono.create<Int> {
        val n = transactionTemplate.execute {
            jdbcTemplate.update(
                    """DELETE FROM "user" WHERE email=:email""",
                    mapOf("email" to email)
            )
        }
        it.success(n)
    }.subscribeOn(Schedulers.elastic())

    override fun updatePassword(email: String, password: String) = Mono.create<Int> {
        val n = transactionTemplate.execute {
            jdbcTemplate.update(
                    """UPDATE "user" SET password=:password,updated_at=now() WHERE email=:email""",
                    mapOf("email" to email, "password" to password)
            )
        }
        it.success(n)
    }.subscribeOn(Schedulers.elastic())

    override fun findByEmail(email: String) = Mono.create<User> { sink ->
        roTransactionTemplate.execute {
            jdbcTemplate.query(
                    """SELECT email,password FROM "user" WHERE "email"=:email""",
                    mapOf("email" to email),
                    ResultSetExtractor {
                        if (it.next()) {
                            sink.success(User(it.getString(1), it.getString(2)))
                        } else {
                            sink.success()
                        }
                    }
            )
        }
    }.subscribeOn(Schedulers.elastic())

    override fun findPage(pageable: Pageable) = Mono.zip(
            Flux.create<User> { sink ->
                roTransactionTemplate.execute {
                    jdbcTemplate.query(
                            """SELECT email,created_at,updated_at FROM "user" LIMIT :limit OFFSET :offset""",
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
                            """SELECT COUNT(1) FROM "user"""",
                            EmptySqlParameterSource.INSTANCE,
                            Int::class.java
                    )
                }
                it.success(c)
            }.subscribeOn(Schedulers.elastic())
    ).map {
        Page(it.t1, it.t2, pageable)
    }

    override fun findAllEmail() = Flux.create<String> { sink ->
        roTransactionTemplate.execute {
            jdbcTemplate.query("""SELECT email FROM "user"""") {
                sink.next(it.getString(1))
            }
            sink.complete()
        }
    }.subscribeOn(Schedulers.elastic())

}