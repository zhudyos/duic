package io.zhudy.duic.repository.mongo

import io.zhudy.duic.repository.BasicTestMongoConfiguration
import io.zhudy.duic.repository.ServerRepository
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.ReactiveCollectionCallback
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.reactive.TransactionalOperator
import java.time.LocalDate

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@SpringBootTest(classes = [
    MongoServerRepositoryImpl::class
])
@ActiveProfiles("mongodb")
@ImportAutoConfiguration(classes = [BasicTestMongoConfiguration::class])
internal class MongoServerRepositoryImplTests {

    @Autowired
    private lateinit var reactiveMongoOperator: ReactiveMongoOperations
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

        val action = ReactiveCollectionCallback { coll ->
            val o = mapOf(
                    "host" to host,
                    "port" to port,
                    "init_at" to LocalDate.of(2000, 2, 2),
                    "active_at" to LocalDate.of(2000, 2, 2)
            )
            coll.insertMany(listOf(
                    Document(o),
                    Document(o)
            ))
        }

        val prepare = reactiveMongoOperator.execute("server", action)
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