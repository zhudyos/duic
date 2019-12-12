package io.zhudy.duic.repository.mongo

import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates.combine
import com.mongodb.client.model.Updates.set
import io.zhudy.duic.domain.Server
import io.zhudy.duic.repository.ServerRepository
import io.zhudy.duic.repository.ServerRepository.Companion.ACTIVE_TIMEOUT
import io.zhudy.duic.repository.ServerRepository.Companion.CLEAN_BEFORE
import io.zhudy.kitty.core.util.toLocalDateTime
import org.bson.Document
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class MongoServerRepositoryImpl(
        private val ops: ReactiveMongoOperations
) : ServerRepository {

    override fun register(host: String, port: Int): Mono<Int> = Mono.defer {
        ops.execute("server") { coll ->
            coll.updateOne(
                    eq("_id", "${host}_$port"),
                    combine(
                            set("host", host),
                            set("port", port),
                            set("init_at", LocalDateTime.now()),
                            set("active_at", LocalDateTime.now())
                    ),
                    UpdateOptions().upsert(true)
            )
        }.map { if (it.upsertedId != null) 1 else it.modifiedCount.toInt() }.next()
    }

    override fun unregister(host: String, port: Int): Mono<Int> = Mono.defer {
        ops.execute("server") { coll ->
            coll.deleteOne(eq("_id", "${host}_$port"))
        }.map { it.deletedCount.toInt() }.next()
    }

    override fun ping(host: String, port: Int): Mono<Int> = Mono.defer {
        ops.execute("server") { coll ->
            coll.updateOne(eq("_id", "${host}_$port"), set("active_at", LocalDateTime.now()))
        }.map { it.modifiedCount.toInt() }.next()
    }

    override fun findServers(): Flux<Server> = Flux.defer {
        ops.execute("server") { coll ->
            coll.find(gte("active_at", LocalDateTime.now().minus(ACTIVE_TIMEOUT)))
        }.map {
            Server(
                    host = it.getString("host"),
                    port = it.getInteger("port"),
                    initAt = it.getDate("init_at").toLocalDateTime(),
                    activeAt = it.getDate("active_at").toLocalDateTime()
            )
        }
    }

    override fun clean(): Mono<Int> = Mono.defer {
        ops.execute("server") { coll ->
            coll.deleteMany(lt("active_at", LocalDateTime.now().minus(CLEAN_BEFORE)))
        }.map { it.deletedCount.toInt() }.next()
    }

    override fun findDbVersion(): Mono<String> = Mono.defer {
        ops.execute { db ->
            db.runCommand(Document("buildinfo", 1))
        }.map { it.getString("version") }.next()
    }
}