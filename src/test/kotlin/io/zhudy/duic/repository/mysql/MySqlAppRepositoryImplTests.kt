package io.zhudy.duic.repository.mysql

import io.zhudy.duic.domain.AppPair
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
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@SpringBootTest(classes = [
    MySqlAppRepositoryImpl::class
])
@ActiveProfiles("mysql")
@ImportAutoConfiguration(classes = [BasicTestRelationalConfiguration::class])
internal class MySqlAppRepositoryImplTests {

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
    fun findOne() {
        val app = newApp()
        val dbApp = appRepository.insert(app)
                .then(appRepository.findOne(AppPair(app.name, app.profile)))
                .`as`(transactionalOperator::transactional)
                .block()
        assertThat(dbApp)
                .isEqualToComparingOnlyGivenFields(app, "name", "profile", "description", "content")
    }

    @Test
    fun search() {
        val q = "game"
        val vo = AppVo.UserQuery(q = q)
        val pageable = PageRequest.of(0, 10)

        val app = newApp("game: League of Legends")

        val page = appRepository.insert(app)
                .then(appRepository.search(vo, pageable))
                .flatMap { rs ->
                    // 手动清理数据
                    // MySQL FULLTEXT 只能搜索到提交的数据
                    appRepository.delete(AppPair(name = app.name, profile = app.profile)).map { rs }
                }
                .block()
        assertThat(page.totalElements).isGreaterThanOrEqualTo(1)
        assertThat(page.content).isNotEmpty
    }

}