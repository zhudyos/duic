package io.zhudy.duic.repository.mysql

import io.zhudy.duic.UserContext
import io.zhudy.duic.domain.App
import io.zhudy.duic.domain.Page
import io.zhudy.duic.domain.Pageable
import io.zhudy.duic.repository.AbstractRelationalAppRepository
import org.springframework.data.r2dbc.core.DatabaseClient
import reactor.core.publisher.Mono

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class MySqlAppRepositoryImpl(
        private val dc: DatabaseClient
) : AbstractRelationalAppRepository(dc) {

    override fun searchPage(q: String, pageable: Pageable): Mono<Page<App>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun searchPageByUser(q: String, pageable: Pageable, userContext: UserContext): Mono<Page<App>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}