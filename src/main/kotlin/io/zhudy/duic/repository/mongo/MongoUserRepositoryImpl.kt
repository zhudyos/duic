package io.zhudy.duic.repository.mongo

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Projections.include
import com.mongodb.client.model.Updates.set
import io.zhudy.duic.dto.NewUserDto
import io.zhudy.duic.dto.UserDto
import io.zhudy.duic.repository.UserRepository
import org.bson.Document
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import java.time.Instant

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class MongoUserRepositoryImpl(
        private val ops: ReactiveMongoOperations
) : UserRepository {

    override fun insert(user: NewUserDto): Mono<Int> = Mono.defer {
        ops.execute("user") { coll ->
            val d = Document().apply {
                append("email", user.email)
                append("password", user.password)
                append("created_at", Instant.now())
                append("updated_at", Instant.now())
            }
            coll.insertOne(d)
        }.next().map { 1 }
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

    override fun findByEmail(email: String): Mono<UserDto> = Mono.defer {
        ops.execute("user") { coll ->
            coll.find(eq("email", email))
        }.next().map(::mapToUser)
    }

    override fun findPage(pageable: Pageable): Mono<Page<UserDto>> = Mono.defer {
        ops.execute("user") { coll ->
            // 记录列表
            val content = coll.find()
                    .skip(pageable.offset.toInt())
                    .limit(pageable.pageSize)
                    .toFlux()
                    .map(::mapToUser)

            // 记录数
            val count = coll.countDocuments().toMono()
            Mono.zip(content.collectList(), count).map {
                PageImpl(it.t1, pageable, it.t2)
            }
        }.next()
    }

    override fun findAllEmail(): Flux<String> = Flux.defer {
        ops.execute("user") { coll ->
            coll.find().projection(include("email"))
        }.map {
            it.getString("email")
        }
    }

    private fun mapToUser(doc: Document) = UserDto(
            email = doc.getString("email"),
            password = doc.getString("password"),
            createdAt = doc.getDate("created_at").toInstant(),
            updatedAt = doc.getDate("updated_at").toInstant()
    )
}