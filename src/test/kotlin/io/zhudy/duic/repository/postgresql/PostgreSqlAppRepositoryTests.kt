package io.zhudy.duic.repository.postgresql

import io.zhudy.duic.repository.AppRepository
import io.zhudy.duic.repository.BasicTestRelationalConfiguration
import io.zhudy.duic.vo.AppVo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.test.StepVerifier
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@SpringBootTest(classes = [
    PostgreSqlAppRepository::class
])
@ActiveProfiles("postgresql")
@ImportAutoConfiguration(classes = [BasicTestRelationalConfiguration::class])
internal class PostgreSqlAppRepositoryTests {

    @Autowired
    private lateinit var databaseClient: DatabaseClient
    @Autowired
    private lateinit var appRepository: AppRepository
    @Autowired
    private lateinit var transactionalOperator: TransactionalOperator

    private fun newApp(content: String? = null) = AppVo.NewApp(
            name = UUID.randomUUID().toString(),
            profile = "integration-test",
            description = "integration test",
            content = content ?: "integration-test: true",
            token = "",
            ipLimit = "",
            users = listOf("integration-test@mail.com")
    )

    @Test
    fun nextGv() {
        val p = appRepository.nextGv()
        StepVerifier.create(p)
                .expectNextMatches { it >= 1 }
                .expectComplete()
                .verify()
    }

    @Test
    fun search() {
        val q = "game"
        val vo = AppVo.UserQuery(q = q)
        val pageable = PageRequest.of(0, 10)

        val app = newApp("game: League of Legends")

        val page = appRepository.insert(app).then(appRepository.search(vo, pageable))
                .`as`(transactionalOperator::transactional)
                .block()
        assertThat(page.totalElements).isGreaterThanOrEqualTo(1)
        assertThat(page.content).isNotEmpty
    }

}