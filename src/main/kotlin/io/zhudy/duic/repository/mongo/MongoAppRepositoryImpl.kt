package io.zhudy.duic.repository.mongo

import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Projections.include
import com.mongodb.client.model.Sorts.ascending
import com.mongodb.client.model.Sorts.descending
import com.mongodb.client.model.Updates.*
import io.zhudy.duic.domain.App
import io.zhudy.duic.domain.AppContentHistory
import io.zhudy.duic.domain.AppHistory
import io.zhudy.duic.domain.AppPair
import io.zhudy.duic.repository.AppRepository
import io.zhudy.duic.vo.AppVo
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.findAll
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import java.time.Instant

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class MongoAppRepositoryImpl(
        private val ops: ReactiveMongoOperations
) : AppRepository {

    override fun insert(vo: AppVo.NewApp): Mono<Int> = Mono.defer {
        val doc = Document()
        vo.apply {
            doc.apply {
                append("name", name)
                append("profile", profile)
                append("description", description)
                append("content", content)
                append("token", token)
                append("ip_limit", ipLimit)
                append("users", users)
                append("v", 1)
                append("created_at", Instant.now())
                append("updated_at", Instant.now())
            }
        }

        ops.execute("app") { coll -> coll.insertOne(doc) }.map { 1 }.next()
    }

    override fun insertHistory(appHistory: AppHistory): Mono<Int> = Mono.defer {
        val doc = Document()
        appHistory.apply {
            doc.apply {
                append("_id", ObjectId().toString())
                append("name", name)
                append("profile", profile)
                append("description", description)
                append("content", content)
                append("token", token)
                append("ip_limit", ipLimit)
                append("v", v)
                append("users", users)
                append("updated_by", updatedBy)
                append("deleted_by", deletedBy)
                append("created_at", Instant.now())
            }
        }

        ops.execute("app_history") { coll -> coll.insertOne(doc) }.map { 1 }.next()
    }

    override fun delete(ap: AppPair): Mono<Int> = Mono.defer {
        ops.execute("app") { coll ->
            coll.deleteOne(and(eq("name", ap.name), eq("profile", ap.profile)))
        }.map { it.deletedCount.toInt() }.next()
    }

    override fun findOne(ap: AppPair): Mono<App> = Mono.defer {
        ops.execute("app") { coll ->
            coll.find(and(eq("name", ap.name), eq("profile", ap.profile)))
        }.map(::mapToApp).next()
    }

    override fun update(ap: AppPair, vo: AppVo.UpdateBasicInfo): Mono<Int> = Mono.defer {
        ops.execute("app") { coll ->
            coll.updateOne(
                    and(eq("name", ap.name), eq("profile", ap.profile), eq("v", vo.v)),
                    combine(
                            set("description", vo.description),
                            set("token", vo.token),
                            set("ip_limit", vo.ipLimit),
                            set("users", vo.users)
                    )
            )
        }.map { it.modifiedCount.toInt() }.next()
    }

    override fun updateContent(ap: AppPair, vo: AppVo.UpdateContent): Mono<Int> = Mono.defer {
        ops.execute("app") { coll ->
            coll.updateOne(
                    and(eq("name", ap.name), eq("profile", ap.profile), eq("v", vo.v)),
                    combine(set("content", vo.content), inc("v", 1))
            )
        }.map { vo.v + 1 }.next()
    }

    override fun findAll(): Flux<App> = Flux.defer {
        ops.findAll<Document>("app").map(::mapToApp)
    }

    override fun search(vo: AppVo.UserQuery, pageable: Pageable): Mono<Page<App>> = Mono.defer {
        val filters = mutableListOf<Bson>()
        vo.run {
            if (q != null) {
                filters.add(text(q))
            }
            if (email != null) {
                filters.add(elemMatch("users", eq("\$eq", email)))
            }
        }

        val q = if (filters.isEmpty()) Document() else and(filters)

        ops.execute("app") { coll ->
            coll.find(q).skip(pageable.offset.toInt()).limit(pageable.pageSize).toFlux().map(::mapToApp).collectList()
                    .flatMap { items ->
                        coll.countDocuments(q).toMono().map { total ->
                            PageImpl(items, pageable, total)
                        }
                    }

        }.next()
    }

    override fun find4UpdatedAt(time: Instant): Flux<App> = Flux.defer {
        ops.execute("app") { coll ->
            coll.find(gt("updated_at", time)).sort(ascending("updated_at"))
        }.map(::mapToApp)
    }

    override fun findAppHistory(ap: AppPair, pageable: Pageable): Flux<AppContentHistory> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findAllNames(): Flux<String> = Flux.defer {
        ops.execute("app") { coll ->
            coll.find().projection(include("name"))
        }.map { it.getString("name") }
    }

    override fun findProfilesByName(name: String): Flux<String> = Flux.defer {
        ops.execute("app") { coll ->
            coll.find(eq("name", name)).projection(include("profile"))
        }.map { it.getString("profile") }
    }

    override fun findLatestDeleted(time: Instant): Flux<AppHistory> {
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
            name = doc.getString("name"),
            profile = doc.getString("profile"),
            description = doc.getString("description"),
            content = doc.getString("content"),
            token = doc.getString("token") ?: "",
            ipLimit = doc.getString("ip_limit") ?: "",
            v = doc.getInteger("v"),
            users = doc["users"] as List<String>,
            createdAt = doc.getDate("created_at").toInstant(),
            updatedAt = doc.getDate("updated_at").toInstant()
    )
}