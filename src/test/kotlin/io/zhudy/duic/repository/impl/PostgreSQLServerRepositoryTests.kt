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
import io.zhudy.duic.repository.config.PostgreSQLConfiguration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
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
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@SpringBootTest(classes = [PostgreSQLConfiguration::class])
@OverrideAutoConfiguration(enabled = false)
@ActiveProfiles("test", "postgresql")
@ImportAutoConfiguration(classes = [
    DataSourceAutoConfiguration::class,
    DataSourceTransactionManagerAutoConfiguration::class,
    JdbcTemplateAutoConfiguration::class,
    LiquibaseAutoConfiguration::class
])
class PostgreSQLServerRepositoryTests {

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
        val n = serverRepository.register("localhost", 1234).block()
        assertEquals(1, n)
    }

    @Test
    fun registerOnExists() {
        serverRepository.register("localhost", 1234).block()
        serverRepository.register("localhost", 1234).block()
    }

    @Test
    fun unregister() {
        serverRepository.register("localhost", 1234).block()
        val n = serverRepository.unregister("localhost", 1234).block()
        assertEquals(1, n)
    }

    @Test
    fun ping() {
        serverRepository.register("localhost", 1234).block()
        val n = serverRepository.ping("localhost", 1234).block()
        assertEquals(1, n)
    }

    @Test
    fun findServers() {
        serverRepository.register("localhost", 1234).block()
        val servers = serverRepository.findServers().collectList().block()
        assertTrue(servers.isNotEmpty())
    }

    @Test
    fun findDbVersion() {
        val version = serverRepository.findDbVersion().block()
        assertTrue(version.isNotEmpty())
    }
}