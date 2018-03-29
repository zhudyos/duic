package io.zhudy.duic.web.v1

import io.zhudy.duic.domain.App
import io.zhudy.duic.server.Application
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.doAnswer
import org.mockito.Mockito
import org.mockito.Mockito.doReturn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Query
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests
import org.springframework.test.web.reactive.server.WebTestClient
import org.testng.annotations.AfterMethod
import org.testng.annotations.Test
import reactor.core.publisher.Mono

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@ActiveProfiles("test")
@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners(MockitoTestExecutionListener::class)
class AppResourceTests : AbstractTestNGSpringContextTests() {

    @SpyBean
    lateinit var mongoOperations: ReactiveMongoOperations

    @Autowired
    lateinit var webTestClient: WebTestClient

    val name = "first"
    val profile = "test"
    val firstTestApp = App(
            id = "123",
            name = name,
            profile = profile,
            description = profile,
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

    val profile2 = "prod"
    val firstProdApp = App(
            id = "456",
            name = name,
            profile = profile2,
            v = 2,
            createdAt = DateTime.now(),
            updatedAt = DateTime.now(),
            content = """environments:
  development:
    name: Development setup2
    url: http://dev2.bar.com
  production:
    name: Production setup2
    url: http://prod2.bar.com
array:
  - a
  - b
  - c""",
            users = listOf("kevin", "lucy")
    )

    @AfterMethod
    fun afterMethod() {
        Mockito.reset(mongoOperations)
    }

    @Test(description = "获取配置状态")
    fun getConfigState() {
        doReturn(Mono.just(firstTestApp))
                .`when`(mongoOperations).findOne<Mono<App>>(any(), any())

        webTestClient.get()
                .uri("/api/v1/apps/states/{name}/{profile}", name, profile)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("state", firstTestApp.v)
                .exists()
    }

    @Test(description = "获取 spring-cloud-config 的数据配置")
    fun getSpringCloudConfig() {
        doReturn(Mono.just(firstTestApp))
                .`when`(mongoOperations).findOne<Mono<App>>(any(), any())

        webTestClient.get()
                .uri("/api/v1/ssc/{name}/{profile}", name, profile)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody()
                .jsonPath("state", firstTestApp.v)
                .exists()
                .jsonPath("$.propertySources[0].source")
                .isMap
    }

    @Test(description = "根据应用名称与配置名称返回数据")
    fun getConfigByNameProfile() {
        doReturn(Mono.just(firstTestApp))
                .`when`(mongoOperations).findOne<Mono<App>>(any(), any())

        webTestClient.get()
                .uri("/api/v1/apps/{name}/{profile}", name, profile)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody()
                .jsonPath("environments.production.url", "http://prod.bar.com")
                .exists()
    }

    @Test(description = "根据应用名称、配置名称与配置键返回数据")
    fun getConfigByNameProfileKey() {
        doReturn(Mono.just(firstTestApp))
                .`when`(mongoOperations).findOne<Mono<App>>(any(), any())

        webTestClient.get()
                .uri("/api/v1/apps/{name}/{profile}/{key}", name, profile, "environments.production")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody()
                .jsonPath("url", "http://prod.bar.com")
                .exists()
    }

    @Test(description = "根据应用名称与配置名称返回数据，多个 profile 合并")
    fun getConfigByNameProfileMulti() {
        doAnswer {
            val q = (it.arguments[0] as Query).queryObject
            if (q["profile"] == profile) {
                return@doAnswer Mono.just(firstTestApp)
            } else if (q["profile"] == profile2) {
                return@doAnswer Mono.just(firstProdApp)
            }

            Mono.empty<App>()
        }.`when`(mongoOperations).findOne<Mono<App>>(any(), any())

        webTestClient.get()
                .uri("/api/v1/apps/{name}/{profile},{profile2}", name, profile, profile2)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody()
                .jsonPath("environments.production.url", "http://prod2.bar.com")
                .exists()
                .jsonPath("array")
                .isArray
    }
}