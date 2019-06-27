package io.zhudy.duic.service

import io.zhudy.duic.Config
import io.zhudy.duic.config.BasicConfiguration
import io.zhudy.duic.domain.Server
import io.zhudy.duic.repository.ServerRepository
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.QueueDispatcher
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono
import reactor.test.StepVerifier
import reactor.test.expectError
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@SpringBootTest(classes = [ServerService::class])
@ActiveProfiles("test")
@OverrideAutoConfiguration(enabled = false)
@ImportAutoConfiguration(classes = [
    BasicConfiguration::class,
    WebClientAutoConfiguration::class
])
@EnableConfigurationProperties(ServerProperties::class)
internal class ServerServiceTests {

    // ====================================== MOCK ============================================//
    @MockBean
    lateinit var serverRepository: ServerRepository
    // ====================================== MOCK ============================================//

    @Autowired
    lateinit var serverService: ServerService

    @Test
    fun init() {
        given(serverRepository.register(Config.server.host, Config.server.port)).willReturn(Mono.empty())

        serverService.Lifecycle().init()
    }

    @Test
    fun destroy() {
        given(serverRepository.unregister(Config.server.host, Config.server.port)).willReturn(Mono.empty())

        serverService.Lifecycle().destroy()
    }

    @Test
    fun clockPing() {
        given(serverRepository.ping(Config.server.host, Config.server.port)).willReturn(Mono.empty())
        given(serverRepository.clean()).willReturn(Mono.empty())

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
        given(serverRepository.findServers()).willReturn(servers.toFlux())

        val rs = serverService.loadServerStates()
        StepVerifier.create(rs)
                .expectNextCount(2)
                .expectComplete()
                .verify()

        mockWebServer.shutdown()
    }

    @Test
    fun info() {
        given(serverRepository.findDbVersion()).willReturn("5.16".toMono())

        val rs = serverService.info()
        StepVerifier.create(rs)
                .expectNextMatches { it.dbVersion.isNotEmpty() }
                .expectComplete()
                .verify()
    }

    @Test
    fun health() {
        given(serverRepository.findDbVersion()).willReturn("5.16".toMono())

        val rs = serverService.health()
        StepVerifier.create(rs)
                .expectNextMatches { it.isNotEmpty() }
                .expectComplete()
                .verify()
    }

    @Test
    fun `health check error`() {
        given(serverRepository.findDbVersion()).willReturn(Mono.error(IllegalStateException("check failed")))

        val rs = serverService.health()
        StepVerifier.create(rs)
                .expectError(HealthCheckException::class)
                .verify()
    }
}