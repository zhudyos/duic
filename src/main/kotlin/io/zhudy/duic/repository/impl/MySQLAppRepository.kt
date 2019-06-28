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

import io.zhudy.duic.BizCodeException
import io.zhudy.duic.BizCodes
import io.zhudy.duic.UserContext
import io.zhudy.duic.domain.*
import io.zhudy.duic.repository.AbstractTransactionRepository
import io.zhudy.duic.repository.AppRepository
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.sql.ResultSet
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
open class MySQLAppRepository(
        transactionManager: PlatformTransactionManager,
        private val jdbcTemplate: NamedParameterJdbcTemplate
) : AppRepository, AbstractTransactionRepository(transactionManager) {

    override fun insert(app: App): Mono<Void> = Mono.defer {
        this.transactionTemplate.execute {
            jdbcTemplate.update(
                    "INSERT INTO DUIC_APP(NAME,PROFILE,DESCRIPTION,TOKEN,IP_LIMIT,CONTENT,V,USERS,UPDATED_AT) VALUES(:name,:profile,:description,:token,:ipLimit,:content,:v,:users,NOW())",
                    mapOf(
                            "name" to app.name,
                            "profile" to app.profile,
                            "description" to app.description,
                            "token" to app.token,
                            "ipLimit" to app.ipLimit,
                            "content" to app.content,
                            "v" to app.v,
                            "users" to app.users.joinToString(",")
                    )
            )
        }

        Mono.empty<Void>()
    }.subscribeOn(Schedulers.elastic())

    override fun delete(app: App, userContext: UserContext): Mono<Void> = findOne<App>(app.name, app.profile)
            .flatMap { dbApp ->
                this.transactionTemplate.execute {
                    jdbcTemplate.update(
                            "DELETE FROM DUIC_APP WHERE NAME=:name AND PROFILE=:profile",
                            mapOf(
                                    "name" to app.name,
                                    "profile" to app.profile
                            )
                    )
                    insertHistory(dbApp, true, userContext)
                }

                Mono.empty<Void>()
            }.subscribeOn(Schedulers.elastic())

    override fun <T> findOne(name: String, profile: String): Mono<App> = Mono.create<App> { sink ->
        transactionTemplate.execute {
            jdbcTemplate.query(
                    "SELECT * FROM DUIC_APP WHERE NAME=:name AND PROFILE=:profile",
                    mapOf("name" to name, "profile" to profile),
                    ResultSetExtractor { rs ->
                        if (rs.next()) {
                            sink.success(mapToApp(rs))
                        } else {
                            sink.success()
                        }
                    }
            )
        }
    }.subscribeOn(Schedulers.elastic())

    @Suppress("HasPlatformType")
    override fun update(app: App, userContext: UserContext): Mono<Void> = findOne<App>(app.name, app.profile)
            .flatMap { dbApp ->
                this.transactionTemplate.execute {
                    val n = jdbcTemplate.update(
                            "UPDATE DUIC_APP SET TOKEN=:token,IP_LIMIT=:ip_limit,DESCRIPTION=:description,USERS=:users,UPDATED_AT=now() WHERE NAME=:name AND PROFILE=:profile AND V=:v",
                            mapOf(
                                    "token" to app.token,
                                    "ip_limit" to app.ipLimit,
                                    "description" to app.description,
                                    "users" to app.users.joinToString(","),
                                    "name" to app.name,
                                    "profile" to app.profile,
                                    "v" to app.v
                            )
                    )
                    if (n != 1) {
                        if (app.v != dbApp.v) {
                            throw BizCodeException(BizCodes.C_1004)
                        }

                        throw BizCodeException(BizCodes.C_1003, "修改 ${app.name}/${app.profile} 失败")
                    }

                    insertHistory(dbApp, false, userContext)
                }

                Mono.empty<Void>()
            }
            .subscribeOn(Schedulers.elastic())

    override fun updateContent(app: App, userContext: UserContext): Mono<App> = findOne<App>(app.name, app.profile)
            .map { dbApp ->
                this.transactionTemplate.execute {
                    val n = jdbcTemplate.update(
                            "UPDATE DUIC_APP SET CONTENT=:content,V=V+1,UPDATED_AT=now() WHERE NAME=:name AND PROFILE=:profile AND V=:v",
                            mapOf(
                                    "content" to app.content,
                                    "name" to app.name,
                                    "profile" to app.profile,
                                    "v" to app.v
                            )
                    )
                    if (n != 1) {
                        if (app.v != dbApp.v) {
                            throw BizCodeException(BizCodes.C_1004)
                        }

                        throw BizCodeException(BizCodes.C_1003, "修改 ${app.name}/${app.profile} 失败")
                    }

                    insertHistory(dbApp, false, userContext)
                }

                dbApp
            }
            .subscribeOn(Schedulers.elastic())

    override fun findAll(): Flux<App> = Flux.create<App> { sink ->
        transactionTemplate.execute {
            jdbcTemplate.query("SELECT * FROM DUIC_APP ORDER BY UPDATED_AT") { rs ->
                sink.next(mapToApp(rs))
            }
        }
        sink.complete()
    }.subscribeOn(Schedulers.elastic())

    override fun findPage(pageable: Pageable): Mono<Page<App>> = Mono.zip(
            Flux.create<App> { sink ->
                transactionTemplate.execute {
                    jdbcTemplate.query(
                            "SELECT * FROM DUIC_APP LIMIT :offset,:limit",
                            mapOf("offset" to pageable.offset, "limit" to pageable.size)
                    ) { rs ->
                        sink.next(mapToApp(rs))
                    }
                }
                sink.complete()
            }.subscribeOn(Schedulers.elastic()).collectList(),

            Mono.create<Int> {
                val c = transactionTemplate.execute {
                    jdbcTemplate.queryForObject(
                            "SELECT COUNT(1) FROM DUIC_APP",
                            EmptySqlParameterSource.INSTANCE,
                            Int::class.java
                    )
                }
                it.success(c)
            }.subscribeOn(Schedulers.elastic())
    ).map {
        Page(it.t1, it.t2, pageable)
    }

    override fun findPageByUser(pageable: Pageable, userContext: UserContext): Mono<Page<App>> = Mono.zip(
            Flux.create<App> { sink ->
                transactionTemplate.execute {
                    jdbcTemplate.query(
                            "SELECT * FROM DUIC_APP WHERE USERS LIKE CONCAT('%', :email, '%') LIMIT :offset,:limit",
                            mapOf(
                                    "email" to userContext.email,
                                    "offset" to pageable.offset,
                                    "limit" to pageable.size
                            )
                    ) { rs ->
                        sink.next(mapToApp(rs))
                    }
                }
                sink.complete()
            }.subscribeOn(Schedulers.elastic()).collectList(),

            Mono.create<Int> {
                val c = transactionTemplate.execute {
                    jdbcTemplate.queryForObject(
                            "SELECT COUNT(1) FROM DUIC_APP WHERE USERS LIKE CONCAT('%', :email, '%')",
                            mapOf("email" to userContext.email),
                            Int::class.java
                    )
                }
                it.success(c)
            }.subscribeOn(Schedulers.elastic())
    ).map {
        Page(it.t1, it.t2, pageable)
    }

    override fun searchPage(q: String, pageable: Pageable): Mono<Page<App>> = Mono.zip(
            Flux.create<App> { sink ->
                transactionTemplate.execute {
                    val sql = if (q.isEmpty()) {
                        "SELECT * FROM DUIC_APP LIMIT :offset,:limit"
                    } else {
                        "SELECT * FROM DUIC_APP WHERE MATCH(NAME, PROFILE, CONTENT) AGAINST(:q) LIMIT :offset,:limit"
                    }

                    jdbcTemplate.query(
                            sql,
                            mapOf(
                                    "q" to q,
                                    "offset" to pageable.offset,
                                    "limit" to pageable.size
                            )
                    ) { rs ->
                        sink.next(mapToApp(rs))
                    }
                }
                sink.complete()
            }.subscribeOn(Schedulers.elastic()).collectList(),

            Mono.create<Int> {
                val c = transactionTemplate.execute {
                    val sql = if (q.isEmpty()) {
                        "SELECT COUNT(*) FROM DUIC_APP"
                    } else {
                        "SELECT COUNT(*) FROM DUIC_APP WHERE MATCH(NAME, PROFILE, CONTENT) AGAINST(:q)"
                    }
                    jdbcTemplate.queryForObject(sql, mapOf("q" to q), Int::class.java)
                }
                it.success(c)
            }.subscribeOn(Schedulers.elastic())
    ).map {
        Page(it.t1, it.t2, pageable)
    }

    override fun searchPageByUser(q: String, pageable: Pageable, userContext: UserContext): Mono<Page<App>> = Mono.zip(
            Flux.create<App> { sink ->
                transactionTemplate.execute {
                    val sql = if (q.isEmpty()) {
                        "SELECT * FROM DUIC_APP WHERE USERS LIKE CONCAT('%', :email, '%') LIMIT :offset,:limit"
                    } else {
                        "SELECT * FROM DUIC_APP WHERE USERS LIKE CONCAT('%', :email, '%') AND MATCH(NAME, PROFILE, CONTENT) AGAINST(:q) LIMIT :offset,:limit"
                    }
                    jdbcTemplate.query(
                            sql,
                            mapOf(
                                    "q" to q,
                                    "email" to userContext.email,
                                    "offset" to pageable.offset,
                                    "limit" to pageable.size
                            )
                    ) { rs ->
                        sink.next(mapToApp(rs))
                    }
                }
                sink.complete()
            }.subscribeOn(Schedulers.elastic()).collectList(),

            Mono.create<Int> {
                val c = transactionTemplate.execute {
                    val sql = if (q.isEmpty()) {
                        "SELECT COUNT(*) FROM DUIC_APP WHERE USERS LIKE CONCAT('%', :email, '%')"
                    } else {
                        "SELECT COUNT(*) FROM DUIC_APP WHERE USERS LIKE CONCAT('%', :email, '%') AND MATCH(NAME, PROFILE, CONTENT) AGAINST(:q)"
                    }
                    jdbcTemplate.queryForObject(sql, mapOf("q" to q, "email" to userContext.email), Int::class.java)
                }
                it.success(c)
            }.subscribeOn(Schedulers.elastic())
    ).map {
        Page(it.t1, it.t2, pageable)
    }

    override fun findByUpdatedAt(updateAt: Date): Flux<App> = Flux.create<App> { sink ->
        this.transactionTemplate.execute {
            jdbcTemplate.query(
                    "SELECT * FROM DUIC_APP WHERE UPDATED_AT > :updated_at ORDER BY UPDATED_AT",
                    mapOf("updated_at" to updateAt)
            ) { rs ->
                sink.next(mapToApp(rs))
            }
        }
        sink.complete()
    }.subscribeOn(Schedulers.elastic())

    override fun findLast50History(name: String, profile: String): Flux<AppContentHistory> = Flux.create<AppContentHistory> { sink ->
        transactionTemplate.execute {
            jdbcTemplate.query(
                    "SELECT * FROM DUIC_APP_HISTORY WHERE NAME=:name AND PROFILE=:profile ORDER BY CREATED_AT DESC LIMIT 0,50",
                    mapOf("name" to name, "profile" to profile)
            ) { rs ->
                sink.next(AppContentHistory(
                        hid = rs.getString("id"),
                        updatedBy = rs.getString("updated_by") ?: "",
                        content = rs.getString("content") ?: "",
                        updatedAt = rs.getTimestamp("created_at")
                ))
            }
        }
        sink.complete()
    }.subscribeOn(Schedulers.elastic())

    override fun findAllNames(): Flux<String> = Flux.create<String> { sink ->
        transactionTemplate.execute {
            jdbcTemplate.query("SELECT NAME FROM DUIC_APP") { rs ->
                sink.next(rs.getString("name"))
            }
        }
        sink.complete()
    }.subscribeOn(Schedulers.elastic())

    override fun findProfilesByName(name: String): Flux<String> = Flux.create<String> { sink ->
        transactionTemplate.execute {
            jdbcTemplate.query("SELECT PROFILE FROM DUIC_APP WHERE NAME=:name", mapOf("name" to name)) { rs ->
                sink.next(rs.getString("profile"))
            }
        }
        sink.complete()
    }.subscribeOn(Schedulers.elastic())

    override fun findDeletedByCreatedAt(createdAt: Date): Flux<AppHistory> = Flux.create<AppHistory> { sink ->
        transactionTemplate.execute {
            jdbcTemplate.query(
                    "SELECT * FROM DUIC_APP_HISTORY WHERE CREATED_AT > :created_at AND DELETED_BY IS NOT NULL AND DELETED_BY != '' ORDER BY CREATED_AT",
                    mapOf("created_at" to createdAt)
            ) { rs ->
                sink.next(AppHistory(
                        id = rs.getString("id"),
                        name = rs.getString("name"),
                        profile = rs.getString("profile"),
                        description = rs.getString("description"),
                        content = rs.getString("content"),
                        token = rs.getString("token"),
                        ipLimit = rs.getString("ip_limit"),
                        v = rs.getInt("v"),
                        createdAt = rs.getTimestamp("created_at"),
                        updatedBy = rs.getString("updated_by"),
                        deletedBy = rs.getString("deleted_by"),
                        users = rs.getString("users").split(",")
                ))
            }
        }
        sink.complete()
    }.subscribeOn(Schedulers.elastic())

    override fun findLastDataTime(): Mono<Long> = Mono.create<Long> { sink ->
        transactionTemplate.execute {
            jdbcTemplate.query("SELECT UPDATED_AT FROM DUIC_APP ORDER BY UPDATED_AT DESC", ResultSetExtractor { rs ->
                if (rs.next()) {
                    sink.success(rs.getTimestamp("updated_at").time)
                } else {
                    sink.success(0)
                }
            })
        }
    }

    private fun insertHistory(app: App, delete: Boolean, userContext: UserContext) = jdbcTemplate.update(
            """INSERT INTO DUIC_APP_HISTORY(NAME,PROFILE,DESCRIPTION,TOKEN,IP_LIMIT,V,CONTENT,USERS,DELETED_BY,UPDATED_BY,CREATED_AT)
VALUES(:name,:profile,:description,:token,:ip_limit,:v,:content,:users,:deleted_by,:updated_by,now())""",
            mapOf(
                    "name" to app.name,
                    "profile" to app.profile,
                    "description" to app.description,
                    "token" to app.token,
                    "ip_limit" to app.ipLimit,
                    "v" to app.v,
                    "content" to app.content,
                    "users" to app.users.joinToString(","),
                    "deleted_by" to if (delete) userContext.email else "",
                    "updated_by" to if (!delete) userContext.email else ""
            )
    )

    private fun mapToApp(rs: ResultSet) = App(
            id = rs.getString("id"),
            name = rs.getString("name"),
            profile = rs.getString("profile"),
            description = rs.getString("description"),
            token = rs.getString("token") ?: "",
            ipLimit = rs.getString("ip_limit") ?: "",
            v = rs.getInt("v"),
            createdAt = rs.getTimestamp("created_at"),
            updatedAt = rs.getTimestamp("updated_at"),
            content = rs.getString("content") ?: "",
            users = rs.getString("users").split(",")
    )

}