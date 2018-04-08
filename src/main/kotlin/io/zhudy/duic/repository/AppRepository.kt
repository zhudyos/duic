package io.zhudy.duic.repository

import io.zhudy.duic.BizCodeException
import io.zhudy.duic.BizCodes
import io.zhudy.duic.UserContext
import io.zhudy.duic.domain.App
import io.zhudy.duic.domain.AppContentHistory
import io.zhudy.duic.domain.AppHistory
import org.bson.types.ObjectId
import org.joda.time.DateTime
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.TextQuery
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

    /**
     *
     */
    fun insert(app: App) = mongoOperations.insert(app)!!

    /**
     *
     */
    fun delete(app: App, userContext: UserContext): Mono<Long> {
        val q = Query(where("name").isEqualTo(app.name).and("profile").isEqualTo(app.profile))
        return findOne(app.name, app.profile).flatMap { old ->
            mongoOperations.remove(q, App::class.java).flatMap { rs ->
                insertHistory(old, true, userContext).map {
                    rs.deletedCount
                }
            }
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
        val u = Update()
                .set("token", app.token)
                .set("ip_limit", app.ipLimit)
                .set("description", app.description)
                .set("users", app.users)
                .set("updated_at", updatedAt)

        return findOne(app.name, app.profile).flatMap { old ->
            mongoOperations.updateFirst(q, u, App::class.java).flatMap { m ->
                if (m.modifiedCount < 1) {
                    throw BizCodeException(BizCodes.C_1003, "修改 ${app.name}/${app.profile} 失败")
                }

                insertHistory(old, false, userContext).map {
                    m.modifiedCount.toInt()
                }
            }
        }
    }

    /**
     *
     */
    fun updateContent(app: App, userContext: UserContext): Mono<Int> {
        return findOne(app.name, app.profile).flatMap { old ->
            val updatedAt = Date()
            val q = Query(where("name").isEqualTo(app.name).and("profile").isEqualTo(app.profile).and("v").isEqualTo(app.v))
            val u = Update()
                    .set("content", app.content)
                    .set("updated_at", updatedAt)
                    .inc("v", 1)

            mongoOperations.updateFirst(q, u, App::class.java).flatMap {
                if (it.modifiedCount < 1) {
                    if (app.v != old.v) {
                        throw BizCodeException(BizCodes.C_1004)
                    }
                    throw BizCodeException(BizCodes.C_1003, "修改 ${app.name}/${app.profile} 失败")
                }

                insertHistory(old, false, userContext).map {
                    app.v + 1
                }
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
        return findPage(Query(), pageable)
    }

    /**
     *
     */
    fun findPageByUser(pageable: Pageable, userContext: UserContext): Mono<Page<App>> {
        val q = Query(where("users").elemMatch(where("\$eq").isEqualTo(userContext.email)))
        return findPage(q, pageable)
    }

    fun searchPage(q: String, pageable: Pageable): Mono<Page<App>> {
        val query = if (q.isEmpty()) Query() else TextQuery(q)
        return findPage(query, pageable)
    }

    fun searchPageByUser(q: String, pageable: Pageable, userContext: UserContext): Mono<Page<App>> {
        val wh = where("users").elemMatch(where("\$eq").isEqualTo(userContext.email))
        val query = if (q.isEmpty()) Query(wh) else TextQuery(q).addCriteria(wh)
        return findPage(query, pageable)
    }

    /**
     *
     */
    fun findByUpdatedAt(updateAt: Date): Flux<App> {
        val q = Query(where("updated_at").gt(updateAt))
        return mongoOperations.find(q, App::class.java)
    }

    /**
     *
     */
    fun findLast50History(name: String, profile: String): Flux<AppContentHistory> {
        val q = Query(where("name").isEqualTo(name).and("profile").isEqualTo(profile))
        q.with(Sort.by(Sort.Direction.DESC, "created_at"))
        return mongoOperations.find(q, AppHistory::class.java).map {
            AppContentHistory(
                    hid = it.id,
                    updatedBy = it.updatedBy,
                    content = it.content,
                    updatedAt = it.createdAt?.toDate() ?: Date()
            )
        }
    }

    /**
     *
     */
    fun findAppHistoryByCreatedAt(createdAt: Date): Flux<AppHistory> {
        val q = Query(where("created_at").gt(createdAt))
        return mongoOperations.find(q, AppHistory::class.java)
    }

    private fun findPage(q: Query, pageable: Pageable): Mono<Page<App>> {
        return mongoOperations.find(q.with(pageable), App::class.java).collectList().flatMap { items ->
            mongoOperations.count(q, App::class.java).map { total ->
                PageImpl(items, pageable, total)
            }
        }
    }

    private fun insertHistory(app: App, delete: Boolean, userContext: UserContext): Mono<AppHistory> {
        val appHis = AppHistory(
                id = ObjectId().toString(),
                name = app.name,
                profile = app.profile,
                description = app.description,
                v = app.v,
                createdAt = DateTime.now(),
                content = app.content,
                users = app.users
        )
        if (delete) appHis.deletedBy = userContext.email else appHis.updatedBy = userContext.email

        return mongoOperations.insert(appHis)
    }
}