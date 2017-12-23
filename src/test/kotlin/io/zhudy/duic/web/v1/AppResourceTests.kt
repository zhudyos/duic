package io.zhudy.duic.web.v1

import io.zhudy.duic.domain.App
import io.zhudy.duic.repository.AppRepository
import io.zhudy.duic.server.Application
import org.joda.time.DateTime
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests
import org.springframework.test.web.reactive.server.WebTestClient
import org.testng.annotations.AfterMethod
import org.testng.annotations.Test
import reactor.core.publisher.Mono

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@MockBean(*[AppRepository::class])
@ActiveProfiles("test")
@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AppResourceTests : AbstractTestNGSpringContextTests() {

    @Autowired
    lateinit var appRepository: AppRepository

    @Autowired
    lateinit var webTestClient: WebTestClient

    val name = "first"
    val profile = "test"
    val firstApp = App(
            id = "123",
            name = name,
            profile = profile,
            description = "test",
            v = 1,
            createdAt = DateTime.now(),
            updatedAt = DateTime.now(),
            content = """environments:
  development:
    name: Development setup
    url: http://dev.bar.com
  production:
    name: Production setup
    url: http://prod.bar.com""",
            users = listOf("kevin", "lucy")
    )

    @AfterMethod
    fun afterMethod() {
        Mockito.reset(appRepository)
    }

    @Test
    fun getConfigState() {
        given(appRepository.findOne(name, profile)).willReturn(Mono.just(firstApp))

        webTestClient.get()
                .uri("/api/v1/apps/states/{name}/{profile}", name, profile)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("state", firstApp.v)
    }

}