package io.zhudy.duic.repository.mysql

import io.zhudy.duic.repository.BasicTestRelationalConfiguration
import io.zhudy.duic.repository.ServerRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.reactive.TransactionalOperator
import java.time.LocalDate
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@SpringBootTest(classes = [
    MySqlServerRepositoryImpl::class
])
@ActiveProfiles("mysql")
@ImportAutoConfiguration(classes = [BasicTestRelationalConfiguration::class])
internal class MySqlServerRepositoryImplTests {

    @Autowired
    private lateinit var databaseClient: DatabaseClient
    @Autowired
    private lateinit var serverRepository: ServerRepository
    @Autowired
    private lateinit var transactionalOperator: TransactionalOperator

    @Test
    fun register() {
        val host = "integration-test"
        val port = 7777
        val p = serverRepository.register(host, port)
        val n = transactionalOperator.transactional(p).block()
        assertThat(n).isEqualTo(1)
    }

    @Test
    fun `register duplicate`() {
        val host = "integration-test"
        val port = 7777
        val p = serverRepository.register(host, port).repeat(1)
        val n = transactionalOperator.transactional(p).blockLast()
        assertThat(n).isEqualTo(1)
    }

    @Test
    fun unregister() {
        val host = "integration-test"
        val port = 7777
        val p = serverRepository.register(host, port)
                .then(serverRepository.unregister(host, port))

        val n = transactionalOperator.transactional(p).block()
        assertThat(n).isEqualTo(1)
    }

    @Test
    fun `unregister not found`() {
        val host = "integration-test"
        val port = 7777

        val p = serverRepository.unregister(host, port)
        val n = transactionalOperator.transactional(p).block()
        assertThat(n).isEqualTo(0)
    }

    @Test
    fun ping() {
        val host = "integration-test"
        val port = 7777

        val insertSql = "INSERT INTO DUIC_SERVER(id,host,port,init_at,active_at) VALUES(:id,:host,:port,:initAt,:activeAt)"
        val prepare = databaseClient.execute(insertSql)
                .bind("id", UUID.randomUUID().toString())
                .bind("host", host)
                .bind("port", port)
                .bind("initAt", LocalDate.of(2000, 2, 2))
                .bind("activeAt", LocalDate.of(2000, 2, 2))
                .fetch()
                .rowsUpdated()
                .thenMany(
                        databaseClient.execute(insertSql)
                                .bind("id", UUID.randomUUID().toString())
                                .bind("host", host)
                                .bind("port", port)
                                .bind("initAt", LocalDate.of(2000, 2, 2))
                                .bind("activeAt", LocalDate.of(2000, 2, 2))
                                .fetch()
                                .rowsUpdated()
                )

        val p = prepare.then(serverRepository.clean())
        val n = transactionalOperator.transactional(p).block()
        assertThat(n).isGreaterThanOrEqualTo(2)
    }

    @Test
    fun findDbVersion() {
        val version = serverRepository.findDbVersion().block()
        assertThat(version).isNotEmpty()
    }

}