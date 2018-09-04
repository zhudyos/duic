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
package io.zhudy.duic.web.admin

import com.fasterxml.jackson.databind.ObjectMapper
import com.mongodb.MongoWriteException
import com.mongodb.reactivestreams.client.MongoDatabase
import io.zhudy.duic.Config
import io.zhudy.duic.domain.User
import io.zhudy.duic.repository.UserRepository
import io.zhudy.duic.server.Application
import io.zhudy.duic.server.BeansInitializer
import io.zhudy.duic.service.UserService
import org.bson.Document
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@RunWith(SpringRunner::class)
@ActiveProfiles("mongodb", "test")
@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [BeansInitializer::class])
class AdminResourceTests {

    @Autowired
    lateinit var mongoDatabase: MongoDatabase
    @Autowired
    lateinit var userRepository: UserRepository
    @Autowired
    lateinit var userService: UserService
    @Autowired
    lateinit var webTestClient: WebTestClient
    @Autowired
    lateinit var objectMapper: ObjectMapper

    private var _token: String = ""
    val token: String
        get() {
            if (_token.isEmpty()) {
                webTestClient.post()
                        .uri("/api/admins/login")
                        .syncBody(mapOf(
                                "email" to Config.rootEmail,
                                "password" to Config.rootPassword
                        ))
                        .exchange()
                        .expectStatus().isOk
                        .expectBody()
                        .consumeWith {
                            val cookie = it.responseCookies.getFirst("token")!!
                            _token = cookie.value
                        }
            }
            return _token
        }

    @Before
    fun before() {
        userService.insert(User(
                email = Config.rootEmail,
                password = Config.rootPassword
        )).onErrorResume(MongoWriteException::class.java) {
            if (it.error.code == 11000) { // duplicate key
                return@onErrorResume Mono.empty()
            }
            throw it
        }.block()
    }

    @After
    fun after() {
        mongoDatabase.getCollection("app").deleteMany(Document()).toMono().block()
        mongoDatabase.getCollection("app_history").deleteMany(Document()).toMono().block()
        mongoDatabase.getCollection("user").deleteMany(Document()).toMono().block()
    }

    @Test
    fun rootUser() {
        webTestClient.get()
                .uri("/api/admins/user/root")
                .cookie("token", token)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("root", Config.rootEmail)
    }

    @Test
    fun insertUser() {
        val email = UUID.randomUUID().toString() + "@unit-test.com"

        webTestClient.post()
                .uri("/api/admins/users")
                .syncBody(mapOf(
                        "email" to email,
                        "password" to "123456"
                ))
                .cookie("token", token)
                .exchange()
                .expectStatus().isOk
    }

    @Test
    fun deleteUser() {
        val email = UUID.randomUUID().toString() + "@unit-test.com"

        userRepository.insert(User(email = email, password = "123456")).subscribe()

        webTestClient.delete()
                .uri("/api/admins/users/{email}", email)
                .cookie("token", token)
                .exchange()
                .expectStatus().isNoContent
    }

    @Test
    fun deleteUser2002() {
        webTestClient.delete()
                .uri("/api/admins/users/{email}", Config.rootEmail)
                .cookie("token", token)
                .exchange()
                .expectStatus().isForbidden
                .expectBody()
                .jsonPath("code").isEqualTo(2002)
    }

    @Test
    fun updateUserPassword() {
        val email = UUID.randomUUID().toString() + "@unit-test.com"

        webTestClient.post()
                .uri("/api/admins/users")
                .syncBody(mapOf(
                        "email" to email,
                        "password" to "123456"
                ))
                .cookie("token", token)
                .exchange()
                .expectStatus().isOk

        var llToken = ""
        webTestClient.post()
                .uri("/api/admins/login")
                .syncBody(mapOf(
                        "email" to email,
                        "password" to "123456"
                ))
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .consumeWith {
                    val cookie = it.responseCookies.getFirst("token")!!
                    llToken = cookie.value
                }

        webTestClient.put()
                .uri("/api/admins/users/password")
                .syncBody(mapOf(
                        "old_password" to "123456",
                        "new_password" to "456789"
                ))
                .cookie("token", llToken)
                .exchange()
                .expectStatus().isNoContent
    }

    @Test
    fun resetUserPassword() {
        val email = UUID.randomUUID().toString() + "@unit-test.com"

        userRepository.insert(User(email = email, password = "123456")).subscribe()

        webTestClient.patch()
                .uri("/api/admins/users/password")
                .syncBody(mapOf(
                        "email" to email,
                        "password" to "123456"
                ))
                .cookie("token", token)
                .exchange()
                .expectStatus().isNoContent
    }

    @Test
    fun resetUserPassword2003() {
        webTestClient.patch()
                .uri("/api/admins/users/password")
                .syncBody(mapOf(
                        "email" to Config.rootEmail,
                        "password" to "123456"
                ))
                .cookie("token", token)
                .exchange()
                .expectStatus().isForbidden
                .expectBody()
                .jsonPath("code").isEqualTo(2003)
    }

    @Test
    fun findPageUser() {
        webTestClient.get()
                .uri("/api/admins/users?page=1&size=10")
                .cookie("token", token)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("items").isArray
    }

    @Test
    fun findAllEmail() {
        webTestClient.get()
                .uri("/api/admins/users/emails")
                .cookie("token", token)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$").isArray
    }

    @Test
    fun insertApp() {
        val name = UUID.randomUUID().toString()
        val profile = UUID.randomUUID().toString()

        webTestClient.post()
                .uri("/api/admins/apps")
                .cookie("token", token)
                .syncBody(mapOf(
                        "name" to name,
                        "profile" to profile
                ))
                .exchange()
                .expectStatus().isOk
    }

    @Test
    fun insertAppForApp() {
        val name = UUID.randomUUID().toString()
        val profile = UUID.randomUUID().toString()

        webTestClient.post()
                .uri("/api/admins/apps")
                .cookie("token", token)
                .syncBody(mapOf(
                        "name" to name,
                        "profile" to profile
                ))
                .exchange()
                .expectStatus().isOk

        // =================================================================================
        val profile2 = UUID.randomUUID().toString()
        webTestClient.post()
                .uri("/api/admins/apps/duplicates/{name}/{profile}", name, profile)
                .cookie("token", token)
                .syncBody(mapOf(
                        "name" to name,
                        "profile" to profile2,
                        "description" to "insertAppForApp -- test"
                ))
                .exchange()
                .expectStatus().isOk
    }

    @Test
    fun updateApp() {
        val name = UUID.randomUUID().toString()
        val profile = UUID.randomUUID().toString()

        webTestClient.post()
                .uri("/api/admins/apps")
                .cookie("token", token)
                .syncBody(mapOf(
                        "name" to name,
                        "profile" to profile
                ))
                .exchange()
                .expectStatus().isOk

        webTestClient.put()
                .uri("/api/admins/apps")
                .cookie("token", token)
                .syncBody(mapOf(
                        "name" to name,
                        "profile" to profile,
                        "description" to "hello unit-test"
                ))
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("v").isNumber
    }

    @Test
    fun updateAppContent() {
        val name = UUID.randomUUID().toString()
        val profile = UUID.randomUUID().toString()

        webTestClient.post()
                .uri("/api/admins/apps")
                .cookie("token", token)
                .syncBody(mapOf(
                        "name" to name,
                        "profile" to profile
                ))
                .exchange()
                .expectStatus().isOk

        webTestClient.put()
                .uri("/api/admins/apps/contents")
                .cookie("token", token)
                .syncBody(mapOf(
                        "content" to "a: 555"
                ))
                .exchange()
                .expectStatus().isOk
    }

    @Test
    fun deleteApp() {
        val name = UUID.randomUUID().toString()
        val profile = UUID.randomUUID().toString()

        webTestClient.post()
                .uri("/api/admins/apps")
                .cookie("token", token)
                .syncBody(mapOf(
                        "name" to name,
                        "profile" to profile
                ))
                .exchange()
                .expectStatus().isOk

        webTestClient.delete()
                .uri("/api/admins/apps/{name}/{profile}", name, profile)
                .cookie("token", token)
                .exchange()
                .expectStatus().isNoContent
    }

    @Test
    fun findOneApp() {
        val name = UUID.randomUUID().toString()
        val profile = UUID.randomUUID().toString()

        webTestClient.post()
                .uri("/api/admins/apps")
                .cookie("token", token)
                .syncBody(mapOf(
                        "name" to name,
                        "profile" to profile
                ))
                .exchange()
                .expectStatus().isOk

        webTestClient.get()
                .uri("/api/admins/apps/{name}/{profile}", name, profile)
                .cookie("token", token)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("name", name).exists()
                .jsonPath("profile", profile).exists()
    }

    @Test
    fun findAppByUser() {
        webTestClient.get()
                .uri("/api/admins/apps/user?page=1&size=10")
                .cookie("token", token)
                .exchange()
                .expectStatus().isOk
    }

    @Test
    fun searchAppByUser() {
        webTestClient.get()
                .uri("/api/admins/search/apps?page=1&size=10")
                .cookie("token", token)
                .exchange()
                .expectStatus().isOk
    }

    @Test
    fun findAppContentHistory() {
//        val doc = Document()
//        doc["histories"] = listOf(
//                Document(mapOf(
//                        "hid" to Random().nextInt().toString(),
//                        "revised_by" to Config.rootEmail,
//                        "updated_at" to Date()
//                )),
//                Document(mapOf(
//                        "hid" to Random().nextInt().toString(),
//                        "content" to "hello: world",
//                        "revised_by" to Config.rootEmail,
//                        "updated_at" to Date()
//                ))
//        )
//
//        doReturn(Mono.just(doc)).`when`(mongoOperations).findOne(any(), eq(Document::class.java), anyString())

        webTestClient.get()
                .uri("/api/admins/apps/{name}/{profile}/histories", "test", "test")
                .cookie("token", token)
                .exchange()
                .expectStatus().isOk
                .expectBody()
    }
}