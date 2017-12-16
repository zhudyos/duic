package io.zhudy.duic.web.security

import com.memeyule.cryolite.core.BizCode
import com.memeyule.cryolite.core.BizCodeException
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono


/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class RootRoleHandler(private val next: (ServerRequest) -> Mono<ServerResponse>) : (ServerRequest) -> Mono<ServerResponse> {

    /**
     *
     */
    override fun invoke(request: ServerRequest): Mono<ServerResponse> {
        val userContext = request.userContext()
        if (userContext.isRoot) {
            throw BizCodeException(BizCode.Classic.C_403)
        }
        return next(request)
    }
}

