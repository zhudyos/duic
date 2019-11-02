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
package io.zhudy.duic.web.v1

import org.springframework.util.ResourceUtils
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.yaml.snakeyaml.Yaml
import reactor.core.publisher.Mono

/**
 * OpenAPI 接口文档数据。
 *
 * @author Kevin Zou (yong.zou@2339.com)
 */
@RestController
class OAIResource {

    private val apiData: String by lazy {
        val stream = ResourceUtils.getURL("classpath:duic-oas3.yml").openStream()
        stream.use {
            val yaml = Yaml()
            val d = yaml.load<Map<String, Any>>(it).toMutableMap()
            d.remove("servers")
            yaml.dump(d)
        }
    }

    /**
     * 获取 OpenAPI。
     */
    fun index(request: ServerRequest): Mono<ServerResponse> = ok().bodyValue(apiData)
}