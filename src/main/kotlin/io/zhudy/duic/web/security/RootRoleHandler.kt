/**
 * Copyright 2017-2018 the original author or authors
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

import io.zhudy.duic.BizCode
import io.zhudy.duic.BizCodeException
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono


/**
 * ROOT 角色处理。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
class RootRoleHandler(private val next: (ServerRequest) -> Mono<ServerResponse>) : (ServerRequest) -> Mono<ServerResponse> {

    override fun invoke(request: ServerRequest): Mono<ServerResponse> {
        val userContext = request.userContext()
        if (!userContext.isRoot) {
            throw BizCodeException(BizCode.Classic.C_403)
        }
        return next(request)
    }
}
