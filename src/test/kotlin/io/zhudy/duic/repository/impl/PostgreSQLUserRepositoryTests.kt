package io.zhudy.duic.repository.impl

import io.zhudy.duic.domain.Pageable
import io.zhudy.duic.domain.User
import io.zhudy.duic.repository.UserRepository
import io.zhudy.duic.repository.config.PostgreSQLConfiguration
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.ContextHierarchy
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests
import org.springframework.transaction.support.TransactionTemplate
import reactor.test.StepVerifier
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@ContextHierarchy(*[
ContextConfiguration(locations = ["classpath:postgresql-spring.xml"]),
ContextConfiguration(classes = [PostgreSQLConfiguration::class])
])
@TestPropertySource(properties = ["duic.dbms=PostgreSQL"])
class PostgreSQLUserRepositoryTests : AbstractJUnit4SpringContextTests() {

    @Autowired
    lateinit var transactionTemplate: TransactionTemplate
    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate
    @Autowired
    lateinit var userRepository: UserRepository

    @After
    fun clean() {
        transactionTemplate.execute {
            jdbcTemplate.execute("""delete from "user"""")
        }
    }

    @Test
    fun insert() {
        val user = User(
                email = "${UUID.randomUUID()}@unit-test.com",
                password = "world",
                createdAt = Date()
        )

        StepVerifier.create(userRepository.insert(user))
                .expectNext(1)
                .verifyComplete()
    }

    @Test
    fun delete() {
        val user = User(
                email = "${UUID.randomUUID()}@unit-test.com",
                password = "world",
                createdAt = Date()
        )
        userRepository.insert(user).block()

        StepVerifier.create(userRepository.delete(user.email))
                .expectNext(1)
                .verifyComplete()
    }

    @Test
    fun updatePassword() {
        val user = User(
                email = "${UUID.randomUUID()}@unit-test.com",
                password = "world",
                createdAt = Date()
        )
        userRepository.insert(user).block()

        StepVerifier.create(userRepository.updatePassword(user.email, "hello"))
                .expectNext(1)
                .verifyComplete()
    }

    @Test
    fun findByEmail() {
        val user = User(
                email = "${UUID.randomUUID()}@unit-test.com",
                password = "world",
                createdAt = Date()
        )
        userRepository.insert(user).block()

        StepVerifier.create(userRepository.findByEmail(user.email))
                .expectNextMatches {
                    it.email == user.email
                }
                .verifyComplete()
    }

    @Test
    fun `findByEmail Not Found`() {
        StepVerifier.create(userRepository.findByEmail("${UUID.randomUUID()}@unit-test.com"))
                .expectNextCount(0)
                .verifyComplete()
    }

    @Test
    fun findPage() {
        for (n in 1..30) {
            val user = User(
                    email = "${UUID.randomUUID()}@unit-test.com",
                    password = "world",
                    createdAt = Date()
            )
            userRepository.insert(user).block()
        }

        val p = Pageable()
        StepVerifier.create(userRepository.findPage(p))
                .expectNextMatches {
                    it.totalItems >= 30 && it.items.size == p.size
                }
                .verifyComplete()
    }

    @Test
    fun findAllEmail() {
        for (n in 1..30) {
            val user = User(
                    email = "${UUID.randomUUID()}@unit-test.com",
                    password = "world",
                    createdAt = Date()
            )
            userRepository.insert(user).block()
        }

        val list = userRepository.findAllEmail().collectList().block()
        assertTrue(list.isNotEmpty())
    }

}