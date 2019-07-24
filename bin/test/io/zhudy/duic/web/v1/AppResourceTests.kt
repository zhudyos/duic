package io.zhudy.duic.web.v1

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.zhudy.duic.BizCodeException
import io.zhudy.duic.BizCodes
import io.zhudy.duic.domain.App
import io.zhudy.duic.service.AppService
import io.zhudy.duic.web.AbstractResourceTestBase
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@SpringBootTest(classes = [AppResource::class])
internal class AppResourceTests : AbstractResourceTestBase() {

    @MockkBean
    lateinit var appService: AppService

    val name = "first"
    val profile = "test"
    val app = App(
            id = "123",
            name = name,
            profile = profile,
            description = profile,
            v = 1,
            createdAt = Date(),
            updatedAt = Date(),
            content = """environments:
  development:
    name: Development setup
    url: http://dev.bar.com
  production:
    name: Production setup
    url: http://prod.bar.com""",
            users = listOf("kevin", "lucy")
    )

    @Test
    fun getConfigState() {
        val state = "123"
        every { appService.getConfigState(any()) } returns state.toMono()

        webTestClient.get()
                .uri("/api/v1/apps/states/{name}/{profile}", name, profile)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("state", state)
                .exists()
    }

    @Test
    fun `getConfigState not found config`() {
        every { appService.getConfigState(any()) } returns Mono.error(BizCodeException(BizCodes.C_1000))

        webTestClient.get()
                .uri("/api/v1/apps/states/{name}/{profile}", name, profile)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .jsonPath("code", 1000)
                .exists()
    }

    @Test
    fun watchConfigState() {
    }

    @Test
    fun getSpringCloudConfig() {
    }

    @Test
    fun getConfigByNameProfile() {
    }

    @Test
    fun getConfigByNameProfileKey() {
    }
}