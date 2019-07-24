package io.zhudy.duic.repository.impl

import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.reactivestreams.client.MongoDatabase
import io.zhudy.duic.UserContext
import io.zhudy.duic.config.BasicConfiguration
import io.zhudy.duic.domain.App
import io.zhudy.duic.domain.Pageable
import io.zhudy.duic.repository.AppRepository
import io.zhudy.duic.repository.config.MongoConfiguration
import org.bson.Document
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.toMono
import reactor.test.StepVerifier
import java.time.Duration
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
internal class MongoAppRepositoryTests {

    @Autowired
    lateinit var appRepository: AppRepository
    @Autowired
    lateinit var mongo: MongoDatabase

    private val appColl: MongoCollection<Document>
        get() = mongo.getCollection("app")
    private val appHistoryColl: MongoCollection<Document>
        get() = mongo.getCollection("app_history")

    private val userContext = object : UserContext {
        override val email: String
            get() = "integration-test@weghst.com"
        override val isRoot: Boolean
            get() = true
    }

    @AfterEach
    fun cleanData() {
        appColl.deleteMany(Document()).toMono().block()
        appHistoryColl.deleteMany(Document()).toMono().block()
    }

    @Test
    fun insert() {
        val app = App(
                name = "junit",
                profile = "test",
                token = UUID.randomUUID().toString(),
                description = "integration test"
        )
        val rs = appRepository.insert(app)
        StepVerifier.create(rs)
                .expectComplete()
                .verify()
    }

    @Test
    fun delete() {
        val app = App(
                name = "junit",
                profile = "test",
                token = UUID.randomUUID().toString(),
                description = "integration test"
        )
        val rs1 = appRepository.insert(app)
        val rs2 = appRepository.delete(app, userContext)
        StepVerifier.create(rs1.then(rs2))
                .expectComplete()
                .verify()

        val c1 = appColl.countDocuments(and(eq("name", app.name), eq("profile", app.profile))).toMono().block()
        assertEquals(c1, 0)

        val c2 = appHistoryColl.countDocuments(and(
                eq("name", app.name),
                eq("profile", app.profile),
                eq("deleted_by", userContext.email)
        )).toMono().block()
        assertEquals(c2, 1)
    }

    @Test
    fun findOne() {
        val app = App(
                name = "junit",
                profile = "test",
                token = UUID.randomUUID().toString(),
                description = "integration test"
        )
        val rs1 = appRepository.insert(app)
        val rs2 = appRepository.findOne(app.name, app.profile)
        StepVerifier.create(rs1.then(rs2))
                .consumeNextWith {
                    assertEquals(app.token, it.token)
                    assertEquals(app.description, it.description)
                    assertNotNull(it.createdAt)
                    assertNotNull(it.updatedAt)
                }
                .expectComplete()
                .verify()
    }

    @Test
    fun update() {
        val app = App(
                name = "junit",
                profile = "test",
                token = UUID.randomUUID().toString(),
                description = "integration test"
        )
        val rs1 = appRepository.insert(app)

        val newApp = App(
                name = app.name,
                profile = app.profile,
                token = UUID.randomUUID().toString(),
                description = "integration test 2"
        )
        val rs2 = appRepository.update(newApp, userContext)
        StepVerifier.create(rs1.then(rs2))
                .expectComplete()
                .verify()

        val dbDoc = appColl.find(and(eq("name", app.name), eq("profile", app.profile))).toMono().block()
        assertEquals(newApp.token, dbDoc["token"])
        assertEquals(newApp.description, dbDoc["description"])

        val dbHisDoc = appHistoryColl.find(and(
                eq("name", app.name),
                eq("profile", app.profile),
                eq("updated_by", userContext.email)
        )).toMono().block()
        assertEquals(app.token, dbHisDoc["token"])
        assertEquals(app.description, dbHisDoc["description"])
    }

    @Test
    fun updateContent() {
        val app = App(
                name = "junit",
                profile = "test",
                token = UUID.randomUUID().toString(),
                description = "integration test"
        )
        val rs1 = appRepository.insert(app)

        val newApp = App(
                name = app.name,
                profile = app.profile,
                content = "a: hello"
        )
        val rs2 = appRepository.updateContent(newApp, userContext)
        StepVerifier.create(rs1.then(rs2))
                .expectComplete()
                .verify()

        val dbDoc = appColl.find(and(eq("name", app.name), eq("profile", app.profile))).toMono().block()
        assertEquals(newApp.content, dbDoc["content"])

        val dbHisDoc = appHistoryColl.find(and(
                eq("name", app.name),
                eq("profile", app.profile),
                eq("updated_by", userContext.email)
        )).toMono().block()
        assertEquals(app.content, dbHisDoc["content"])
    }

    @Test
    fun findAll() {
        for (i in 1..30) {
            val app = App(
                    name = "junit",
                    profile = "test$i",
                    token = UUID.randomUUID().toString(),
                    description = "integration test"
            )
            appRepository.insert(app).block()
        }

        val rs = appRepository.findAll()
        StepVerifier.create(rs)
                .expectNextCount(30)
                .expectComplete()
                .verify()
    }

    @Test
    fun findPage() {
        for (i in 1..30) {
            val app = App(
                    name = "junit",
                    profile = "test$i",
                    token = UUID.randomUUID().toString(),
                    description = "integration test"
            )
            appRepository.insert(app).block()
        }

        val pageable = Pageable()
        val rs = appRepository.findPage(pageable)
        StepVerifier.create(rs)
                .consumeNextWith {
                    assertEquals(30, it.totalItems)
                    assertEquals(pageable.size, it.items.size)
                }
                .expectComplete()
                .verify()
    }

    @Test
    fun findPageByUser() {
        for (i in 1..30) {
            val app = App(
                    name = "junit",
                    profile = "test$i",
                    token = UUID.randomUUID().toString(),
                    description = "integration test"
            )
            appRepository.insert(app).block()
        }

        val pageable = Pageable()
        val rs = appRepository.findPageByUser(pageable, userContext)
        StepVerifier.create(rs)
                .consumeNextWith {
                    assertEquals(0, it.totalItems)
                    assertEquals(0, it.items.size)
                }
                .expectComplete()
                .verify()
    }

    @Test
    fun searchPage() {
        for (i in 1..30) {
            val app = App(
                    name = "junit",
                    profile = "test$i",
                    token = UUID.randomUUID().toString(),
                    description = "integration test"
            )
            appRepository.insert(app).block()
        }

        val pageable = Pageable()
        val rs = appRepository.searchPage("junit", pageable)
        StepVerifier.create(rs)
                .consumeNextWith {
                    assertEquals(30, it.totalItems)
                    assertEquals(pageable.size, it.items.size)
                }
                .expectComplete()
                .verify()
    }

    @Test
    fun searchPageByUser() {
        for (i in 1..30) {
            val app = App(
                    name = "junit",
                    profile = "test$i",
                    token = UUID.randomUUID().toString(),
                    description = "integration test"
            )
            appRepository.insert(app).block()
        }

        val pageable = Pageable()
        val rs = appRepository.searchPageByUser("junit", pageable, userContext)
        StepVerifier.create(rs)
                .consumeNextWith {
                    assertEquals(0, it.totalItems)
                    assertEquals(0, it.items.size)
                }
                .expectComplete()
                .verify()
    }

    @Test
    fun findByUpdatedAt() {
        for (i in 1..30) {
            val app = App(
                    name = "junit",
                    profile = "test$i",
                    token = UUID.randomUUID().toString(),
                    description = "integration test"
            )
            appRepository.insert(app).block()
        }

        val rs = appRepository.findByUpdatedAt(Date.from(Instant.now().plusSeconds(10)))
        StepVerifier.create(rs)
                .expectNextCount(0)
                .expectComplete()
                .verify()
    }

    @Test
    fun findLast50History() {
        val app = App(
                name = "junit",
                profile = "test",
                token = UUID.randomUUID().toString(),
                description = "integration test"
        )
        appRepository.insert(app).block()

        for (i in 1..55) {
            val newApp = App(
                    name = app.name,
                    profile = app.profile,
                    description = "integration test $i"
            )
            appRepository.update(newApp, userContext).block()
        }

        val rs = appRepository.findLast50History(app.name, app.profile)
        StepVerifier.create(rs)
                .expectNextCount(50)
                .expectComplete()
                .verify()
    }

    @Test
    fun findAllNames() {
        for (i in 1..30) {
            val app = App(
                    name = "junit$i",
                    profile = "test",
                    token = UUID.randomUUID().toString(),
                    description = "integration test"
            )
            appRepository.insert(app).block()
        }

        val rs = appRepository.findAllNames()
        StepVerifier.create(rs)
                .expectNextCount(30)
                .expectComplete()
                .verify()
    }

    @Test
    fun findProfilesByName() {
        for (i in 1..30) {
            val app = App(
                    name = "junit",
                    profile = "test$i",
                    token = UUID.randomUUID().toString(),
                    description = "integration test"
            )
            appRepository.insert(app).block()
        }

        val rs = appRepository.findProfilesByName("junit")
        StepVerifier.create(rs)
                .expectNextCount(30)
                .expectComplete()
                .verify()
    }

    @Test
    fun findDeletedByCreatedAt() {
        for (i in 1..30) {
            val app = App(
                    name = "junit",
                    profile = "test$i",
                    token = UUID.randomUUID().toString(),
                    description = "integration test"
            )
            appRepository.insert(app)
                    .then(appRepository.delete(app, userContext))
                    .block()
        }

        val rs = appRepository.findDeletedByCreatedAt(Date.from(Instant.now().minus(Duration.ofHours(1))))
        StepVerifier.create(rs)
                .expectNextCount(30)
                .expectComplete()
                .verify()
    }

    @Test
    fun findLastDataTime() {
        val app = App(
                name = "junit",
                profile = "test",
                token = UUID.randomUUID().toString(),
                description = "integration test"
        )
        appRepository.insert(app).block()
        val dbApp = appRepository.findOne(app.name, app.profile).block()

        val rs = appRepository.findLastDataTime()
        StepVerifier.create(rs)
                .expectNext(dbApp.updatedAt!!.time)
                .expectComplete()
                .verify()
    }
}