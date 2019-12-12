package io.zhudy.duic.repository.mongo

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Projections.include
import com.mongodb.client.model.Updates.set
import io.zhudy.duic.dto.UserDto
import io.zhudy.duic.repository.UserRepository
import io.zhudy.duic.vo.UserVo
import io.zhudy.kitty.core.util.toLocalDateTime
import org.bson.Document
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.index.Index
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import java.time.Instant
import java.time.LocalDateTime

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class MongoUserRepositoryImpl(
        private val ops: ReactiveMongoOperations
) : UserRepository {

    init {
        // email unique index
        ops.indexOps("user").ensureIndex(
                Index("email", Sort.Direction.ASC).named("email_unique").unique().background()
        ).subscribe()
    }

    override fun insert(vo: UserVo.NewUser): Mono<Int> = Mono.defer {
        ops.execute("user") { coll ->
            val d = Document().apply {
                append("email", vo.email)
                append("password", vo.password)
                append("created_at", Instant.now())
                append("updated_at", Instant.now())
            }
            coll.insertOne(d)
        }.map { 1 }.next()
    }

    override fun delete(email: String): Mono<Int> = Mono.defer {
        ops.execute("user") { coll ->
            coll.deleteOne(eq("email", email))
        }.map { it.deletedCount.toInt() }.next()
    }

    override fun updatePassword(email: String, password: String): Mono<Int> = Mono.defer {
        ops.execute("user") { coll ->
            coll.updateOne(eq("email", email), set("password", password))
        }.map { it.modifiedCount.toInt() }.next()
    }

    override fun findByEmail(email: String): Mono<UserDto> = Mono.defer {
        ops.execute("user") { coll ->
            coll.find(eq("email", email))
        }.map(::mapToUser).next()
    }

    override fun findPage(pageable: Pageable): Mono<Page<UserDto>> = Mono.defer {
        ops.execute("user") { coll ->
            // 记录列表
            coll.find()
                    .skip(pageable.offset.toInt())
                    .limit(pageable.pageSize)
                    .toFlux()
                    .map(::mapToUser)
                    .collectList()
                    .flatMap { items ->

                        // 记录数
                        coll.countDocuments().toMono().map { total ->
                            PageImpl(items, pageable, total)
                        }
                    }

        }.next()
    }

    override fun findAllEmail(): Flux<String> = Flux.defer {
        ops.execute("user") { coll ->
            coll.find().projection(include("email"))
        }.map { it.getString("email") }
    }

    private fun mapToUser(doc: Document) = UserDto(
            email = doc.getString("email"),
            password = doc.getString("password"),
            createdAt = doc.getDate("created_at").toLocalDateTime(),
            updatedAt = doc.getDate("updated_at")?.toLocalDateTime() ?: LocalDateTime.MIN
    )
}