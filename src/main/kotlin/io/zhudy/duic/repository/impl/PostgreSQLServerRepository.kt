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

import io.zhudy.duic.domain.Server
import io.zhudy.duic.repository.AbstractTransactionRepository
import io.zhudy.duic.repository.ServerRepository
import io.zhudy.duic.repository.ServerRepository.Companion.ACTIVE_TIMEOUT
import io.zhudy.duic.repository.ServerRepository.Companion.CLEAN_BEFORE
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Instant
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
open class PostgreSQLServerRepository(
        transactionManager: PlatformTransactionManager,
        private val jdbcTemplate: NamedParameterJdbcTemplate
) : ServerRepository, AbstractTransactionRepository(transactionManager) {

    @Suppress("HasPlatformType")
    override fun register(host: String, port: Int) = Mono.create<Int> {
        val n = transactionTemplate.execute {
            jdbcTemplate.update(
                    "INSERT INTO DUIC_SERVER(ID,HOST,PORT,INIT_AT,ACTIVE_AT) VALUES(:id,:host,:port,now(),now()) ON CONFLICT (ID) DO UPDATE SET INIT_AT=now(),ACTIVE_AT=NOW()",
                    mapOf(
                            "id" to "${host}_$port",
                            "host" to host,
                            "port" to port
                    )
            )
        }
        it.success(n)
    }.subscribeOn(Schedulers.elastic())

    @Suppress("HasPlatformType")
    override fun unregister(host: String, port: Int) = Mono.create<Int> {
        val n = transactionTemplate.execute {
            jdbcTemplate.update("DELETE FROM DUIC_SERVER WHERE ID=:id", mapOf("id" to "${host}_$port"))
        }
        it.success(n)
    }.subscribeOn(Schedulers.elastic())

    @Suppress("HasPlatformType")
    override fun ping(host: String, port: Int) = Mono.create<Int> {
        val n = transactionTemplate.execute {
            jdbcTemplate.update(
                    "UPDATE DUIC_SERVER SET ACTIVE_AT=:active_at WHERE ID=:id",
                    mapOf(
                            "id" to "${host}_$port",
                            "active_at" to Date()
                    )
            )
        }
        it.success(n)
    }.subscribeOn(Schedulers.elastic())

    @Suppress("HasPlatformType")
    override fun findServers() = Flux.create<Server> { sink ->
        roTransactionTemplate.execute {
            jdbcTemplate.query(
                    "SELECT HOST,PORT,INIT_AT,ACTIVE_AT FROM DUIC_SERVER WHERE ACTIVE_AT >= :active_at",
                    mapOf(
                            "active_at" to Date.from(Instant.now().minus(ACTIVE_TIMEOUT))
                    )
            ) { rs ->
                sink.next(Server(
                        host = rs.getString("host"),
                        port = rs.getInt("port"),
                        initAt = rs.getTimestamp("init_at"),
                        activeAt = rs.getTimestamp("active_at")
                ))
            }
        }
        sink.complete()
    }.subscribeOn(Schedulers.elastic())

    @Suppress("HasPlatformType")
    override fun clean() = Mono.create<Int> {
        val n = transactionTemplate.execute {
            jdbcTemplate.update(
                    "DELETE FROM DUIC_SERVER WHERE ACTIVE_AT<=:active_at",
                    mapOf(
                            "active_at" to Date.from(Instant.now().minus(CLEAN_BEFORE))
                    )
            )
        }
        it.success(n)
    }.subscribeOn(Schedulers.elastic())

    @Suppress("HasPlatformType")
    override fun findDbVersion() = Mono.create<String> {
        val v = transactionTemplate.execute {
            val m = jdbcTemplate.queryForMap("SELECT VERSION()", emptyMap<String, Any>())
            m["version"] as String
        }
        it.success(v)
    }.subscribeOn(Schedulers.elastic())

}