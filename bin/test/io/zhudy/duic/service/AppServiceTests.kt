package io.zhudy.duic.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.zhudy.duic.BizCode
import io.zhudy.duic.UserContext
import io.zhudy.duic.config.BasicConfiguration
import io.zhudy.duic.domain.App
import io.zhudy.duic.expectError
import io.zhudy.duic.repository.AppRepository
import io.zhudy.duic.repository.ServerRepository
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import reactor.test.StepVerifier

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@SpringBootTest(classes = [
    AppService::class,
    BasicConfiguration::class,
    WebClientAutoConfiguration::class
])
internal class AppServiceTests {

    // ====================================== MOCK ============================================//
    @MockkBean
    lateinit var appRepository: AppRepository
    @MockkBean
    lateinit var serverRepository: ServerRepository
    // ====================================== MOCK ============================================//

    private val userContext = object : UserContext {
        override val email: String
            get() = "integration-test@weghst.com"
        override val isRoot: Boolean
            get() = true
    }

    @Autowired
    lateinit var appService: AppService

    @Test
    fun watchApps() {
        println("hello")
    }

    @Test
    fun watchDeletedApps() {
    }

    @Test
    fun refresh() {
    }

    @Test
    fun getMemoryLastDataTime() {
    }

    @Test
    fun insert() {
        val app = App(
                name = "junit",
                profile = "test"
        )
        every { appRepository.insert(any()) } returns Mono.empty()

        val rs = appService.insert(app)
        StepVerifier.create(rs)
                .expectComplete()
                .verify()

        assertTrue(app.id.isNotEmpty())
        assertNotNull(app.createdAt)
        assertNotNull(app.updatedAt)
    }

    @Test
    fun delete() {
        val app = App(
                name = "junit",
                profile = "test"
        )
        every { appRepository.delete(app, userContext) } returns Mono.empty()

        val rs = appService.delete(app, userContext)
        StepVerifier.create(rs)
                .expectComplete()
                .verify()
    }

    @Test
    fun `delete by normal user`() {
        val email = "integration-test-x@weghst.com"
        val app = App(
                name = "junit",
                profile = "test",
                users = listOf(email)
        )
        val userContext = object : UserContext {
            override val email: String
                get() = email
            override val isRoot: Boolean
                get() = false
        }
        every { appRepository.findOne(app.name, app.profile) } returns app.toMono()
        every { appRepository.delete(app, userContext) } returns Mono.empty()

        val rs = appService.delete(app, userContext)
        StepVerifier.create(rs)
                .expectComplete()
                .verify()
    }

    @Test
    fun `delete no permission`() {
        val email = "integration-test-x@weghst.com"
        val app = App(
                name = "junit",
                profile = "test"
        )
        val userContext = object : UserContext {
            override val email: String
                get() = email
            override val isRoot: Boolean
                get() = false
        }
        every { appRepository.findOne(app.name, app.profile) } returns app.toMono()
        every { appRepository.delete(app, userContext) } returns Mono.empty()

        val rs = appService.delete(app, userContext)
        StepVerifier.create(rs)
                .expectError(BizCode.Classic.C_403)
                .verify()
    }

    @Test
    fun update() {
    }

    @Test
    fun updateContent() {
    }

    @Test
    fun getConfigState() {
    }

    @Test
    fun watchConfigState() {
    }

    @Test
    fun loadSpringCloudConfig() {
    }

    @Test
    fun loadConfigByNameProfile() {
    }

    @Test
    fun loadConfigByNameProfileKey() {
    }

    @Test
    fun findOne() {
    }

    @Test
    fun findAll() {
    }

    @Test
    fun findByUpdatedAt() {
    }

    @Test
    fun findPage() {
    }

    @Test
    fun findPageByUser() {
    }

    @Test
    fun searchPageByUser() {
    }

    @Test
    fun findLast50History() {
    }

    @Test
    fun findAllNames() {
    }

    @Test
    fun findProfilesByName() {
    }

    @Test
    fun findLastDataTime() {
    }
}