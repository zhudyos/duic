package io.zhudy.duic.web.admin

import com.fasterxml.jackson.databind.ObjectMapper
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.and
import com.mongodb.reactivestreams.client.MongoDatabase
import io.zhudy.duic.Config
import io.zhudy.duic.domain.User
import io.zhudy.duic.repository.UserRepository
import io.zhudy.duic.server.Application
import io.zhudy.duic.server.BeansInitializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests
import org.springframework.test.web.reactive.server.WebTestClient
import org.testng.annotations.Test
import reactor.core.publisher.toMono
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@ActiveProfiles("test")
@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [BeansInitializer::class])
@TestExecutionListeners(MockitoTestExecutionListener::class)
class AdminResourceTests : AbstractTestNGSpringContextTests() {

    @Autowired
    lateinit var mongoDatabase: MongoDatabase
    @Autowired
    lateinit var userRepository: UserRepository

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
                            val rs = objectMapper.readValue(it.responseBody, Map::class.java)
                            _token = rs["token"] as String
                        }
            }
            return _token
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
        val email = UUID.randomUUID().toString() + "@testng.com"

        webTestClient.post()
                .uri("/api/admins/users")
                .syncBody(mapOf(
                        "email" to email,
                        "password" to "123456"
                ))
                .cookie("token", token)
                .exchange()
                .expectStatus().isOk

        // clean
        userRepository.delete(email).subscribe()
    }

    @Test
    fun deleteUser() {
        val email = UUID.randomUUID().toString() + "@testng.com"

        userRepository.insert(User(email = email, password = "123456")).subscribe()

        webTestClient.delete()
                .uri("/api/admins/users/{email}", email)
                .cookie("token", token)
                .exchange()
                .expectStatus().isNoContent
    }

    @Test
    fun updateUserPassword() {
        val email = UUID.randomUUID().toString() + "@testng.com"

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
                    val rs = objectMapper.readValue(it.responseBody, Map::class.java)
                    llToken = rs["token"] as String
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

        // clean
        userRepository.delete(email).subscribe()
    }

    @Test
    fun resetUserPassword() {
        val email = UUID.randomUUID().toString() + "@testng.com"

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

        // clean
        userRepository.delete(email).subscribe()
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

        // clean
        mongoDatabase.getCollection("app").deleteMany(and(Filters.eq("name", name), Filters.eq("profile", profile)))
                .toMono()
                .subscribe()
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

        // clean
        mongoDatabase.getCollection("app").deleteMany(Filters.eq("name", name))
                .toMono()
                .subscribe()
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
                        "description" to "hello testng"
                ))
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("v").isNumber

        // clean
        mongoDatabase.getCollection("app").deleteMany(and(Filters.eq("name", name), Filters.eq("profile", profile)))
                .toMono()
                .subscribe()
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

        // clean
        mongoDatabase.getCollection("app").deleteMany(and(Filters.eq("name", name), Filters.eq("profile", profile)))
                .toMono()
                .subscribe()
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

        // clean
        mongoDatabase.getCollection("app_history").deleteMany(and(Filters.eq("name", name), Filters.eq("profile", profile)))
                .toMono()
                .subscribe()
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

        // clean
        mongoDatabase.getCollection("app").deleteMany(and(Filters.eq("name", name), Filters.eq("profile", profile)))
                .toMono()
                .subscribe()
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