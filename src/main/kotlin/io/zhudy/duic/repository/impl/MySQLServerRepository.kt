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

import io.zhudy.duic.domain.Server
import io.zhudy.duic.repository.AbstractTransactionRepository
import io.zhudy.duic.repository.ServerRepository
import io.zhudy.duic.repository.ServerRepository.Companion.ACTIVE_TIMEOUT
import io.zhudy.duic.repository.ServerRepository.Companion.CLEAN_BEFORE
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
open class MySQLServerRepository(
        transactionManager: PlatformTransactionManager,
        private val jdbcTemplate: NamedParameterJdbcTemplate
) : ServerRepository, AbstractTransactionRepository(transactionManager) {

    override fun register(host: String, port: Int) = Mono.create<Int> {
        val n = transactionTemplate.execute {
            jdbcTemplate.update(
                    "INSERT INTO DUIC_SERVER(ID,HOST,PORT,INIT_AT,ACTIVE_AT) VALUES(:id,:host,:port,NOW(),NOW()) ON DUPLICATE KEY UPDATE INIT_AT=NOW(),ACTIVE_AT=NOW()",
                    mapOf(
                            "id" to "${host}_$port",
                            "host" to host,
                            "port" to port
                    )
            )
        }
        it.success(n)
    }

    override fun unregister(host: String, port: Int) = Mono.create<Int> {
        val n = transactionTemplate.execute {
            jdbcTemplate.update("DELETE FROM DUIC_SERVER WHERE ID=:id", mapOf("id" to "${host}_$port"))
        }
        it.success(n)
    }

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
    }

    override fun findServers() = Flux.create<Server> { sink ->
        roTransactionTemplate.execute {
            jdbcTemplate.query(
                    "SELECT HOST,PORT,INIT_AT,ACTIVE_AT FROM DUIC_SERVER WHERE ACTIVE_AT >= :active_at",
                    mapOf(
                            "active_at" to Date.from(Instant.now().minus(ACTIVE_TIMEOUT))
                    )
            ) {
                sink.next(Server(
                        host = it.getString("host"),
                        port = it.getInt("port"),
                        initAt = it.getTimestamp("init_at"),
                        activeAt = it.getTimestamp("active_at")
                ))
            }
        }
        sink.complete()
    }

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
    }
}