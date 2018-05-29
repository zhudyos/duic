package io.zhudy.duic.repository.impl

import com.mongodb.client.model.Filters.gte
import com.mongodb.client.model.Filters.lt
import com.mongodb.client.model.IndexModel
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.reactivestreams.client.MongoDatabase
import io.zhudy.duic.domain.Server
import io.zhudy.duic.repository.ServerRepository
import io.zhudy.duic.repository.ServerRepository.Companion.ACTIVE_TIMEOUT_MINUTES
import io.zhudy.duic.repository.ServerRepository.Companion.CLEAN_BEFORE_MINUTES
import org.bson.Document
import org.joda.time.DateTime
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
open class MongoServerRepository(
        private val mongo: MongoDatabase
) : ServerRepository {

    private val serverColl: MongoCollection<Document>
        get() = mongo.getCollection("server")

    init {
        serverColl.createIndexes(listOf(
                IndexModel(
                        Document(mapOf("host" to 1, "port" to 1)),
                        IndexOptions().unique(true).background(true)
                ),
                IndexModel(
                        Document("active_at", 1),
                        IndexOptions().background(true)
                )
        ))
    }

    override fun register(host: String, port: Int) = serverColl.updateOne(
            Document("_id", "${host}_$port"),
            Document("\$set", mapOf(
                    "host" to host,
                    "port" to port,
                    "init_at" to Date(),
                    "active_at" to Date()
            )),
            UpdateOptions().upsert(true)
    ).toMono()

    override fun unregister(host: String, port: Int) = serverColl.deleteOne(
            Document("_id", "${host}_$port")
    ).toMono()

    override fun ping(host: String, port: Int) = serverColl.updateOne(
            Document("_id", "${host}_$port"),
            Updates.set("active_at", Date())
    ).toMono()

    override fun findServers() = serverColl.find(
            gte("active_at", DateTime.now().minusMinutes(ACTIVE_TIMEOUT_MINUTES).toDate())
    ).toFlux().map {
        Server(
                host = it.getString("host"),
                port = it.getInteger("port"),
                initAt = DateTime(it.getDate("init_at")),
                activeAt = DateTime(it.getDate("active_at"))
        )
    }

    override fun clean() = serverColl.deleteMany(
            lt("active_at", DateTime.now().minusMinutes(CLEAN_BEFORE_MINUTES).toDate())
    ).toMono()
}