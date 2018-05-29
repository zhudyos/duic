package io.zhudy.duic.repository.impl

import io.zhudy.duic.domain.Server
import io.zhudy.duic.repository.AbstractTransactionRepository
import io.zhudy.duic.repository.ServerRepository
import org.joda.time.DateTime
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
open class PostgreSQLServerRepository(
        transactionManager: PlatformTransactionManager,
        private val jdbcTemplate: NamedParameterJdbcTemplate
) : ServerRepository, AbstractTransactionRepository(transactionManager) {

    override fun register(host: String, port: Int) = Mono.create<Int> {
        val n = transactionTemplate.execute {
            jdbcTemplate.update(
                    "INSERT INTO server(id,host,port,init_at,active_at) VALUES(:id,:host,:port,:init_at,:active_at) ON CONFLICT (id) DO UPDATE SET init_at=:init_at,active_at=:active_at",
                    mapOf(
                            "id" to "${host}_$port",
                            "host" to host,
                            "port" to port,
                            "init_at" to Date(),
                            "active_at" to Date()
                    )
            )
        }
        it.success(n)
    }

    override fun unregister(host: String, port: Int) = Mono.create<Int> {
        val n = transactionTemplate.execute {
            jdbcTemplate.update("DELETE FROM server WHERE id=:id", mapOf("id" to "${host}_$port"))
        }
        it.success(n)
    }

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
    }

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
    }

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
    }
}