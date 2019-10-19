package io.zhudy.duic.repository.mysql

import io.zhudy.duic.domain.App
import io.zhudy.duic.domain.AppHistory
import io.zhudy.duic.repository.AbstractRelationalAppRepository
import org.springframework.data.r2dbc.core.DatabaseClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class MySqlAppRepositoryImpl(
        private val dc: DatabaseClient
) : AbstractRelationalAppRepository(dc) {

    override fun insertHistory(appHistory: AppHistory): Mono<Int> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun find4UpdatedAt(time: Instant): Flux<App> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}