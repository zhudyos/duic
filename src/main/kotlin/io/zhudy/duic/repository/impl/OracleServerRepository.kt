package io.zhudy.duic.repository.impl

import io.zhudy.duic.domain.Server
import io.zhudy.duic.repository.AbstractTransactionRepository
import io.zhudy.duic.repository.ServerRepository
import org.joda.time.DateTime
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
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
                    """MERGE INTO SERVER d
USING (SELECT :id id,:host host,:port port from DUAL) s
ON (d.id = s.id)
WHEN MATCHED THEN UPDATE SET d.init_at=CURRENT_TIMESTAMP, d.active_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (id,host,port,init_at,active_at) VALUES (s.id,s.host,s.port,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)""",
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
            jdbcTemplate.update("DELETE FROM server WHERE id=:id", mapOf("id" to "${host}_$port"))
        }
        it.success(n)
    }.subscribeOn(Schedulers.elastic())

    override fun ping(host: String, port: Int) = Mono.create<Int> {
        val n = transactionTemplate.execute {
            jdbcTemplate.update(
                    "UPDATE server SET active_at=:active_at WHERE id=:id",
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
                    "SELECT host,port,init_at,active_at FROM server WHERE active_at >= :active_at",
                    mapOf(
                            "active_at" to DateTime.now().minusMinutes(ServerRepository.ACTIVE_TIMEOUT_MINUTES).toDate()
                    )
            ) {
                sink.next(Server(
                        host = it.getString("host"),
                        port = it.getInt("port"),
                        initAt = DateTime(it.getTimestamp("init_at")),
                        activeAt = DateTime(it.getTimestamp("active_at"))
                ))
            }
        }
        sink.complete()
    }.subscribeOn(Schedulers.elastic())

    override fun clean() = Mono.create<Int> {
        val n = transactionTemplate.execute {
            jdbcTemplate.update(
                    "DELETE FROM server WHERE active_at<=:active_at",
                    mapOf(
                            "active_at" to DateTime.now().minusMinutes(ServerRepository.CLEAN_BEFORE_MINUTES).toDate()
                    )
            )
        }
        it.success(n)
    }.subscribeOn(Schedulers.elastic())
}