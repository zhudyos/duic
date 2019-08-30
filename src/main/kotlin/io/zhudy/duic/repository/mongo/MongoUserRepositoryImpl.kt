package io.zhudy.duic.repository.mongo

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Projections.include
import com.mongodb.client.model.Updates.set
import io.zhudy.duic.domain.User
import io.zhudy.duic.repository.UserRepository
import org.bson.Document
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class MongoUserRepositoryImpl(
        private val ops: ReactiveMongoOperations
) : UserRepository {

    override fun insert(user: User): Mono<Int> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(email: String): Mono<Int> = Mono.defer {
        ops.execute("user") { coll ->
            coll.deleteOne(eq("email", email))
        }.next().map {
            it.deletedCount.toInt()
        }
    }

    override fun updatePassword(email: String, password: String): Mono<Int> = Mono.defer {
        ops.execute("user") { coll ->
            coll.updateOne(eq("email", email), set("password", password))
        }.next().map {
            it.modifiedCount.toInt()
        }
    }

    override fun findByEmail(email: String): Mono<User> = Mono.defer {
        ops.execute("user") { coll ->
            coll.find(eq("email", email))
        }.next().map(::mapToUser)
    }

    override fun findPage(pageable: Pageable): Mono<Page<User>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findAllEmail(): Flux<String> = Flux.defer {
        ops.execute("user") { coll ->
            coll.find().projection(include("email"))
        }.map {
            it.getString("email")
        }
    }

    private fun mapToUser(doc: Document) = User(
            email = doc.getString("email"),
            password = doc.getString("password")
    )
}