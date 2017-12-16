package io.zhudy.duic.repository

import io.zhudy.duic.domain.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.ReactiveMongoOperations
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
class UserRepository(
        val mongoOperations: ReactiveMongoOperations
) {

    /**
     *
     */
    fun insert(user: User): Mono<User> = mongoOperations.insert(user)

    /**
     *
     */
    fun delete(email: String): Mono<Int> = mongoOperations.remove(Query(where("email").isEqualTo(email)),
            User::class.java).map { it.deletedCount.toInt() }

    /**
     *
     */
    fun updatePassword(email: String, password: String): Mono<Int> {
        val q = Query(where("email").isEqualTo(email))
        val u = Update().set("password", password).set("updated_at", Date())
        return mongoOperations.updateFirst(q, u, User::class.java).map { it.modifiedCount.toInt() }
    }

    /**
     *
     */
    fun findByEmail(email: String): Mono<User?> = mongoOperations.findOne(Query(where("email").isEqualTo(email)), User::class.java)

    /**
     *
     */
    fun findPage(pageable: Pageable): Mono<Page<User>> {
        val q = Query().with(pageable)
        q.fields().exclude("password")
        return mongoOperations.find(q, User::class.java).collectList().flatMap { items ->
            mongoOperations.count(q, User::class.java).map { total ->
                PageImpl<User>(items, pageable, total)
            }
        }
    }

    /**
     *
     */
    fun findAllEmail(): Flux<String> {
        val q = Query()
        q.fields().include("email")
        return mongoOperations.find(q, User::class.java).map { it.email }
    }
}