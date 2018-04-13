/**
 * Copyright 2017-2018 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zhudy.duic.repository

import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.IndexModel
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Sorts.descending
import com.mongodb.client.model.Updates.*
import com.mongodb.reactivestreams.client.MongoDatabase
import io.zhudy.duic.BizCodeException
import io.zhudy.duic.BizCodes
import io.zhudy.duic.UserContext
import io.zhudy.duic.domain.*
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.joda.time.DateTime
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import reactor.core.publisher.SynchronousSink
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Repository
class AppRepository(
        mongoDatabase: MongoDatabase
) {

    private val appColl = mongoDatabase.getCollection("app")
    private val appHisColl = mongoDatabase.getCollection("app_history")

    init {
        appColl.createIndexes(listOf(
                IndexModel(Document(mapOf("name" to 1, "profile" to 1)), IndexOptions().background(true).unique(true)),
                IndexModel(Document("created_at", 1), IndexOptions().background(true)),
                IndexModel(Document("updated_at", 1), IndexOptions().background(true)),
                IndexModel(Document("users", 1), IndexOptions().background(true)),
                IndexModel(Document(mapOf("_fts" to "text", "_ftsx" to 1)), IndexOptions().background(true).weights(
                        Document(mapOf("content" to 1, "name" to 1, "profile" to 1))
                ))
        )).toMono().subscribe()

        appHisColl.createIndexes(listOf(
                IndexModel(Document(mapOf("name" to 1, "profile" to 1)), IndexOptions().background(true)),
                IndexModel(Document("created_at", 1), IndexOptions().background(true))
        )).toMono().subscribe()
    }

    /**
     *
     */
    fun insert(app: App) = appColl.insertOne(Document(
            mapOf(
                    "_id" to ObjectId().toString(),
                    "name" to app.name,
                    "profile" to app.profile,
                    "token" to app.token,
                    "description" to app.description,
                    "v" to app.v,
                    "created_at" to Date(),
                    "updated_at" to Date(),
                    "content" to app.content,
                    "users" to app.users
            )
    )).toMono()

    /**
     *
     */
    fun delete(app: App, userContext: UserContext) = findOne<Any>(app.name, app.profile)
            .flatMap { old ->
                appColl.deleteOne(and(eq("name", app.name), eq("profile", app.profile)))
                        .toMono()
                        .flatMap { rs ->
                            insertHistory(old, true, userContext).map {
                                rs.deletedCount.toInt()
                            }
                        }
            }

    /**
     *
     */
    fun <T> findOne(name: String, profile: String): Mono<App> {
        val q = and(eq("name", name), eq("profile", profile))
        return appColl.find(q).first().toMono().map(::mapToApp)
    }

    /**
     *
     */
    fun update(app: App, userContext: UserContext): Mono<Int> {
        val updatedAt = Date()
        val q = and(eq("name", app.name), eq("profile", app.profile), eq("v", app.v))
        val u = combine(
                set("token", app.token),
                set("ip_limit", app.ipLimit),
                set("description", app.description),
                set("users", app.users),
                set("updated_at", updatedAt)
        )
        return findOne<Any>(app.name, app.profile).flatMap { old ->
            appColl.updateOne(q, u).toMono().flatMap { rs ->
                if (rs.modifiedCount < 1) {
                    findOne<Any>(app.name, app.profile).handle { latestApp, _: SynchronousSink<App> ->
                        if (latestApp.v != old.v) {
                            throw BizCodeException(BizCodes.C_1004)
                        }

                        throw BizCodeException(BizCodes.C_1003, "修改 ${app.name}/${app.profile} 失败")
                    }
                } else {
                    insertHistory(old, false, userContext)
                }.map {
                    rs.modifiedCount.toInt()
                }
            }
        }
    }

    /**
     *
     */
    fun updateContent(app: App, userContext: UserContext): Mono<Int> {
        val updatedAt = Date()
        val q = and(eq("name", app.name), eq("profile", app.profile), eq("v", app.v))
        val u = combine(
                set("content", app.content),
                set("updated_at", updatedAt),
                inc("v", 1)
        )

        return findOne<Any>(app.name, app.profile).flatMap { old ->
            appColl.updateOne(q, u).toMono().flatMap { rs ->
                if (rs.modifiedCount < 1) {
                    findOne<Any>(app.name, app.profile).handle { latestApp, _: SynchronousSink<App> ->
                        if (latestApp.v != old.v) {
                            throw BizCodeException(BizCodes.C_1004)
                        }

                        throw BizCodeException(BizCodes.C_1003, "修改 ${app.name}/${app.profile} 失败")
                    }
                } else {
                    insertHistory(old, false, userContext)
                }.map {
                    rs.modifiedCount.toInt()
                }
            }
        }
    }

    /**
     *
     */
    fun findAll() = appColl.find().toFlux().map(::mapToApp)

    fun findPage(pageable: Pageable): Mono<Page<App>> {
        return findPage(Document(), pageable)
    }

    fun findPageByUser(pageable: Pageable, userContext: UserContext): Mono<Page<App>> {
        return findPage(elemMatch("users", eq(userContext.email)), pageable)
    }

    fun searchPage(q: String, pageable: Pageable): Mono<Page<App>> {
        val query = if (q.isEmpty()) Document() else text(q)
        return findPage(query, pageable)
    }

    fun searchPageByUser(q: String, pageable: Pageable, userContext: UserContext): Mono<Page<App>> {
        val wh = elemMatch("users", Document("\$eq", userContext.email))
        val query = if (q.isEmpty()) wh else and(text(q), wh)
        return findPage(query, pageable)
    }

    fun findByUpdatedAt(updateAt: Date) = appColl.find(gt("updated_at", updateAt)).toFlux().map(::mapToApp)

    /**
     *
     */
    fun findLast50History(name: String, profile: String) = appHisColl
            .find(and(eq("name", name), eq("profile", profile)))
            .sort(descending("created_at"))
            .toFlux()
            .map {
                AppContentHistory(
                        hid = it.get("_id").toString(),
                        updatedBy = it.getString("updated_by") ?: "",
                        content = it.getString("content"),
                        updatedAt = it.getDate("created_at")
                )
            }

    /**
     *
     */
    fun findAppHistoryByCreatedAt(createdAt: Date) = appHisColl
            .find(and(gt("created_at", createdAt), ne("deleted_by", "")))
            .toFlux()
            .map(::mapToAppHistory)

    private fun findPage(q: Bson, pageable: Pageable) = appColl
            .find(q).skip(pageable.offset).limit(pageable.size)
            .toFlux()
            .map(::mapToApp)
            .collectList()
            .flatMap { items ->
                Mono.from(appColl.count(q))
                        .map { total ->
                            Page(items, total.toInt(), pageable)
                        }
            }

    private fun insertHistory(app: App, delete: Boolean, userContext: UserContext) = appHisColl.insertOne(Document(
            mapOf(
                    "_id" to ObjectId().toString(),
                    "name" to app.name,
                    "profile" to app.profile,
                    "description" to app.description,
                    "v" to app.v,
                    "created_at" to Date(),
                    "content" to app.content,
                    "users" to app.users,
                    if (delete) "deleted_by" to userContext.email else "updated_by" to userContext.email
            )
    )).toMono()

    private fun mapToApp(doc: Document) = App(
            id = doc.get("_id").toString(),
            name = doc.getString("name"),
            profile = doc.getString("profile"),
            token = doc.getString("token") ?: "",
            ipLimit = doc.getString("ip_limit") ?: "",
            description = doc.getString("description"),
            v = doc.getInteger("v"),
            createdAt = DateTime(doc.getDate("created_at")),
            updatedAt = DateTime(doc.getDate("updated_at")),
            content = doc.getString("content"),
            users = doc.get("users") as List<String>
    )

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
            users = doc.get("users") as List<String>
    )
}