package io.zhudy.duic.web.route

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.javalin.ApiBuilder.*
import io.javalin.Javalin
import io.javalin.embeddedserver.jetty.EmbeddedJettyFactory
import io.javalin.translator.json.JavalinJacksonPlugin
import io.zhudy.duic.Config
import io.zhudy.duic.web.WebConstants
import io.zhudy.duic.web.admin.AdminResource
import org.eclipse.jetty.server.Server
import org.slf4j.MDC
import org.springframework.stereotype.Component
import java.net.InetSocketAddress
import java.util.*
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Component
class Routes(
        val adminResource: AdminResource
) {

    private val lin: Javalin

    init {
        lin = Javalin.create()
                .enableDynamicGzip()
                .enableCorsForAllOrigins()
                .embeddedServer(EmbeddedJettyFactory({
                    val server = Server(InetSocketAddress(Config.address, Config.port))
                    server
                }))
                .start()

        // jackson
        val objectMapper = ObjectMapper()
        objectMapper.propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        objectMapper.registerModules(Jdk8Module(), JavaTimeModule())
        objectMapper.registerKotlinModule()
        JavalinJacksonPlugin.configure(objectMapper)
    }

    @PostConstruct
    fun init() {
        // request id
        lin.before { ctx ->
            var requestId = ctx.header(WebConstants.REQUEST_ID)
            if (requestId == null) {
                requestId = UUID.randomUUID().toString().replace("-", "")
                ctx.header(WebConstants.REQUEST_ID, requestId)
            }

            MDC.put("requestId", " <$requestId>")
            ctx.attribute(WebConstants.REQUEST_ID, requestId)
        }

        lin.error(404, { ctx ->
            ctx.status(404).result("""{"code":404, "message":"未找到资源"}""")
        })

        lin.routes {
            // api
            path("/api/v1") {
                get("/sc") { ctx ->
                    ctx.result("HELLO")
                }
            }

            // admin
            path("/admin") {
                post("/login", adminResource::login)
                post("/apps", adminResource::saveApp)
                delete("/apps", adminResource::deleteApp)
            }
        }
    }

    @PreDestroy
    fun destroy() {
        lin.stop()
    }
}
