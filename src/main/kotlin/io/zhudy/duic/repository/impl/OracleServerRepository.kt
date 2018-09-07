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
open class OracleServerRepository(
        transactionManager: PlatformTransactionManager,
        private val jdbcTemplate: NamedParameterJdbcTemplate
) : ServerRepository, AbstractTransactionRepository(transactionManager) {

    override fun register(host: String, port: Int) = Mono.create<Int> {
        val n = transactionTemplate.execute {
            jdbcTemplate.update(
                    """MERGE INTO DUIC_SERVER d
USING (SELECT :id ID,:host HOST,:port PORT from DUAL) s
ON (d.ID = s.ID)
WHEN MATCHED THEN UPDATE SET d.INIT_AT=CURRENT_TIMESTAMP, d.ACTIVE_AT=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ID,HOST,PORT,INIT_AT,ACTIVE_AT) VALUES (s.ID,s.HOST,s.PORT,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)""",
                    mapOf(
                            "id" to "${host}_$port",
                            "host" to host,
                            "port" to port
                    )
            )
        }
        it.success(n)
    }.subscribeOn(Schedulers.elastic())

    override fun unregister(host: String, port: Int) = Mono.create<Int> {
        val n = transactionTemplate.execute {
            jdbcTemplate.update("DELETE FROM DUIC_SERVER WHERE ID=:id", mapOf("id" to "${host}_$port"))
        }
        it.success(n)
    }.subscribeOn(Schedulers.elastic())

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
    }.subscribeOn(Schedulers.elastic())

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
}