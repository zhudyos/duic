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
import io.zhudy.duic.repository.ServerRepository.Companion.ACTIVE_TIMEOUT
import io.zhudy.duic.repository.ServerRepository.Companion.CLEAN_BEFORE
import org.bson.Document
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono
import java.time.Instant
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
            gte("active_at", Date.from(Instant.now().minus(ACTIVE_TIMEOUT)))
    ).toFlux().map {
        Server(
                host = it.getString("host"),
                port = it.getInteger("port"),
                initAt = it.getDate("init_at"),
                activeAt = it.getDate("active_at")
        )
    }

    override fun clean() = serverColl.deleteMany(
            lt("active_at", Date.from(Instant.now().minus(CLEAN_BEFORE)))
    ).toMono()

    override fun findDbVersion(): Mono<String> {
        return mongo.runCommand(Document("buildinfo", 1)).toMono().map {
            it.getString("version")
        }
    }
}