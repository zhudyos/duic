package io.zhudy.duic.web.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.zhudy.duic.BizCode
import io.zhudy.duic.BizCodeException
import io.zhudy.duic.web.MissingRequestParameterException
import io.zhudy.duic.web.RequestParameterFormatException
import org.slf4j.LoggerFactory
import org.springframework.core.NestedRuntimeException
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.UnsupportedMediaTypeException
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebExceptionHandler
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Component
class GlobalWebExceptionHandler(
        val objectMapper: ObjectMapper
) : WebExceptionHandler {

    private val log = LoggerFactory.getLogger(WebExceptionHandler::class.java)

    override fun handle(exchange: ServerWebExchange, e: Throwable): Mono<Void> {
        val response = exchange.response
        var status = 400
        val body = when (e) {
            is DuplicateKeyException -> mapOf(
                    "code" to BizCode.Classic.C_995.code,
                    "message" to e.rootCause.message
            )
            is MissingRequestParameterException -> mapOf(
                    "code" to BizCode.Classic.C_999.code,
                    "message" to e.message
            )
            is RequestParameterFormatException -> mapOf(
                    "code" to BizCode.Classic.C_998.code,
                    "message" to e.message
            )
            is BizCodeException -> {
                status = e.bizCode.status
                mapOf(
                        "code" to e.bizCode.code,
                        "message" to if (e.exactMessage.isNotEmpty()) e.exactMessage else e.bizCode.msg
                )
            }
            is UnsupportedMediaTypeException -> {
                status = 415
                mapOf(
                        "code" to 415,
                        "message" to HttpStatus.UNSUPPORTED_MEDIA_TYPE.reasonPhrase
                )
            }
            is NestedRuntimeException -> {
                log.error("unhandled exception", e)
                status = 500
                mapOf(
                        "code" to BizCode.Classic.C_500.code,
                        "message" to e.rootCause.message
                )
            }
            else -> {
                log.error("unhandled exception", e)
                status = 500
                mapOf(
                        "code" to BizCode.Classic.C_500.code,
                        "message" to e.message
                )
            }
        }

        response.statusCode = HttpStatus.resolve(status)
        response.headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
        val buffer = response.bufferFactory().wrap(objectMapper.writeValueAsBytes(body))
        return response.writeWith(Flux.just(buffer))
    }
}