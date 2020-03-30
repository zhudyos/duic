/**
 * Copyright 2017-2019 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zhudy.duic.web.security

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.security.Keys
import io.zhudy.duic.Config
import io.zhudy.duic.UserContext
import io.zhudy.kitty.core.biz.BizCode
import io.zhudy.kitty.core.biz.BizCodeException
import org.springframework.web.reactive.function.server.HandlerFilterFunction
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.security.SignatureException

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class AuthorizedHandlerFilter : HandlerFilterFunction<ServerResponse, ServerResponse> {

    override fun filter(request: ServerRequest, next: HandlerFunction<ServerResponse>): Mono<ServerResponse> {
        if (request.uri().path.endsWith("/api/admins/login")) {
            return next.handle(request)
        }
        val token = request.cookies().getFirst("token")?.value
        if (token.isNullOrEmpty()) {
            throw BizCodeException(BizCode.C401, "缺少 token")
        }


        val jwt = try {
            Jwts.parser()
                    .setSigningKey(Keys.hmacShaKeyFor(Config.jwt.secret.toByteArray()))
                    .parseClaimsJwt(token)
        } catch (e: MalformedJwtException) {
            throw BizCodeException(BizCode.C401, "解析 token 失败 ${e.message}")
        } catch (e: ExpiredJwtException) {
            throw BizCodeException(BizCode.C401, "token 已经过期请重新登录")
        } catch (e: SignatureException) {
            throw BizCodeException(BizCode.C401, "access_token 校验失败")
        }

        if (jwt.body.id.isNullOrEmpty()) {
            throw BizCodeException(BizCode.C401, "错误的 token")
        }

        request.attributes()[UserContext.CONTEXT_KEY] = object : UserContext {
            override val email: String
                get() = jwt.body.id
            override val isRoot: Boolean
                get() = Config.rootEmail == email
        }
        return next.handle(request)
    }
}