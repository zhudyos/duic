package io.zhudy.duic.web

import io.zhudy.duic.config.BasicConfiguration
import io.zhudy.duic.web.config.GlobalWebExceptionHandler
import io.zhudy.duic.web.config.Routers
import io.zhudy.duic.web.config.WebConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.autoconfigure.web.reactive.HttpHandlerAutoConfiguration
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration
import org.springframework.http.server.reactive.HttpHandler
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.server.WebHandler

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@ActiveProfiles("test")
@OverrideAutoConfiguration(enabled = false)
@ImportAutoConfiguration(classes = [
    WebFluxAutoConfiguration::class,
    ReactiveWebServerFactoryAutoConfiguration::class,
    HttpHandlerAutoConfiguration::class,
    JacksonAutoConfiguration::class,
    BasicConfiguration::class,
    WebConfig::class,
    Routers::class,
    GlobalWebExceptionHandler::class
])
abstract class AbstractResourceTestBase {

    @Autowired
    private lateinit var httpHandler: HttpHandler

    /**
     *
     */
    val webTestClient: WebTestClient by lazy {
        WebTestClient.bindToWebHandler(httpHandler as WebHandler).build()
    }
}