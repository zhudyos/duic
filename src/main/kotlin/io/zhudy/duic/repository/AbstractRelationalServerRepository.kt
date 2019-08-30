package io.zhudy.duic.repository

import io.zhudy.duic.domain.Server
import io.zhudy.duic.repository.ServerRepository.Companion.ACTIVE_TIMEOUT
import io.zhudy.duic.repository.ServerRepository.Companion.CLEAN_BEFORE
import org.springframework.data.r2dbc.core.DatabaseClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
abstract class AbstractRelationalServerRepository(
        private val dc: DatabaseClient
) : ServerRepository {

    companion object {
        private const val UNREGISTER_SQL = "DELETE FROM DUIC_SERVER WHERE id=:id"
        private const val PING_SQL = "UPDATE DUIC_SERVER SET active_at=:activeAt WHERE id=:id"
        private const val FIND_SERVERS_SQL = "SELECT host,port,init_at,active_at FROM DUIC_SERVER WHERE active_at >= :activeAt"
        private const val CLEAN_SQL = "DELETE FROM DUIC_SERVER WHERE active_at<=:activeAt"
        private const val FIND_DB_VERSION_SQL = "SELECT VERSION()"
    }

    override fun unregister(host: String, port: Int): Mono<Int> = Mono.defer {
        dc.execute(UNREGISTER_SQL)
                .bind("id", "${host}_$port")
                .fetch()
                .rowsUpdated()
    }

    override fun ping(host: String, port: Int): Mono<Int> = Mono.defer {
        dc.execute(PING_SQL)
                .bind("activeAt", LocalDateTime.now())
                .bind("id", "${host}_$port")
                .fetch()
                .rowsUpdated()
    }

    override fun findServers(): Flux<Server> = Flux.defer {
        dc.execute(FIND_SERVERS_SQL)
                .bind("activeAt", LocalDateTime.now().minus(ACTIVE_TIMEOUT))
                .`as`(Server::class.java)
                .fetch()
                .all()
    }

    override fun clean(): Mono<Int> = Mono.defer {
        dc.execute(CLEAN_SQL)
                .bind("activeAt", LocalDateTime.now().minus(CLEAN_BEFORE))
                .fetch()
                .rowsUpdated()
    }

    override fun findDbVersion(): Mono<String> = Mono.defer {
        dc.execute(FIND_DB_VERSION_SQL)
                .map { row -> row.get(0, String::class.java) }
                .one()
    }
}