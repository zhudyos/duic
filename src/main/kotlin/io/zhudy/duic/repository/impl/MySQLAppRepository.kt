package io.zhudy.duic.repository.impl

import io.zhudy.duic.UserContext
import io.zhudy.duic.domain.*
import io.zhudy.duic.repository.AppRepository
import org.bson.Document
import org.bson.types.ObjectId
import org.joda.time.DateTime
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.support.TransactionTemplate
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
open class MySQLAppRepository(
        private val transactionTemplate: TransactionTemplate,
        private val jdbcTemplate: NamedParameterJdbcTemplate
) : AppRepository {

    override fun insert(app: App) = Mono.create<Int> {
        var n = 0
        transactionTemplate.execute {
            n = jdbcTemplate.update(
                    "INSERT INTO `app`(`name`,`profile`,`token`,`description`,`v`,`content`,`users`) VALUES (:name,:profile,:token,:description,:v,:content,:users)",
                    mapOf(
                            "name" to app.name,
                            "profile" to app.profile,
                            "token" to app.token,
                            "description" to app.description,
                            "v" to app.v,
                            "content" to app.content,
                            "users" to app.users.joinToString(",")
                    )
            )
        }
        it.success(n)
    }.subscribeOn(Schedulers.elastic())

    override fun delete(app: App, userContext: UserContext) = Mono.create<Int> {

    }

    override fun <T> findOne(name: String, profile: String) = Mono.create<App> {
        var app: App? = null
        transactionTemplate.execute {
            jdbcTemplate.query(
                    "SELECT ",
                    mapOf(
                            "name" to name,
                            "profile" to profile
                    ), {
                app = App()
            }
            )
        }
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

    @Suppress("UNCHECKED_CAST")
    private fun mapToApp(doc: Document) = App(
            id = doc["_id"].toString(),
            name = doc.getString("name"),
            profile = doc.getString("profile"),
            token = doc.getString("token") ?: "",
            ipLimit = doc.getString("ip_limit") ?: "",
            description = doc.getString("description"),
            v = doc.getInteger("v"),
            createdAt = DateTime(doc.getDate("created_at")),
            updatedAt = DateTime(doc.getDate("updated_at")),
            content = doc.getString("content"),
            users = doc["users"] as List<String>
    )

    @Suppress("UNCHECKED_CAST")
    private fun mapToAppHistory(doc: Document) = AppHistory(
            id = doc.getString("_id"),
            name = doc.getString("name"),
            profile = doc.getString("profile"),
            token = doc.getString("token") ?: "",
            ipLimit = doc.getString("ip_limit") ?: "",
            description = doc.getString("description") ?: "",
            content = doc.getString("content") ?: "",
            v = doc.getInteger("v"),
            createdAt = DateTime(doc.getDate("created_at")),
            updatedBy = doc.getString("updated_by") ?: "",
            deletedBy = doc.getString("deleted_by") ?: "",
            users = doc["users"] as List<String>
    )

}