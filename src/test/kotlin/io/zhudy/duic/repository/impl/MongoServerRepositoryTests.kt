package io.zhudy.duic.repository.impl

import com.mongodb.reactivestreams.client.MongoDatabase
import io.zhudy.duic.config.BasicConfiguration
import io.zhudy.duic.repository.ServerRepository
import io.zhudy.duic.repository.config.MongoConfiguration
import org.bson.Document
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.toMono
import reactor.test.StepVerifier
import java.time.Instant
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@SpringBootTest(classes = [
    MongoConfiguration::class,
    BasicConfiguration::class
])
@ActiveProfiles("mongodb")
internal class MongoServerRepositoryTests {

    @Autowired
    lateinit var serverRepository: ServerRepository
    @Autowired
    lateinit var mongo: MongoDatabase

    @AfterEach
    fun cleanData() {
        mongo.getCollection("server").deleteMany(Document()).toMono().block()
    }

    @Test
    fun register() {
        val rs = serverRepository.register("127.0.0.1", 1234)
        StepVerifier.create(rs)
                .expectComplete()
                .verify()
    }

    @Test
    fun unregister() {
        val rs1 = serverRepository.register("127.0.0.1", 1234)
        val rs2 = serverRepository.unregister("127.0.0.1", 1234)
        StepVerifier.create(rs1.then(rs2))
                .expectComplete()
                .verify()
    }

    @Test
    fun ping() {
        val rs1 = serverRepository.register("127.0.0.1", 1234)
        val rs2 = serverRepository.ping("127.0.0.1", 1234)
        StepVerifier.create(rs1.then(rs2))
                .expectComplete()
                .verify()
    }

    @Test
    fun findServers() {
        for (i in 1..10) {
            serverRepository.register("127.0.0.1", 1234 + i).block()
        }
        val rs = serverRepository.findServers()
        StepVerifier.create(rs)
                .expectNextCount(10)
                .expectComplete()
                .verify()
    }

    @Test
    fun `findServers not inactive`() {
        val serverColl = mongo.getCollection("server")
        serverColl.insertOne(Document(mapOf(
                "_id" to "127.0.0.1_1234",
                "host" to "127.0.0.1",
                "port" to 1234,
                "init_at" to Date.from(Instant.now().minus(ServerRepository.ACTIVE_TIMEOUT.plusMinutes(1))),
                "active_at" to Date.from(Instant.now().minus(ServerRepository.ACTIVE_TIMEOUT.plusMinutes(1)))
        )))

        val rs = serverRepository.findServers()
        StepVerifier.create(rs)
                .expectComplete()
                .verify()
    }

    @Test
    fun clean() {
        val serverColl = mongo.getCollection("server")
        serverColl.insertOne(Document(mapOf(
                "_id" to "127.0.0.1_1234",
                "host" to "127.0.0.1",
                "port" to 1234,
                "init_at" to Date.from(Instant.now().minus(ServerRepository.CLEAN_BEFORE.plusMinutes(1))),
                "active_at" to Date.from(Instant.now().minus(ServerRepository.CLEAN_BEFORE).plusMillis(1))
        )))

        val rs = serverRepository.clean()
        StepVerifier.create(rs)
                .expectComplete()
                .verify()

        assertEquals(serverColl.countDocuments().toMono().block(), 0)
    }

    @Test
    fun findDbVersion() {
        val rs = serverRepository.findDbVersion()
        StepVerifier.create(rs)
                .expectNextCount(1)
                .expectComplete()
                .verify()
    }
}