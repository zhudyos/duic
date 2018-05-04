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

import io.zhudy.duic.BizCodeException
import io.zhudy.duic.BizCodes
import io.zhudy.duic.UserContext
import io.zhudy.duic.domain.*
import io.zhudy.duic.repository.AbstractTransactionRepository
import io.zhudy.duic.repository.AppRepository
import org.joda.time.DateTime
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

    override fun insert(app: App) = Mono.create<Int> {
        val n = transactionTemplate.execute {
            jdbcTemplate.update(
                    "INSERT INTO app(name,profile,description,token,ip_limit,v,users,updated_at) VALUES(:name,:profile,:description,:token,:ipLimit,:v,:users,now())",
                    mapOf(
                            "name" to app.name,
                            "profile" to app.profile,
                            "description" to app.description,
                            "token" to app.token,
                            "ipLimit" to app.ipLimit,
                            "v" to app.v,
                            "users" to app.users.joinToString(",")
                    )
            )
        }
        it.success(n)
    }.subscribeOn(Schedulers.elastic())

    override fun delete(app: App, userContext: UserContext) = findOne<App>(app.name, app.profile).flatMap { dbApp ->
        Mono.create<Int> { sink ->
            val n = transactionTemplate.execute {
                val n = jdbcTemplate.update("DELETE FROM app WHERE name=:name AND profile=:profile", mapOf(
                        "name" to app.name,
                        "profile" to app.profile
                ))

                insertHistory(dbApp, true, userContext)
                n
            }
            sink.success(n)
        }.subscribeOn(Schedulers.elastic())
    }

    override fun <T> findOne(name: String, profile: String) = Mono.create<App> { sink ->
        roTransactionTemplate.execute {
            jdbcTemplate.query(
                    "SELECT * FROM app WHERE name=:name AND profile=:profile",
                    mapOf("name" to name, "profile" to profile),
                    ResultSetExtractor {
                        if (it.next()) {
                            sink.success(mapToApp(it))
                        } else {
                            sink.success()
                        }
                    }
            )
        }
    }.subscribeOn(Schedulers.elastic())

    override fun update(app: App, userContext: UserContext) = findOne<App>(app.name, app.profile).flatMap { dbApp ->
        Mono.create<Int> { sink ->
            val n = transactionTemplate.execute {
                val n = jdbcTemplate.update(
                        "UPDATE app SET token=:token,ip_limit=:ip_limit,description=:description,users=:users,updated_at=now() WHERE name=:name and profile=:profile and v=:v",
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
                n
            }
            sink.success(n)
        }.subscribeOn(Schedulers.elastic())
    }

    override fun updateContent(app: App, userContext: UserContext) = findOne<App>(app.name, app.profile).flatMap { dbApp ->
        Mono.create<Int> { sink ->
            val v = transactionTemplate.execute {
                val n = jdbcTemplate.update(
                        "UPDATE app SET content=:content,v=v+1,updated_at=now() WHERE name=:name AND profile=:profile AND v=:v",
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

                app.v + 1
            }
            sink.success(v)
        }.subscribeOn(Schedulers.elastic())
    }

    override fun findAll() = Flux.create<App> { sink ->
        roTransactionTemplate.execute {
            jdbcTemplate.query("SELECT * FROM app ORDER BY updated_at") {
                sink.next(mapToApp(it))
            }
        }
        sink.complete()
    }.subscribeOn(Schedulers.elastic())

    override fun findPage(pageable: Pageable) = Mono.zip(
            Flux.create<App> { sink ->
                roTransactionTemplate.execute {
                    jdbcTemplate.query(
                            "SELECT * FROM app LIMIT :offset,:limit",
                            mapOf("offset" to pageable.offset, "limit" to pageable.size)
                    ) {
                        sink.next(mapToApp(it))
                    }
                }
                sink.complete()
            }.subscribeOn(Schedulers.elastic()).collectList(),
            Mono.create<Int> {
                val c = roTransactionTemplate.execute {
                    jdbcTemplate.queryForObject(
                            "SELECT COUNT(1) FROM app",
                            EmptySqlParameterSource.INSTANCE,
                            Int::class.java
                    )
                }
                it.success(c)
            }.subscribeOn(Schedulers.elastic())
    ).map {
        Page(it.t1, it.t2, pageable)
    }

    override fun findPageByUser(pageable: Pageable, userContext: UserContext) = Mono.zip(
            Flux.create<App> { sink ->
                roTransactionTemplate.execute {
                    jdbcTemplate.query(
                            "SELECT * FROM app WHERE users LIKE CONCAT('%', :email, '%') LIMIT :offset,:limit",
                            mapOf(
                                    "email" to userContext.email,
                                    "offset" to pageable.offset,
                                    "limit" to pageable.size
                            )
                    ) {
                        sink.next(mapToApp(it))
                    }
                }
                sink.complete()
            }.subscribeOn(Schedulers.elastic()).collectList(),
            Mono.create<Int> {
                val c = roTransactionTemplate.execute {
                    jdbcTemplate.queryForObject(
                            "SELECT COUNT(1) FROM app WHERE users LIKE CONCAT('%', :email, '%')",
                            mapOf("email" to userContext.email),
                            Int::class.java
                    )
                }
                it.success(c)
            }.subscribeOn(Schedulers.elastic())
    ).map {
        Page(it.t1, it.t2, pageable)
    }

    override fun searchPage(q: String, pageable: Pageable) = Mono.zip(
            Flux.create<App> { sink ->
                roTransactionTemplate.execute {
                    val sql = if (q.isEmpty()) {
                        "SELECT * FROM app LIMIT :offset,:limit"
                    } else {
                        "SELECT * FROM app WHERE MATCH(name, profile, content) AGAINST(:q) LIMIT :offset,:limit"
                    }

                    jdbcTemplate.query(
                            sql,
                            mapOf(
                                    "q" to q,
                                    "offset" to pageable.offset,
                                    "limit" to pageable.size
                            )
                    ) {
                        sink.next(mapToApp(it))
                    }
                }
                sink.complete()
            }.subscribeOn(Schedulers.elastic()).collectList(),
            Mono.create<Int> {
                val c = roTransactionTemplate.execute {
                    val sql = if (q.isEmpty()) {
                        "SELECT COUNT(*) FROM app"
                    } else {
                        "SELECT COUNT(*) FROM app WHERE MATCH(name, profile, content) AGAINST(:q)"
                    }
                    jdbcTemplate.queryForObject(sql, mapOf("q" to q), Int::class.java)
                }
                it.success(c)
            }.subscribeOn(Schedulers.elastic())
    ).map {
        Page(it.t1, it.t2, pageable)
    }

    override fun searchPageByUser(q: String, pageable: Pageable, userContext: UserContext) = Mono.zip(
            Flux.create<App> { sink ->
                roTransactionTemplate.execute {
                    val sql = if (q.isEmpty()) {
                        "SELECT * FROM app WHERE users LIKE CONCAT('%', :email, '%') LIMIT :offset,:limit"
                    } else {
                        "SELECT * FROM app WHERE users LIKE CONCAT('%', :email, '%') AND MATCH(name, profile, content) AGAINST(:q) LIMIT :offset,:limit"
                    }
                    jdbcTemplate.query(
                            sql,
                            mapOf(
                                    "q" to q,
                                    "email" to userContext.email,
                                    "offset" to pageable.offset,
                                    "limit" to pageable.size
                            )
                    ) {
                        sink.next(mapToApp(it))
                    }
                }
                sink.complete()
            }.subscribeOn(Schedulers.elastic()).collectList(),
            Mono.create<Int> {
                val c = roTransactionTemplate.execute {
                    val sql = if (q.isEmpty()) {
                        "SELECT COUNT(*) FROM app WHERE users LIKE CONCAT('%', :email, '%')"
                    } else {
                        "SELECT COUNT(*) FROM app WHERE users LIKE CONCAT('%', :email, '%') AND MATCH(name, profile, content) AGAINST(:q)"
                    }
                    jdbcTemplate.queryForObject(sql, mapOf("q" to q, "email" to userContext.email), Int::class.java)
                }
                it.success(c)
            }.subscribeOn(Schedulers.elastic())
    ).map {
        Page(it.t1, it.t2, pageable)
    }

    override fun findByUpdatedAt(updateAt: Date) = Flux.create<App> { sink ->
        roTransactionTemplate.execute {
            jdbcTemplate.query(
                    "SELECT * FROM app WHERE updated_at > :updated_at ORDER BY updated_at",
                    mapOf("updated_at" to updateAt)
            ) {
                sink.next(mapToApp(it))
            }
        }
        sink.complete()
    }.subscribeOn(Schedulers.elastic())

    override fun findLast50History(name: String, profile: String) = Flux.create<AppContentHistory> { sink ->
        roTransactionTemplate.execute {
            jdbcTemplate.query(
                    "SELECT * FROM app_history WHERE name=:name AND profile=:profile ORDER BY created_at DESC LIMIT 0,50",
                    mapOf("name" to name, "profile" to profile)
            ) {
                sink.next(AppContentHistory(
                        hid = it.getString("id"),
                        updatedBy = it.getString("updated_by") ?: "",
                        content = it.getString("content") ?: "",
                        updatedAt = it.getDate("created_at")
                ))
            }
        }
        sink.complete()
    }.subscribeOn(Schedulers.elastic())

    override fun findAllNames() = Flux.create<String> { sink ->
        roTransactionTemplate.execute {
            jdbcTemplate.query("SELECT name FROM app") {
                sink.next(it.getString("name"))
            }
        }
        sink.complete()
    }.subscribeOn(Schedulers.elastic())

    override fun findProfilesByName(name: String) = Flux.create<String> { sink ->
        roTransactionTemplate.execute {
            jdbcTemplate.query("SELECT profile FROM app WHERE name=:name", mapOf("name" to name)) {
                sink.next(it.getString("profile"))
            }
        }
        sink.complete()
    }.subscribeOn(Schedulers.elastic())

    override fun findDeletedByCreatedAt(createdAt: Date) = Flux.create<AppHistory> { sink ->
        roTransactionTemplate.execute {
            jdbcTemplate.query(
                    "SELECT * FROM app_history WHERE created_at > :created_at AND deleted_by IS NOT NULL AND deleted_by != '' ORDER BY created_at",
                    mapOf("created_at" to createdAt)
            ) {
                sink.next(AppHistory(
                        id = it.getString("id"),
                        name = it.getString("name"),
                        profile = it.getString("profile"),
                        description = it.getString("description"),
                        content = it.getString("content"),
                        token = it.getString("token"),
                        ipLimit = it.getString("ip_limit"),
                        v = it.getInt("v"),
                        createdAt = DateTime(it.getDate("created_at")),
                        updatedBy = it.getString("updated_by"),
                        deletedBy = it.getString("deleted_by"),
                        users = it.getString("users").split(",")
                ))
            }
        }
        sink.complete()
    }.subscribeOn(Schedulers.elastic())

    private fun insertHistory(app: App, delete: Boolean, userContext: UserContext) = jdbcTemplate.update(
            """INSERT INTO app_history(name,profile,description,token,ip_limit,v,content,users,deleted_by,updated_by,created_at)
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
            createdAt = DateTime(rs.getDate("created_at")),
            updatedAt = DateTime(rs.getDate("updated_at")),
            content = rs.getString("content") ?: "",
            users = rs.getString("users").split(",")
    )

}