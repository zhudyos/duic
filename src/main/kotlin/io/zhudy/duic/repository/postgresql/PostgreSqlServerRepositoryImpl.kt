package io.zhudy.duic.repository.postgresql

import io.zhudy.duic.repository.AbstractRelationalServerRepository
import org.springframework.data.r2dbc.core.DatabaseClient
import reactor.core.publisher.Mono

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class PostgreSqlServerRepositoryImpl(private val dc: DatabaseClient) : AbstractRelationalServerRepository(dc) {

    companion object {
        private const val REGISTER_SQL = "INSERT INTO DUIC_SERVER(id,host,port,init_at,active_at) VALUES(:id,:host,:port,now(),now()) ON CONFLICT (ID) DO UPDATE SET init_at=NOW(),active_at=NOW()"
    }

    override fun register(host: String, port: Int): Mono<Int> = Mono.defer {
        dc.execute(REGISTER_SQL)
                .bind("id", "${host}_$port")
                .bind("host", host)
                .bind("port", port)
                .fetch()
                .rowsUpdated()
    }
}