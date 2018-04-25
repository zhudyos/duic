package io.zhudy.duic.repository.impl

import io.zhudy.duic.UserContext
import io.zhudy.duic.domain.*
import io.zhudy.duic.repository.AppRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.support.TransactionTemplate
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class MySQLAppRepository(
        private val transactionTemplate: TransactionTemplate,
        private val jdbcTemplate: NamedParameterJdbcTemplate
) : AppRepository {

    override fun insert(app: App): Mono<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(app: App, userContext: UserContext): Mono<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T> findOne(name: String, profile: String): Mono<App> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(app: App, userContext: UserContext): Mono<Int> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateContent(app: App, userContext: UserContext): Mono<Int> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findAll(): Flux<App> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findPage(pageable: Pageable): Mono<Page<App>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findPageByUser(pageable: Pageable, userContext: UserContext): Mono<Page<App>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun searchPage(q: String, pageable: Pageable): Mono<Page<App>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun searchPageByUser(q: String, pageable: Pageable, userContext: UserContext): Mono<Page<App>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findByUpdatedAt(updateAt: Date): Flux<App> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findLast50History(name: String, profile: String): Flux<AppContentHistory> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findAllNames(): Flux<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findProfilesByName(name: String): Flux<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findDeletedByCreatedAt(createdAt: Date): Flux<AppHistory> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}