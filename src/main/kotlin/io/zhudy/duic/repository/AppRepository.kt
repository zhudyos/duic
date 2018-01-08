package io.zhudy.duic.repository

import io.zhudy.duic.BizCodeException
import io.zhudy.duic.BizCodes
import io.zhudy.duic.UserContext
import io.zhudy.duic.domain.App
import io.zhudy.duic.domain.AppContentHistory
import io.zhudy.duic.domain.AppHistory
import org.bson.Document
import org.hashids.Hashids
import org.joda.time.DateTime
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Repository
class AppRepository(
        private val mongoOperations: ReactiveMongoOperations
) {

    private val hashids = Hashids()

    /**
     *
     */
    fun insert(app: App) = mongoOperations.insert(app)!!

    /**
     *
     */
    fun delete(app: App, appHistory: AppHistory): Mono<Unit> {
        val q = Query(where("name").isEqualTo(app.name).and("profile").isEqualTo(app.profile))
        return mongoOperations.findAndRemove(q, App::class.java).flatMap {
            it.createdAt = DateTime.now()
            it.updatedAt = DateTime.now()

            mongoOperations.insert(appHistory).map { Unit }
        }
    }

    /**
     *
     */
    fun findOne(name: String, profile: String): Mono<App> {
        val q = Query(where("name").isEqualTo(name).and("profile").isEqualTo(profile))
        return mongoOperations.findOne(q, App::class.java)
    }

    /**
     *
     */
    fun update(app: App, userContext: UserContext): Mono<Int> {
        val updatedAt = Date()
        val q = Query(where("name").isEqualTo(app.name).and("profile").isEqualTo(app.profile).and("v").isEqualTo(app.v))
        val u = Update().set("token", app.token).set("description", app.description).set("users", app.users)
                .set("updated_at", updatedAt)
                .push("histories").atPosition(0).value(mapOf(
                "hid" to hashids.encode(app.id.hashCode().toLong(), System.currentTimeMillis(), app.v.toLong()),
                "token" to app.token,
                "revised_by" to userContext.email,
                "updated_at" to updatedAt
        ))
        return mongoOperations.updateFirst(q, u, App::class.java).map {
            if (it.modifiedCount < 1) {
                throw BizCodeException(BizCodes.C_1003, "修改 ${app.name}/${app.profile} 失败")
            }
            it.modifiedCount.toInt()
        }
    }

    /**
     *
     */
    fun updateContent(app: App, userContext: UserContext): Mono<Int> {
        return findOne(app.name, app.profile).flatMap { old ->
            val updatedAt = Date()
            val q = Query(where("name").isEqualTo(app.name).and("profile").isEqualTo(app.profile).and("v").isEqualTo(app.v))
            val u = Update().set("content", app.content).set("updated_at", updatedAt).inc("v", 1)
                    .push("histories").atPosition(0).value(mapOf(
                    "hid" to hashids.encode(app.id.hashCode().toLong(), System.currentTimeMillis(), app.v.toLong()),
                    "content" to old.content,
                    "revised_by" to userContext.email,
                    "updated_at" to updatedAt
            ))

            mongoOperations.updateFirst(q, u, App::class.java).map {
                if (it.modifiedCount < 1) {
                    if (app.v != old.v) {
                        throw BizCodeException(BizCodes.C_1004)
                    }
                    throw BizCodeException(BizCodes.C_1003, "修改 ${app.name}/${app.profile} 失败")
                }
                app.v + 1
            }
        }
    }

    /**
     *
     */
    fun findAll() = mongoOperations.findAll(App::class.java)!!

    /**
     *
     */
    fun findPage(pageable: Pageable): Mono<Page<App>> {
        val q = Query().with(pageable)
        return mongoOperations.find(q, App::class.java).collectList().flatMap { items ->
            mongoOperations.count(q, App::class.java).map { total ->
                PageImpl(items, pageable, total)
            }
        }
    }

    /**
     *
     */
    fun findPageByUser(pageable: Pageable, userContext: UserContext): Mono<Page<App>> {
        val q = Query(where("users").elemMatch(where("\$eq").isEqualTo(userContext.email))).with(pageable)
        return mongoOperations.find(q, App::class.java).collectList().flatMap { items ->
            mongoOperations.count(q, App::class.java).map { total ->
                PageImpl(items, pageable, total)
            }
        }
    }

    /**
     *
     */
    fun findByUpdatedAt(updateAt: Date): Flux<App> {
        val q = Query(where("updated_at").gte(updateAt))
        return mongoOperations.find(q, App::class.java)
    }

    /**
     *
     */
    fun findLast50History(name: String, profile: String): Flux<AppContentHistory> {
        val q = Query(where("name").isEqualTo(name).and("profile").isEqualTo(profile))
        q.fields().slice("histories", 50)

        return mongoOperations.findOne<Document>(q, "app").flatMapIterable {
            it["histories"] as List<Document>
        }.map {
            AppContentHistory(
                    hid = it.getString("hid"),
                    revisedBy = it.getString("revised_by") ?: "",
                    content = it.getString("content") ?: "",
                    updatedAt = it.getDate("updated_at")
            )
        }
    }

    /**
     *
     */
    fun findAppHistoryByCreatedAt(createdAt: Date): Flux<AppHistory> {
        val q = Query(where("created_at").gte(createdAt))
        return mongoOperations.find(q, AppHistory::class.java)
    }
}