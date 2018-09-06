/**
 * Copyright 2017-2018 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zhudy.duic.web.v1

import io.zhudy.duic.domain.App
import io.zhudy.duic.repository.AppRepository
import io.zhudy.duic.server.Application
import io.zhudy.duic.server.BeansInitializer
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.doReturn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@RunWith(SpringRunner::class)
@ActiveProfiles("mongodb", "test")
@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [BeansInitializer::class])
class AppResourceTests {

    @SpyBean
    lateinit var appRepository: AppRepository

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

    val profile2 = "prod"
    val firstProdApp = App(
            id = "456",
            name = name,
            profile = profile2,
            v = 2,
            createdAt = Date(),
            updatedAt = Date(),
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

    @After
    fun afterMethod() {
        Mockito.reset(appRepository)
    }

    /**
     * 获取配置状态。
     */
    @Test
    fun getConfigState() {
        doReturn(Mono.just(firstTestApp))
                .`when`(appRepository).findOne<Mono<App>>(name, profile)

        webTestClient.get()
                .uri("/api/v1/apps/states/{name}/{profile}", name, profile)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("state", firstTestApp.v)
                .exists()
    }

    /**
     * 获取不存在的配置状态，预期返回错误码 1000。
     */
    @Test
    fun getConfigStateExpectedCode1000() {
        webTestClient.get()
                .uri("/api/v1/apps/states/{name}/{profile}", UUID.randomUUID().toString(), UUID.randomUUID().toString())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .jsonPath("code", 1000)
                .exists()
    }

    /**
     * 获取 spring-cloud-config 的数据配置。
     */
    @Test
    fun getSpringCloudConfig() {
        doReturn(Mono.just(firstTestApp))
                .`when`(appRepository).findOne<Mono<App>>(name, profile)

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

    /**
     * 根据应用名称与配置名称返回数据。
     */
    @Test
    fun getConfigByNameProfile() {
        doReturn(Mono.just(firstTestApp))
                .`when`(appRepository).findOne<Mono<App>>(name, profile)

        webTestClient.get()
                .uri("/api/v1/apps/{name}/{profile}", name, profile)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody()
                .jsonPath("environments.production.url", "http://prod.bar.com")
                .exists()
    }

    /**
     * 根据应用名称、配置名称与配置键返回数据。
     */
    @Test
    fun getConfigByNameProfileKey() {
        doReturn(Mono.just(firstTestApp))
                .`when`(appRepository).findOne<Mono<App>>(name, profile)

        webTestClient.get()
                .uri("/api/v1/apps/{name}/{profile}/{key}", name, profile, "environments.production")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody()
                .jsonPath("url", "http://prod.bar.com")
                .exists()
    }

    /**
     * 根据应用名称与配置名称返回数据，多个 profile 合并。
     */
    @Test
    fun getConfigByNameProfileMulti() {
        doReturn(Mono.just(firstTestApp))
                .`when`(appRepository).findOne<Mono<App>>(name, profile)
        doReturn(Mono.just(firstProdApp))
                .`when`(appRepository).findOne<Mono<App>>(name, profile2)

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