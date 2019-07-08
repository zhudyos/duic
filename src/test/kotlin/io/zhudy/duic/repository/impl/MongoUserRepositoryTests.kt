package io.zhudy.duic.repository.impl

import com.mongodb.reactivestreams.client.MongoDatabase
import io.zhudy.duic.config.BasicConfiguration
import io.zhudy.duic.domain.Pageable
import io.zhudy.duic.domain.User
import io.zhudy.duic.repository.UserRepository
import io.zhudy.duic.repository.config.MongoConfiguration
import org.bson.Document
import org.junit.jupiter.api.AfterEach
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
internal class MongoUserRepositoryTests {

    @Autowired
    lateinit var userRepository: UserRepository
    @Autowired
    lateinit var mongo: MongoDatabase

    @AfterEach
    fun clean() {
        mongo.getCollection("user").deleteMany(Document()).toMono().block()
    }

    @Test
    fun insert() {
        val user = User(
                email = "${UUID.randomUUID()}@unit-test.com",
                password = "world",
                createdAt = Date()
        )

        val rs = userRepository.insert(user)
        StepVerifier.create(rs)
                .expectComplete()
                .verify()
    }

    @Test
    fun delete() {
        val user = User(
                email = "${UUID.randomUUID()}@unit-test.com",
                password = "world",
                createdAt = Date()
        )
        val rs1 = userRepository.insert(user)
        val rs2 = userRepository.delete(user.email)

        StepVerifier.create(rs1.then(rs2))
                .expectComplete()
                .verify()
    }

    @Test
    fun updatePassword() {
        val user = User(
                email = "${UUID.randomUUID()}@unit-test.com",
                password = "world",
                createdAt = Date()
        )
        val rs1 = userRepository.insert(user)
        val rs2 = userRepository.updatePassword(user.email, "hello")

        StepVerifier.create(rs1.then(rs2))
                .expectComplete()
                .verify()
    }

    @Test
    fun findByEmail() {
        val user = User(
                email = "${UUID.randomUUID()}@unit-test.com",
                password = "world",
                createdAt = Date()
        )
        val rs1 = userRepository.insert(user)
        val rs2 = userRepository.findByEmail(user.email)

        StepVerifier.create(rs1.then(rs2))
                .expectNextMatches { it.email == user.email }
                .expectComplete()
                .verify()
    }

    @Test
    fun `findByEmail not found`() {
        val rs1 = userRepository.findByEmail("${UUID.randomUUID()}@unit-test.com")

        StepVerifier.create(rs1)
                .expectComplete()
                .verify()
    }

    @Test
    fun findPage() {
        repeat(30) {
            val user = User(
                    email = "${UUID.randomUUID()}@unit-test.com",
                    password = "world",
                    createdAt = Date()
            )
            userRepository.insert(user).block()
        }

        val p = Pageable()
        val rs = userRepository.findPage(p)
        StepVerifier.create(rs)
                .expectNextMatches { it.totalItems == 30 && it.items.size == p.size }
                .expectComplete()
                .verify()
    }

    @Test
    fun findAllEmail() {
        repeat(30) {
            val user = User(
                    email = "${UUID.randomUUID()}@unit-test.com",
                    password = "world",
                    createdAt = Date()
            )
            userRepository.insert(user).block()
        }

        val rs = userRepository.findAllEmail()
        StepVerifier.create(rs)
                .expectNextCount(30)
                .expectComplete()
                .verify()
    }
}