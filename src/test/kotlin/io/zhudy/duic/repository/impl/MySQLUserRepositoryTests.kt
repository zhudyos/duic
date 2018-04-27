package io.zhudy.duic.repository.impl

import io.zhudy.duic.domain.Pageable
import io.zhudy.duic.domain.User
import io.zhudy.duic.repository.UserRepository
import io.zhudy.duic.server.Application
import io.zhudy.duic.server.BeansInitializer
import org.junit.After
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests
import org.springframework.transaction.support.TransactionTemplate
import reactor.test.StepVerifier
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@ActiveProfiles("mysql", "test")
@SpringBootTest(classes = [Application::class])
@ContextConfiguration(initializers = [BeansInitializer::class])
class MySQLUserRepositoryTests : AbstractJUnit4SpringContextTests() {

    @Autowired
    lateinit var transactionTemplate: TransactionTemplate
    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate
    @Autowired
    lateinit var userRepository: UserRepository

    @After
    fun after() {
        transactionTemplate.execute {
            jdbcTemplate.execute("delete from `user`")
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
    fun findPage() {
        StepVerifier.create(userRepository.findPage(Pageable())).expectNext()
    }

}