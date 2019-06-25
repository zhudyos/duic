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

import io.zhudy.duic.UserContext
import io.zhudy.duic.domain.App
import io.zhudy.duic.domain.Pageable
import io.zhudy.duic.repository.AppRepository
import io.zhudy.duic.repository.config.MySQLConfiguration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import reactor.test.StepVerifier
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@SpringBootTest(classes = [MySQLConfiguration::class])
@OverrideAutoConfiguration(enabled = false)
@ActiveProfiles("test", "mysql")
@ImportAutoConfiguration(classes = [
    DataSourceAutoConfiguration::class,
    DataSourceTransactionManagerAutoConfiguration::class,
    JdbcTemplateAutoConfiguration::class,
    LiquibaseAutoConfiguration::class
])
class MySQLAppRepositoryTests {

    @Autowired
    lateinit var transactionManager: PlatformTransactionManager
    @Autowired
    lateinit var jdbcTemplate: NamedParameterJdbcTemplate
    @Autowired
    lateinit var appRepository: AppRepository

    val normalUserContext = object : UserContext {
        override val email: String
            get() = "a@unit-test.com"
        override val isRoot: Boolean
            get() = false
    }

    @AfterEach
    fun clean() {
        TransactionTemplate(transactionManager).execute {
            jdbcTemplate.update("DELETE FROM DUIC_APP", EmptySqlParameterSource.INSTANCE)
            jdbcTemplate.update("DELETE FROM DUIC_APP_HISTORY", EmptySqlParameterSource.INSTANCE)
        }
    }

    @Test
    fun insert() {
        val app = App(
                name = UUID.randomUUID().toString(),
                profile = UUID.randomUUID().toString(),
                description = "unit test",
                token = "[TOKEN]",
                users = listOf("a@unit-test.com", "b@unit-test.com")
        )
        StepVerifier.create(appRepository.insert(app))
                .expectNext(1)
                .verifyComplete()
    }

    @Test
    fun delete() {
        val app = App(
                name = UUID.randomUUID().toString(),
                profile = UUID.randomUUID().toString(),
                description = "unit test",
                token = "[TOKEN]",
                users = listOf("a@unit-test.com", "b@unit-test.com")
        )
        appRepository.insert(app).block()

        StepVerifier.create(appRepository.delete(app, normalUserContext))
                .expectNext(1)
                .verifyComplete()
    }

    @Test
    fun findOne() {
        val app = App(
                name = UUID.randomUUID().toString(),
                profile = UUID.randomUUID().toString(),
                description = "unit test",
                token = "[TOKEN]",
                users = listOf("a@unit-test.com", "b@unit-test.com")
        )
        appRepository.insert(app).block()

        val dbApp = appRepository.findOne<App>(app.name, app.profile).block()
        assertEquals(app.name, dbApp.name)
        assertEquals(app.profile, dbApp.profile)
        assertEquals(app.description, dbApp.description)
        assertEquals(app.token, dbApp.token)
        assertEquals(app.ipLimit, dbApp.ipLimit)
        assertEquals(app.users.joinToString(","), dbApp.users.joinToString(","))
    }

    @Test
    fun update() {
        val app = App(
                name = UUID.randomUUID().toString(),
                profile = UUID.randomUUID().toString(),
                description = "unit test",
                token = "[TOKEN]",
                users = listOf("a@unit-test.com", "b@unit-test.com")
        )
        appRepository.insert(app).block()

        StepVerifier.create(appRepository.update(app, normalUserContext))
                .expectNext(app.v)
                .verifyComplete()
    }

    @Test
    fun updateContent() {
        val app = App(
                name = UUID.randomUUID().toString(),
                profile = UUID.randomUUID().toString(),
                description = "unit test",
                token = "[TOKEN]",
                users = listOf("a@unit-test.com", "b@unit-test.com")
        )
        appRepository.insert(app).block()

        app.content = "a: a"
        val dbApp = appRepository.updateContent(app, normalUserContext).block()
        assertEquals(app.v, dbApp.v)
    }

    @Test
    fun findAll() {
        for (n in 1..30) {
            val app = App(
                    name = UUID.randomUUID().toString(),
                    profile = UUID.randomUUID().toString(),
                    description = "unit test",
                    token = "[TOKEN]",
                    users = listOf("a@unit-test.com", "b@unit-test.com")
            )
            appRepository.insert(app).block()
        }

        val all = appRepository.findAll().collectList().block()
        assertTrue(all.isNotEmpty())
    }

    @Test
    fun findPage() {
        for (n in 1..30) {
            val app = App(
                    name = UUID.randomUUID().toString(),
                    profile = UUID.randomUUID().toString(),
                    description = "unit test",
                    token = "[TOKEN]",
                    users = listOf("a@unit-test.com", "b@unit-test.com")
            )
            appRepository.insert(app).block()
        }

        val p = Pageable()
        val list = appRepository.findPage(p).block()
        assertTrue(list.items.isNotEmpty())
    }

    @Test
    fun findPageByUser() {
        for (n in 1..30) {
            val app = App(
                    name = UUID.randomUUID().toString(),
                    profile = UUID.randomUUID().toString(),
                    description = "unit test",
                    token = "[TOKEN]",
                    users = listOf("a@unit-test.com", "b@unit-test.com")
            )
            appRepository.insert(app).block()
        }

        val p = Pageable()
        val list = appRepository.findPageByUser(p, normalUserContext).block()
        assertTrue(list.items.isNotEmpty())
    }

    @Test
    fun searchPage() {
        for (n in 1..30) {
            val app = App(
                    name = UUID.randomUUID().toString(),
                    profile = UUID.randomUUID().toString(),
                    description = "unit test",
                    token = "[TOKEN]",
                    users = listOf("a@unit-test.com", "b@unit-test.com")
            )
            appRepository.insert(app).block()
        }

        val p = Pageable()
        StepVerifier.create(appRepository.searchPage("", p))
                .expectNextMatches {
                    it.totalItems >= 30 && it.items.size == p.size
                }
                .verifyComplete()

        StepVerifier.create(appRepository.searchPage("测试", p))
                .expectNextMatches {
                    it.totalItems == 0 && it.items.isEmpty()
                }
                .verifyComplete()
    }

    @Test
    fun searchPageByUser() {
        for (n in 1..30) {
            val app = App(
                    name = UUID.randomUUID().toString(),
                    profile = UUID.randomUUID().toString(),
                    description = "unit test",
                    token = "[TOKEN]",
                    users = listOf("a@unit-test.com", "b@unit-test.com")
            )
            appRepository.insert(app).block()
        }

        val p = Pageable()
        StepVerifier.create(appRepository.searchPageByUser("", p, normalUserContext))
                .expectNextMatches {
                    it.totalItems >= 30 && it.items.size == p.size
                }
                .verifyComplete()

        StepVerifier.create(appRepository.searchPageByUser("测试", p, normalUserContext))
                .expectNextMatches {
                    it.totalItems == 0 && it.items.isEmpty()
                }
                .verifyComplete()
    }

    @Test
    fun findByUpdatedAt() {
        for (n in 1..30) {
            val app = App(
                    name = UUID.randomUUID().toString(),
                    profile = UUID.randomUUID().toString(),
                    description = "unit test",
                    token = "[TOKEN]",
                    users = listOf("a@unit-test.com", "b@unit-test.com")
            )
            appRepository.insert(app).block()
        }

        val updatedAt = Date.from(LocalDate.parse("2018-01-01").atStartOfDay(ZoneId.systemDefault()).toInstant())
        val list = appRepository.findByUpdatedAt(updatedAt).collectList().block()

        assertTrue(list.size >= 30)

        var prevUpdatedAt: Instant? = null
        list.forEach {
            if (prevUpdatedAt != null) {
                assertFalse(it.updatedAt!!.toInstant().isBefore(prevUpdatedAt))
            }
            prevUpdatedAt = it.updatedAt?.toInstant()
        }
    }

    @Test
    fun findLast50History() {
        val app = App(
                name = UUID.randomUUID().toString(),
                profile = UUID.randomUUID().toString(),
                description = "unit test",
                token = "[TOKEN]",
                users = listOf("a@unit-test.com", "b@unit-test.com")
        )
        appRepository.insert(app).block()

        for (n in 1..100) {
            app.description = "unit test[$n]"
            appRepository.update(app, normalUserContext).block()
        }

        val list = appRepository.findLast50History(app.name, app.profile).collectList().block()
        assertEquals(50, list.size)
    }

    @Test
    fun findAllNames() {
        for (n in 1..30) {
            val app = App(
                    name = UUID.randomUUID().toString(),
                    profile = UUID.randomUUID().toString(),
                    description = "unit test",
                    token = "[TOKEN]",
                    users = listOf("a@unit-test.com", "b@unit-test.com")
            )
            appRepository.insert(app).block()
        }

        val list = appRepository.findAllNames().collectList().block()
        assertTrue(list.isNotEmpty())
    }

    @Test
    fun findProfilesByName() {
        val name = UUID.randomUUID().toString()
        for (n in 1..30) {
            val app = App(
                    name = name,
                    profile = UUID.randomUUID().toString(),
                    description = "unit test",
                    token = "[TOKEN]",
                    users = listOf("a@unit-test.com", "b@unit-test.com")
            )
            appRepository.insert(app).block()
        }

        val list = appRepository.findProfilesByName(name).collectList().block()
        assertEquals(30, list.size)
    }

    @Test
    fun findDeletedByCreatedAt() {
        val apps = mutableListOf<App>()
        for (n in 1..30) {
            val app = App(
                    name = UUID.randomUUID().toString(),
                    profile = UUID.randomUUID().toString(),
                    description = "unit test",
                    token = "[TOKEN]",
                    users = listOf("a@unit-test.com", "b@unit-test.com")
            )

            appRepository.insert(app).block()
            apps.add(app)
        }

        apps.forEach {
            appRepository.delete(it, normalUserContext).block()
        }

        val createdAt = Date.from(LocalDate.parse("2018-01-01").atStartOfDay(ZoneId.systemDefault()).toInstant())
        val list = appRepository.findDeletedByCreatedAt(createdAt).collectList().block()
        assertTrue(list.isNotEmpty())

        var prevCreatedAt: Instant? = null
        list.forEach {
            if (prevCreatedAt != null) {
                assertFalse(it.createdAt!!.toInstant().isBefore(prevCreatedAt))
            }
            prevCreatedAt = it.createdAt?.toInstant()
        }
    }

}