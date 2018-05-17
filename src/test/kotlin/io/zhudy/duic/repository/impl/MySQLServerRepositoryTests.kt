package io.zhudy.duic.repository.impl

import io.zhudy.duic.repository.ServerRepository
import io.zhudy.duic.server.Application
import io.zhudy.duic.server.BeansInitializer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests
import org.springframework.transaction.support.TransactionTemplate
import reactor.test.StepVerifier

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@ActiveProfiles("mysql", "test")
@SpringBootTest(classes = [Application::class])
@ContextConfiguration(initializers = [BeansInitializer::class])
class MySQLServerRepositoryTests : AbstractJUnit4SpringContextTests() {

    @Autowired
    lateinit var transactionTemplate: TransactionTemplate
    @Autowired
    lateinit var jdbcTemplate: NamedParameterJdbcTemplate
    @Autowired
    lateinit var serverRepository: ServerRepository

    @After
    fun after() {
        transactionTemplate.execute {
            jdbcTemplate.update("delete from server", EmptySqlParameterSource.INSTANCE)
        }
    }

    @Test
    fun register() {
        StepVerifier.create(serverRepository.register("localhost", 1234))
                .expectNext(1)
                .verifyComplete()
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
        val servers = serverRepository.findServers().buffer().blockFirst()
        assertTrue(servers.isNotEmpty())
    }
}