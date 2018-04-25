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
package io.zhudy.duic.repository.impl

import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.IndexModel
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Projections.include
import com.mongodb.client.model.Sorts.ascending
import com.mongodb.client.model.Sorts.descending
import com.mongodb.client.model.Updates.*
import com.mongodb.reactivestreams.client.MongoDatabase
import io.zhudy.duic.BizCodeException
import io.zhudy.duic.BizCodes
import io.zhudy.duic.UserContext
import io.zhudy.duic.domain.*
import io.zhudy.duic.repository.AppRepository
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.joda.time.DateTime
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono
import reactor.core.scheduler.Schedulers
import java.util.*

/**
 * MongoDB `app` 表数据操作实现。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
class MongoAppRepository(
        private val mongo: MongoDatabase
) : AppRepository {

    companion object {
        private val APP_COLL_NAME = "app"
        private val APP_HIS_COLL_NAME = "app_history"
    }

    init {
        mongo.getCollection(APP_COLL_NAME).createIndexes(listOf(
                IndexModel(Document(mapOf("name" to 1, "profile" to 1)), IndexOptions().background(true).unique(true)),
                IndexModel(Document("created_at", 1), IndexOptions().background(true)),
                IndexModel(Document("updated_at", 1), IndexOptions().background(true)),
                IndexModel(Document("users", 1), IndexOptions().background(true)),
                IndexModel(Document(mapOf("_fts" to "text", "_ftsx" to 1)), IndexOptions().background(true).weights(
                        Document(mapOf("content" to 1, "name" to 1, "profile" to 1))
                ))
        )).toMono().subscribe()

        mongo.getCollection(APP_HIS_COLL_NAME).createIndexes(listOf(
                IndexModel(Document(mapOf("name" to 1, "profile" to 1)), IndexOptions().background(true)),
                IndexModel(Document("created_at", 1), IndexOptions().background(true))
        )).toMono().subscribe()
    }

    /**
     * 保存应用配置信息。
     *
     * @param app 应用配置信息
     */
    override fun insert(app: App) = mongo.getCollection(APP_COLL_NAME).insertOne(Document(
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
     * 删除应用配置信息，并在 `app_history` 中保存已删除的应用配置信息。
     *
     * @param app 应用配置信息
     * @param userContext 用户上下文
     */
    override fun delete(app: App, userContext: UserContext) = findOne<Any>(app.name, app.profile).flatMap { dbApp ->
        mongo.getCollection(APP_COLL_NAME).deleteOne(and(eq("name", app.name), eq("profile", app.profile)))
                .toMono()
                .flatMap {
                    insertHistory(dbApp, true, userContext)
                }
    }

    /**
     * 返回指定的应用配置信息。
     *
     * @param name 应用名称
     * @param profile 应用配置
     */
    override fun <T> findOne(name: String, profile: String): Mono<App> {
        val q = and(eq("name", name), eq("profile", profile))
        return mongo.getCollection(APP_COLL_NAME).find(q).first().toMono().map(::mapToApp)
    }

    /**
     * 更新指定的应用配置信息，并在 `app_history` 中保存被更新的应用配置信息。
     *
     * @param app 更新的应用配置信息
     * @param userContext 用户上下文
     */
    override fun update(app: App, userContext: UserContext): Mono<Int> {
        val updatedAt = Date()
        val q = and(eq("name", app.name), eq("profile", app.profile), eq("v", app.v))
        val u = combine(
                set("token", app.token),
                set("ip_limit", app.ipLimit),
                set("description", app.description),
                set("users", app.users),
                set("updated_at", updatedAt)
        )
        return findOne<Any>(app.name, app.profile).flatMap { dbApp ->
            mongo.getCollection(APP_COLL_NAME).updateOne(q, u).toMono().map { rs ->
                if (rs.modifiedCount < 1) {
                    throw BizCodeException(BizCodes.C_1003, "修改 ${app.name}/${app.profile} 失败")
                }
                insertHistory(dbApp, false, userContext)
                dbApp.v
            }
        }
    }

    /**
     * 更新应用配置信息，并在 `app_history` 中保存被更新的应用配置信息。
     *
     * @param app 更新的应用配置信息
     * @param userContext 用户上下文
     * @return 当前应用版本
     */
    override fun updateContent(app: App, userContext: UserContext): Mono<Int> {
        val updatedAt = Date()
        val q = and(eq("name", app.name), eq("profile", app.profile), eq("v", app.v))
        val u = combine(
                set("content", app.content),
                set("updated_at", updatedAt),
                inc("v", 1)
        )

        return findOne<Any>(app.name, app.profile).flatMap { dbApp ->
            mongo.getCollection(APP_COLL_NAME).updateOne(q, u).toMono().map { rs ->
                if (rs.modifiedCount < 1) {
                    if (app.v != dbApp.v) {
                        throw BizCodeException(BizCodes.C_1004)
                    }

                    throw BizCodeException(BizCodes.C_1003, "修改 ${app.name}/${app.profile} 失败")
                }
                insertHistory(dbApp, false, userContext)

                app.v + 1
            }
        }
    }

    /**
     * 返回所有应用配置信息，并且按更新时间 `updated_at` 升序排列。
     */
    override fun findAll() = mongo.getCollection(APP_COLL_NAME).find().sort(ascending("updated_at")).toFlux().map(::mapToApp)

    /**
     * 分页返回应用配置信息。
     *
     * @param pageable 分页参数
     */
    override fun findPage(pageable: Pageable): Mono<Page<App>> {
        return findPage(Document(), pageable)
    }

    /**
     * 分页返回所属用户的应用配置信息。
     *
     * @param pageable 分页参数
     * @param userContext 用户上下文
     */
    override fun findPageByUser(pageable: Pageable, userContext: UserContext): Mono<Page<App>> {
        return findPage(elemMatch("users", eq(userContext.email)), pageable)
    }

    /**
     * 搜索应用配置并分页返回。
     *
     * @param q 搜索关键字
     * @param pageable 分页参数
     */
    override fun searchPage(q: String, pageable: Pageable): Mono<Page<App>> {
        val query = if (q.isEmpty()) Document() else text(q)
        return findPage(query, pageable)
    }

    /**
     * 搜索所属用户应用配置并分页返回。
     *
     * @param q 搜索关键字
     * @param pageable 分页参数
     * @param userContext 用户上下文
     */
    override fun searchPageByUser(q: String, pageable: Pageable, userContext: UserContext): Mono<Page<App>> {
        val wh = elemMatch("users", Document("\$eq", userContext.email))
        val query = if (q.isEmpty()) wh else and(text(q), wh)
        return findPage(query, pageable)
    }

    /**
     * 返回大于指定更新时间的应用配置信息，并且按更新时间 `updated_at` 升序排列。
     *
     * @param updateAt 应用更新时间
     */
    override fun findByUpdatedAt(updateAt: Date) = mongo.getCollection(APP_COLL_NAME)
            .find(gt("updated_at", updateAt))
            .sort(ascending("updated_at"))
            .toFlux().map(::mapToApp)

    /**
     * 返回应用配置最新 50 条修改记录信息。
     *
     * @param name 应用名称
     * @param profile 应用配置
     */
    override fun findLast50History(name: String, profile: String) = mongo.getCollection(APP_HIS_COLL_NAME)
            .find(and(eq("name", name), eq("profile", profile)))
            .sort(descending("created_at"))
            .toFlux()
            .map {
                AppContentHistory(
                        hid = it["_id"].toString(),
                        updatedBy = it.getString("updated_by") ?: "",
                        content = it.getString("content"),
                        updatedAt = it.getDate("created_at")
                )
            }

    /**
     * 返回所有的应用名称。
     */
    override fun findAllNames() = mongo.getCollection(APP_COLL_NAME).distinct("name", String::class.java).toFlux()

    /**
     * 返回应用名称下所有的应用配置名称。
     */
    override fun findProfilesByName(name: String) = mongo.getCollection(APP_COLL_NAME)
            .find(eq("name", name))
            .projection(include("profile"))
            .toFlux()
            .map { it.getString("profile") }

    /**
     * 返回指定时间以后删除的应用配置信息。
     *
     * @param createdAt 删除时间
     */
    override fun findDeletedByCreatedAt(createdAt: Date) = mongo.getCollection(APP_HIS_COLL_NAME)
            .find(and(gt("created_at", createdAt), ne("deleted_by", "")))
            .sort(ascending("created_at"))
            .toFlux()
            .map(::mapToAppHistory)

    private fun findPage(q: Bson, pageable: Pageable) = Mono.zip(
            mongo.getCollection(APP_COLL_NAME)
                    .find(q).skip(pageable.offset).limit(pageable.size)
                    .toFlux()
                    .publishOn(Schedulers.parallel())
                    .map(::mapToApp)
                    .collectList(),
            mongo.getCollection(APP_COLL_NAME)
                    .count(q)
                    .toMono()
    ).map {
        Page(it.t1, it.t2.toInt(), pageable)
    }

    private fun insertHistory(app: App, delete: Boolean, userContext: UserContext) = mongo.getCollection(APP_HIS_COLL_NAME).insertOne(Document(
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