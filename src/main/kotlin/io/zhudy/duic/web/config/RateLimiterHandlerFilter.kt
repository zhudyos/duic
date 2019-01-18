package io.zhudy.duic.web.config

import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.reactor.ratelimiter.operator.MonoRateLimiter
import org.springframework.web.reactive.function.server.HandlerFilterFunction
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

/**
 * 限流过滤器。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
class RateLimiterHandlerFilter(
        private val rateLimiter: RateLimiter
) : HandlerFilterFunction<ServerResponse, ServerResponse> {

    private val scheduler = Schedulers.newElastic("rate-limiter-handler", 30, true)!!

    override fun filter(request: ServerRequest, next: HandlerFunction<ServerResponse>): Mono<ServerResponse> {
        return MonoRateLimiter(next.handle(request), rateLimiter, scheduler)
    }
}