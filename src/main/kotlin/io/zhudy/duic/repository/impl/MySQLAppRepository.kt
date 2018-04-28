package io.zhudy.duic.repository.impl

import io.zhudy.duic.UserContext
import io.zhudy.duic.domain.*
import io.zhudy.duic.repository.AbstractTransactionRepository
import io.zhudy.duic.repository.AppRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
open class MySQLAppRepository(
        transactionManager: PlatformTransactionManager,
        private val jdbcTemplate: NamedParameterJdbcTemplate
) : AppRepository, AbstractTransactionRepository(transactionManager) {

    override fun insert(app: App) = Mono.create<Int> {
        val n = transactionTemplate.execute {
            jdbcTemplate.update(
                    "INSERT INTO `app`(`name`,`profile`,`description`,`token`,`ip_limit`,`v`,`users`) VALUES(:name,:profile,:description,:token,:ipLimit,:v,:users)",
                    mapOf(
                            "name" to app.name,
                            "profile" to app.profile,
                            "description" to app.description,
                            "token" to app.token,
                            "ipLimit" to app.ipLimit,
                            "v" to app.v,
                            "users" to app.users.joinToString(",")
                    )
            )
        }
        it.success(n)
    }.subscribeOn(Schedulers.elastic())

    override fun delete(app: App, userContext: UserContext) = findOne<App>(app.name, app.profile).flatMap { dbApp ->
        Mono.create<Int> { sink ->
            val n = transactionTemplate.execute {
                insertHistory(dbApp, true, userContext)

                jdbcTemplate.update("DELETE FROM `app` WHERE `name`=:name and `profile`=:profile", mapOf(
                        "name" to app.name,
                        "profile" to app.profile
                ))
            }
            sink.success(n)
        }
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

    private fun insertHistory(app: App, delete: Boolean, userContext: UserContext) =
            jdbcTemplate.execute("INSERT INTO `app_history`(`name`,`profile`,`description`,`token`,`ip_limit`,`v`,`content`,`users`,`deleted_by`,`updated_by`,`created_at`)" +
                    " VALUES(?,?,?,?,?,?,?,?,?,?,now())") {
                val deletedBy = if (delete) userContext.email else ""
                val updatedBy = if (!delete) userContext.email else ""

                var i = 1
                it.setString(i++, app.name)
                it.setString(i++, app.profile)
                it.setString(i++, app.description)
                it.setString(i++, app.token)
                it.setString(i++, app.ipLimit)
                it.setInt(i++, app.v)
                it.setString(i++, app.content)
                it.setString(i++, app.users.joinToString(","))
                it.setString(i++, deletedBy)
                it.setString(i++, updatedBy)

                it.executeUpdate()
            }

//    @Suppress("UNCHECKED_CAST")
//    private fun mapToApp(doc: Document) = App(
//            id = doc["_id"].toString(),
//            name = doc.getString("name"),
//            profile = doc.getString("profile"),
//            token = doc.getString("token") ?: "",
//            ipLimit = doc.getString("ip_limit") ?: "",
//            description = doc.getString("description"),
//            v = doc.getInteger("v"),
//            createdAt = DateTime(doc.getDate("created_at")),
//            updatedAt = DateTime(doc.getDate("updated_at")),
//            content = doc.getString("content"),
//            users = doc["users"] as List<String>
//    )
//
//    @Suppress("UNCHECKED_CAST")
//    private fun mapToAppHistory(doc: Document) = AppHistory(
//            id = doc.getString("_id"),
//            name = doc.getString("name"),
//            profile = doc.getString("profile"),
//            token = doc.getString("token") ?: "",
//            ipLimit = doc.getString("ip_limit") ?: "",
//            description = doc.getString("description") ?: "",
//            content = doc.getString("content") ?: "",
//            v = doc.getInteger("v"),
//            createdAt = DateTime(doc.getDate("created_at")),
//            updatedBy = doc.getString("updated_by") ?: "",
//            deletedBy = doc.getString("deleted_by") ?: "",
//            users = doc["users"] as List<String>
//    )

}