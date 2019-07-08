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

import io.zhudy.duic.repository.ServerRepository
import io.zhudy.duic.repository.config.MySQLConfiguration
import org.junit.jupiter.api.AfterEach
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
class MySQLServerRepositoryTests {

    @Autowired
    lateinit var transactionManager: PlatformTransactionManager
    @Autowired
    lateinit var jdbcTemplate: NamedParameterJdbcTemplate
    @Autowired
    lateinit var serverRepository: ServerRepository

    @AfterEach
    fun clean() {
        TransactionTemplate(transactionManager).execute {
            jdbcTemplate.update("DELETE FROM DUIC_SERVER", EmptySqlParameterSource.INSTANCE)
        }
    }

    @Test
    fun register() {
        val rs = serverRepository.register("localhost", 1234)
        StepVerifier.create(rs)
                .expectComplete()
                .verify()
    }

    @Test
    fun registerOnExists() {
        val rs1 = serverRepository.register("localhost", 1234)
        val rs2 = serverRepository.register("localhost", 1234)
        StepVerifier.create(rs1.concatWith(rs2))
                .expectComplete()
                .verify()
    }

    @Test
    fun unregister() {
        val rs1 = serverRepository.register("localhost", 1234)
        val rs2 = serverRepository.unregister("localhost", 1234)
        StepVerifier.create(rs1.then(rs2))
                .expectComplete()
                .verify()
    }

    @Test
    fun ping() {
        val rs1 = serverRepository.register("localhost", 1234)
        val rs2 = serverRepository.ping("localhost", 1234)
        StepVerifier.create(rs1.then(rs2))
                .expectComplete()
                .verify()
    }

    @Test
    fun findServers() {
        val rs1 = serverRepository.register("localhost", 1234)
        val rs2 = serverRepository.findServers()
        StepVerifier.create(rs1.thenMany(rs2))
                .expectNextCount(1)
                .expectComplete()
                .verify()
    }

    @Test
    fun findDbVersion() {
        val rs = serverRepository.findDbVersion()
        StepVerifier.create(rs)
                .expectNextMatches { it.isNotEmpty() }
                .expectComplete()
                .verify()
    }

}