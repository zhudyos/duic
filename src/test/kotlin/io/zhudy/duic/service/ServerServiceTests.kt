package io.zhudy.duic.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.zhudy.duic.Config
import io.zhudy.duic.config.BasicConfiguration
import io.zhudy.duic.domain.Server
import io.zhudy.duic.repository.ServerRepository
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.QueueDispatcher
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono
import reactor.test.StepVerifier
import reactor.test.expectError
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@SpringBootTest(classes = [
    ServerService::class,
    BasicConfiguration::class,
    WebClientAutoConfiguration::class
])
internal class ServerServiceTests {

    // ====================================== MOCK ============================================//
    @MockkBean
    lateinit var serverRepository: ServerRepository
    // ====================================== MOCK ============================================//

    @Autowired
    lateinit var serverService: ServerService

    @Test
    fun init() {
        every { serverRepository.register(Config.server.host, Config.server.port) } returns Mono.empty()

        serverService.Lifecycle().init()
    }

    @Test
    fun destroy() {
        every { serverRepository.unregister(Config.server.host, Config.server.port) } returns Mono.empty()

        serverService.Lifecycle().destroy()
    }

    @Test
    fun clockPing() {
        every { serverRepository.ping(Config.server.host, Config.server.port) } returns Mono.empty()
        every { serverRepository.clean() } returns Mono.empty()

        serverService.clockPing()
    }

    @Test
    fun loadServerStates() {
        val mockWebServer = MockWebServer()
        mockWebServer.dispatcher = QueueDispatcher().apply {
            enqueueResponse(
                    MockResponse()
                            .setResponseCode(200)
                            .addHeader("content-type", "application/json; charset=utf-8")
                            .setBody("""{"last_data_time":"${System.currentTimeMillis()}"}""")
            )
            enqueueResponse(
                    MockResponse()
                            .setResponseCode(500)
                            .addHeader("content-type", "application/json; charset=utf-8")
            )
        }
        mockWebServer.start()
        val server = Server(
                host = "127.0.0.1",
                port = mockWebServer.port,
                initAt = Date(),
                activeAt = Date()
        )
        val servers = arrayOf(server, server)
        every { serverRepository.findServers() } returns servers.toFlux()

        val rs = serverService.loadServerStates()
        StepVerifier.create(rs)
                .expectNextCount(2)
                .expectComplete()
                .verify()

        mockWebServer.shutdown()
    }

    @Test
    fun info() {
        every { serverRepository.findDbVersion() } returns "5.16".toMono()

        val rs = serverService.info()
        StepVerifier.create(rs)
                .expectNextMatches { it.dbVersion.isNotEmpty() }
                .expectComplete()
                .verify()
    }

    @Test
    fun health() {
        every { serverRepository.findDbVersion() } returns "5.16".toMono()

        val rs = serverService.health()
        StepVerifier.create(rs)
                .expectNextMatches { it.isNotEmpty() }
                .expectComplete()
                .verify()
    }

    @Test
    fun `health check error`() {
        every { serverRepository.findDbVersion() } returns Mono.error(IllegalStateException("check failed"))

        val rs = serverService.health()
        StepVerifier.create(rs)
                .expectError(HealthCheckException::class)
                .verify()
    }
}