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
        mongoDatabase: MongoDatabase
) {

    val userColl = mongoDatabase.getCollection("user")

    init {
        userColl.createIndexes(listOf(
                IndexModel(Document("email", 1), IndexOptions().background(true).unique(true))
        )).toMono().subscribe()
    }

    /**
     *
     */
    fun insert(user: User) = userColl.insertOne(Document(
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
    fun delete(email: String) = userColl.deleteOne(eq("email", email)).toMono().map { it.deletedCount.toInt() }

    /**
     *
     */
    fun updatePassword(email: String, password: String) = userColl.updateOne(
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
    fun findByEmail(email: String): Mono<User> = userColl.find(eq("email", email))
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
    fun findPage(pageable: Pageable) = userColl.find()
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
                userColl.count().toMono().map { total ->
                    Page(items, total.toInt(), pageable)
                }
            }

    /**
     *
     */
    fun findAllEmail() = userColl.find()
            .projection(include("email"))
            .toFlux()
            .map { it.getString("email") }
}