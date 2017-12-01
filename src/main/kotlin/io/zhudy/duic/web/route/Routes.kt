package io.zhudy.duic.web.route

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.memeyule.cryolite.core.BizCode
import com.memeyule.cryolite.core.BizCodeException
import io.javalin.ApiBuilder.*
import io.javalin.Context
import io.javalin.Javalin
import io.javalin.embeddedserver.jetty.EmbeddedJettyFactory
import io.javalin.translator.json.JavalinJacksonPlugin
import io.zhudy.duic.Config
import io.zhudy.duic.web.WebConstants
import io.zhudy.duic.web.admin.AdminResource
import io.zhudy.duic.web.admin.AppResource
import org.eclipse.jetty.server.Server
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.NestedRuntimeException
import org.springframework.dao.DuplicateKeyException
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
        val adminResource: AdminResource,
        val appResource: AppResource
) {

    private val log = LoggerFactory.getLogger(Routes::class.java)
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
        objectMapper.registerModules(Jdk8Module(), JodaModule())
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

        lin.before("/api") { ctx ->
            ctx.header("Cache-Control", "no-cache").header("Pragma", "no-cache")
        }

        lin.routes {
            // api
            path("/api") {
                path("/v1") {
                    get("/sc") { ctx ->
                        ctx.result("HELLO")
                    }

                    path("/apps/:name/:profiles") {
                        get("", appResource::loadConfigByNp)
                        get("/:key", appResource::loadConfigByNpAndKey)
                    }
                }

                // admin
                path("/admin") {
                    post("/login", adminResource::login)

                    path("/apps") {
                        post("/", adminResource::saveApp)
                        get("/", adminResource::findAll)
                        get("/:name/:profile", adminResource::findOne)
                        put("/", adminResource::updateApp)
                        delete("/:name/:profile", adminResource::deleteApp)
                    }
                }
            }
        }

        // ======================================== 异常处理 ==============================================
        lin.exception(DuplicateKeyException::class.java) { e: DuplicateKeyException, ctx: Context ->
            ctx.status(400).json(mapOf(
                    "code" to BizCode.Classic.C_995,
                    "message" to e.rootCause.message
            ))
            Unit
        }
        lin.exception(BizCodeException::class.java) { e: BizCodeException, ctx: Context ->
            ctx.status(400).json(mapOf(
                    "code" to e.bizCode.code,
                    "message" to if (e.exactMessage.isNotEmpty()) e.exactMessage else e.bizCode.msg
            ))
            Unit
        }
        lin.exception(NestedRuntimeException::class.java) { e: NestedRuntimeException, ctx: Context ->
            log.error("unhandled exception", e)
            ctx.status(500).json(mapOf(
                    "code" to BizCode.Classic.C_500.code,
                    "message" to e.rootCause.message
            ))
            Unit
        }
        lin.exception(Exception::class.java) { e: Exception, ctx: Context ->
            log.error("unhandled exception", e)
            ctx.status(500).json(mapOf(
                    "code" to BizCode.Classic.C_500.code,
                    "message" to e.message
            ))
            Unit
        }
    }

    @PreDestroy
    fun destroy() {
        lin.stop()
    }
}
