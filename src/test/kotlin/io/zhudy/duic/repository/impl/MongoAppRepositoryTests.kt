package io.zhudy.duic.repository.impl

import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.reactivestreams.client.MongoDatabase
import io.zhudy.duic.UserContext
import io.zhudy.duic.config.BasicConfiguration
import io.zhudy.duic.domain.App
import io.zhudy.duic.repository.AppRepository
import io.zhudy.duic.repository.config.MongoConfiguration
import org.bson.Document
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.toMono
import reactor.test.StepVerifier
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@SpringBootTest(classes = [MongoConfiguration::class])
@OverrideAutoConfiguration(enabled = false)
@ActiveProfiles("test", "mongodb")
@ImportAutoConfiguration(classes = [
    BasicConfiguration::class
])
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
    }

    @Test
    fun update() {
    }

    @Test
    fun updateContent() {
    }

    @Test
    fun findAll() {
    }

    @Test
    fun findPage() {
    }

    @Test
    fun findPageByUser() {
    }

    @Test
    fun searchPage() {
    }

    @Test
    fun searchPageByUser() {
    }

    @Test
    fun findByUpdatedAt() {
    }

    @Test
    fun findLast50History() {
    }

    @Test
    fun findAllNames() {
    }

    @Test
    fun findProfilesByName() {
    }

    @Test
    fun findDeletedByCreatedAt() {
    }

    @Test
    fun findLastDataTime() {
    }
}