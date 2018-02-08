package io.zhudy.duic.web.admin

import com.fasterxml.jackson.databind.ObjectMapper
import io.zhudy.duic.Config
import io.zhudy.duic.server.Application
import org.bson.Document
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito
import org.mockito.Mockito.doReturn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests
import org.springframework.test.web.reactive.server.WebTestClient
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import reactor.core.publisher.Mono
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@ActiveProfiles("test")
@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners(MockitoTestExecutionListener::class)
class AdminResourceTests : AbstractTestNGSpringContextTests(){

    @SpyBean
    lateinit var mongoOperations: ReactiveMongoOperations

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var objectMapper: ObjectMapper

    var token: String = ""

    @BeforeMethod
    fun beforeLogin() {
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
                    token = rs["token"] as String
                }
    }

    @AfterMethod
    fun afterMethod() {
        Mockito.reset(mongoOperations)
    }

    @Test
    fun findAppContentHistory() {
        val doc = Document()
        doc["histories"] = listOf(
                Document(mapOf(
                        "hid" to Random().nextInt().toString(),
                        "revised_by" to Config.rootEmail,
                        "updated_at" to Date()
                )),
                Document(mapOf(
                        "hid" to Random().nextInt().toString(),
                        "content" to "hello: world",
                        "revised_by" to Config.rootEmail,
                        "updated_at" to Date()
                ))
        )

        doReturn(Mono.just(doc)).`when`(mongoOperations).findOne(any(), eq(Document::class.java), anyString())

        webTestClient.get()
                .uri("/api/admins/apps/{name}/{profile}/histories", "test", "test")
                .cookie("token", token)
                .exchange()
                .expectStatus().isOk
                .expectBody()
    }

}