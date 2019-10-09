package io.zhudy.duic.repository.mongo

import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Projections.include
import com.mongodb.client.model.Sorts.descending
import com.mongodb.client.model.Updates.*
import io.zhudy.duic.domain.*
import io.zhudy.duic.dto.AppQueryDto
import io.zhudy.duic.repository.AppRepository
import org.bson.Document
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class MongoAppRepositoryImpl(
        private val ops: ReactiveMongoOperations
) : AppRepository {

    override fun insert(app: App): Mono<Int> = Mono.defer {
        val doc = Document()
        app.apply {
            doc.apply {
                append("name", name)
                append("profile", profile)
                append("description", description)
                append("content", content)
                append("token", token)
                append("ip_limit", ipLimit)
                append("users", users)
                append("v", v)
                append("created_at", Instant.now())
                append("updated_at", Instant.now())
            }
        }

        ops.execute("app") { coll ->
            coll.insertOne(doc)
        }.next().map { 1 }
    }

    override fun insertHistory(appHistory: AppHistory): Mono<Int> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(name: String, profile: String): Mono<Int> = Mono.defer {
        ops.execute("app") { coll ->
            coll.deleteOne(and(eq("name", name), eq("profile", profile)))
        }.next().map {
            it.deletedCount.toInt()
        }
    }

    override fun findOne(name: String, profile: String): Mono<App> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(app: App): Mono<Int> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateContent(name: String, profile: String, content: String): Mono<Int> = Mono.defer {
        ops.execute("app") { coll ->
            coll.updateOne(
                    and(
                            eq("name", name),
                            eq("profile", profile),
                            // FIXME 修改需要判断数据版本
                            eq("v", 1)
                    ),
                    combine(
                            set("content", content),
                            inc("v", 1)
                    )
            )
        }.next().map {
            it.modifiedCount.toInt()
        }
    }

    override fun findAll(): Flux<App> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun searchPage(query: AppQueryDto, pageable: Pageable): Mono<Page<App>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findByUpdatedAt(updateAt: Instant): Flux<App> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findLast50History(name: String, profile: String): Flux<AppContentHistory> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findAllNames(): Flux<String> = Flux.defer {
        ops.execute("app") { coll ->
            coll.find().projection(include("name"))
        }.map {
            it.getString("name")
        }
    }

    override fun findProfilesByName(name: String): Flux<String> = Flux.defer {
        ops.execute("app") { coll ->
            coll.find(eq("name", name)).projection(include("profile"))
        }.map {
            it.getString("profile")
        }
    }

    override fun findDeletedByCreatedAt(createdAt: Instant): Flux<AppHistory> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findLastDataTime(): Mono<Long> = Mono.defer {
        ops.execute("ops") { coll ->
            coll.find().sort(descending("updated_at")).limit(1)
        }.next().map {
            it.getDate("updated_at").time
        }.defaultIfEmpty(0)
    }

    private fun mapToApp(doc: Document) = App(
            id = doc["_id"].toString(),
            name = doc.getString("name"),
            profile = doc.getString("profile"),
            description = doc.getString("description"),
            content = doc.getString("content"),
            token = doc.getString("token") ?: "",
            ipLimit = doc.getString("ip_limit") ?: "",
            v = doc.getInteger("v"),
            users = doc["users"] as List<String>,
            createdAt = doc.getDate("created_at"),
            updatedAt = doc.getDate("updated_at")
    )
}