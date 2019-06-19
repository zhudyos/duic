/**
 * Copyright 2017-2019 the original author or authors
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

import io.zhudy.duic.domain.Pageable
import io.zhudy.duic.domain.User
import io.zhudy.duic.repository.UserRepository
import io.zhudy.duic.repository.config.PostgreSQLConfiguration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import reactor.test.StepVerifier
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@SpringBootTest(classes = [DBMSRepositoryApplication::class])
@OverrideAutoConfiguration(enabled = false)
@ActiveProfiles("test", "postgresql")
@ImportAutoConfiguration(classes = [
    DataSourceAutoConfiguration::class,
    DataSourceTransactionManagerAutoConfiguration::class,
    JdbcTemplateAutoConfiguration::class,
    PostgreSQLConfiguration::class,
    LiquibaseAutoConfiguration::class
])
class PostgreSQLUserRepositoryTests {

    @Autowired
    lateinit var transactionManager: PlatformTransactionManager
    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate
    @Autowired
    lateinit var userRepository: UserRepository

    @AfterEach
    fun clean() {
        TransactionTemplate(transactionManager).execute {
            jdbcTemplate.execute("DELETE FROM DUIC_USER")
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