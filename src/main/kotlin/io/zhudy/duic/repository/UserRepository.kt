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

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.IndexModel
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Projections.exclude
import com.mongodb.client.model.Projections.include
import com.mongodb.client.model.Updates.combine
import com.mongodb.client.model.Updates.set
import com.mongodb.reactivestreams.client.MongoDatabase
import io.zhudy.duic.domain.Page
import io.zhudy.duic.domain.Pageable
import io.zhudy.duic.domain.User
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Repository
class UserRepository(
        private val mongo: MongoDatabase
) {

    companion object {
        private val USER_COLL_NAME = "user"
    }

    init {
        mongo.getCollection(USER_COLL_NAME).createIndexes(listOf(
                IndexModel(Document("email", 1), IndexOptions().background(true).unique(true))
        )).toMono().subscribe()
    }

    /**
     *
     */
    fun insert(user: User) = mongo.getCollection(USER_COLL_NAME).insertOne(Document(
            mapOf(
                    "_id" to ObjectId().toString(),
                    "email" to user.email,
                    "password" to user.password,
                    "created_at" to Date(),
                    "updated_at" to Date()
            )
    )).toMono()

    /**
     *
     */
    fun delete(email: String) = mongo.getCollection(USER_COLL_NAME).deleteOne(eq("email", email))
            .toMono()
            .map { it.deletedCount.toInt() }

    /**
     *
     */
    fun updatePassword(email: String, password: String) = mongo.getCollection(USER_COLL_NAME).updateOne(
            eq("email", email),
            combine(
                    set("password", password),
                    set("updated_at", Date())
            ))
            .toMono()
            .map { it.modifiedCount.toInt() }

    /**
     *
     */
    fun findByEmail(email: String): Mono<User> = mongo.getCollection(USER_COLL_NAME).find(eq("email", email))
            .first()
            .toMono()
            .map {
                User(
                        email = it.getString("email"),
                        password = it.getString("password")
                )
            }

    /**
     *
     */
    fun findPage(pageable: Pageable) = mongo.getCollection(USER_COLL_NAME).find()
            .projection(exclude("password"))
            .skip(pageable.offset)
            .limit(pageable.size)
            .toFlux()
            .map {
                User(
                        email = it.getString("email"),
                        createdAt = it.getDate("created_at"),
                        updatedAt = it.getDate("updated_at")
                )
            }
            .collectList()
            .flatMap { items ->
                mongo.getCollection(USER_COLL_NAME).count().toMono().map { total ->
                    Page(items, total.toInt(), pageable)
                }
            }

    /**
     *
     */
    fun findAllEmail() = mongo.getCollection(USER_COLL_NAME).find()
            .projection(include("email"))
            .toFlux()
            .map { it.getString("email") }
}