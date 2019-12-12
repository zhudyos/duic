package io.zhudy.duic.repository.mongo

import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import io.zhudy.duic.domain.AppHistory
import io.zhudy.duic.domain.AppPair
import io.zhudy.duic.repository.AppRepository
import io.zhudy.duic.repository.BasicTestMongoConfiguration
import io.zhudy.duic.vo.AppVo
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@SpringBootTest(classes = [
    MongoAppRepositoryImpl::class
])
@ActiveProfiles("mongodb")
@ImportAutoConfiguration(classes = [BasicTestMongoConfiguration::class])
internal class MongoAppRepositoryImplTests {

    @Autowired
    private lateinit var reactiveMongoOperations: ReactiveMongoOperations
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
    fun insert() {
        val app = newApp()
        val ap = AppPair(app.name, app.profile)
        val p = appRepository.insert(app).flatMap { n ->
            appDbRow(ap).map { row -> Pair(n, row) }
        }
        val (n, row) = transactionalOperator.transactional(p).block()
        assertThat(n).isEqualTo(1)
        assertThat(row)
                .containsEntry("name", app.name)
                .containsEntry("profile", app.profile)
                .containsEntry("description", app.description)
                .containsEntry("content", app.content)
                .containsEntry("token", app.token)
                .containsEntry("ip_limit", app.ipLimit)
                .containsEntry("users", app.users)
                .containsEntry("v", 1)
        assertThat(row).extracting("created_at").isNotNull()
        assertThat(row).extracting("updated_at").isNotNull()
    }

    @Test
    fun insertHistory() {
        val appHistory = AppHistory(
                name = UUID.randomUUID().toString(),
                profile = "integration-test",
                description = "integration-test",
                content = "integration-test: true",
                v = 1,
                updatedBy = "integration-test@mail.com",
                deletedBy = "integration-test@mail.com",
                users = listOf("integration-test@mail.com"),
                createdAt = LocalDateTime.now()
        )
        val ap = AppPair(appHistory.name, appHistory.profile)
        val p = appRepository.insertHistory(appHistory).flatMap { n ->
            historyDbRow(ap).map { row -> Pair(n, row) }
        }

        val (n, row) = transactionalOperator.transactional(p).block()
        assertThat(n).isEqualTo(1)
        assertThat(row).extracting("_id").isNotNull()
        assertThat(row)
                .containsEntry("name", appHistory.name)
                .containsEntry("profile", appHistory.profile)
                .containsEntry("description", appHistory.description)
                .containsEntry("content", appHistory.content)
                .containsEntry("token", appHistory.token)
                .containsEntry("ip_limit", appHistory.ipLimit)
                .containsEntry("users", appHistory.users)
                .containsEntry("updated_by", appHistory.updatedBy)
                .containsEntry("deleted_by", appHistory.deletedBy)
        assertThat(row).extracting("created_at").isNotNull()
    }

    @Test
    fun delete() {
        val app = newApp()
        val ap = AppPair(app.name, app.profile)
        val p = appRepository.insert(app).then(appRepository.delete(ap)).flatMap { n ->
            appDbRow(ap).hasElement().map { failed -> Pair(n, failed) }
        }
        val (n, failed) = transactionalOperator.transactional(p).block()
        assertThat(n).isEqualTo(1)
        assertThat(failed).isFalse()
    }

    @Test
    fun `delete not exists app`() {
        val ap = AppPair(UUID.randomUUID().toString(), "integration-test@mail.com")
        val p = appRepository.delete(ap)

        val n = transactionalOperator.transactional(p).block()
        assertThat(n).isEqualTo(0)
    }

    @Test
    fun findOne() {
        val app = newApp()
        val p = appRepository.insert(app).then(appRepository.findOne(AppPair(app.name, app.profile)))
        val dbApp = transactionalOperator.transactional(p).block()
        assertThat(dbApp).isEqualToComparingOnlyGivenFields(app, "name", "profile", "description", "content")
    }

    @Test
    fun update() {
        val app = newApp()
        val ap = app.run { AppPair(name, profile) }
        val vo = AppVo.UpdateBasicInfo(
                description = "New integration test",
                token = "new integration token",
                ipLimit = "127.0.0.1",
                v = 1,
                users = listOf("integration-test@mail.com")
        )

        val p = appRepository.insert(app).then(appRepository.update(ap, vo)).flatMap { n ->
            appDbRow(ap).map { row -> Pair(n, row) }
        }

        val (n, row) = transactionalOperator.transactional(p).block()
        assertThat(n).isEqualTo(1)
        assertThat(row)
                .containsEntry("description", vo.description)
                .containsEntry("token", vo.token)
                .containsEntry("ip_limit", vo.ipLimit)
                .containsEntry("v", vo.v + 1)
                .containsEntry("users", vo.users)
    }

    @Test
    fun updateContent() {
        val app = newApp()
        val ap = app.run { AppPair(name, profile) }
        val vo = AppVo.UpdateContent(
                content = "integration: new content",
                v = 1
        )

        val p = appRepository.insert(app).then(appRepository.updateContent(ap, vo)).flatMap { v ->
            appDbRow(ap).map { row ->
                Pair(v, row)
            }
        }

        val (v, row) = transactionalOperator.transactional(p).block()
        assertThat(row)
                .containsEntry("v", v)
                .containsEntry("content", vo.content)
    }

    @Test
    fun findAll() {
        val apps = mutableListOf<AppVo.NewApp>()
        repeat(10) {
            apps.add(newApp())
        }

        val p = Flux.just(*apps.toTypedArray())
                .concatMap(appRepository::insert)
                .thenMany(appRepository.findAll())
                .collectList()

        val list = transactionalOperator.transactional(p).block()
        assertThat(list).hasSizeGreaterThanOrEqualTo(apps.size)
    }

    @Test
    fun search() {
        val q = "game"
        val vo = AppVo.UserQuery(q = q)
        val pageable = PageRequest.of(1, 10)

        val app = newApp("game: League of Legends")

        val p = appRepository.insert(app).then(appRepository.search(vo, pageable))
        val page = transactionalOperator.transactional(p).block()
        assertThat(page.totalElements).isGreaterThanOrEqualTo(1)
        assertThat(page.content).allMatch {
            it.name.contains(q, ignoreCase = true)
                    || it.profile.contains(q, ignoreCase = true)
                    || it.content.contains(q, ignoreCase = true)
        }.containsAll(page.content)
    }

    @Test
    fun find4UpdatedAt() {
        val time = LocalDateTime.now()
        val app = newApp()
        val p = appRepository.insert(app).thenMany(appRepository.find4UpdatedAt(time)).collectList()
        val list = transactionalOperator.transactional(p).block()

        assertThat(list).hasSizeGreaterThanOrEqualTo(1)
        assertThat(list)
                .allMatch { it.updatedAt.isAfter(time) }
                .hasSize(list.size)
    }

    @Test
    fun findAppHistory() {
        // FIXME 补充测试
    }

    @Test
    fun findAllNames() {
        val app1 = AppVo.NewApp(
                name = "first",
                profile = "integration-test",
                description = "integration test",
                token = "",
                ipLimit = "",
                users = emptyList()
        )
        val app2 = AppVo.NewApp(
                name = "second",
                profile = "integration-test",
                description = "integration test",
                token = "",
                ipLimit = "",
                users = emptyList()
        )

        val p = appRepository.insert(app1).then(appRepository.insert(app2))
                .thenMany(appRepository.findAllNames()).collectList()
        val list = transactionalOperator.transactional(p).block()
        assertThat(list).hasSizeGreaterThanOrEqualTo(2)
    }

    @Test
    fun findProfilesByName() {
        val app = newApp()

        val p = appRepository.insert(app).thenMany(appRepository.findProfilesByName(app.name)).collectList()
        val list = transactionalOperator.transactional(p).block()
        assertThat(list).hasSizeGreaterThanOrEqualTo(1)
    }

    private fun appDbRow(ap: AppPair): Mono<Document> = Mono.defer {
        reactiveMongoOperations.execute("app") { coll ->
            coll.find(and(eq("name", ap.name), eq("profile", ap.profile)))
        }.next()
    }

    private fun historyDbRow(ap: AppPair): Mono<Document> = Mono.defer {
        reactiveMongoOperations.execute("app_history") { coll ->
            coll.find(and(eq("name", ap.name), eq("profile", ap.profile)))
        }.next()
    }
}