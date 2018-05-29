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
package io.zhudy.duic.web.server

import io.zhudy.duic.Config
import io.zhudy.duic.dto.ServerRefreshDto
import io.zhudy.duic.service.AppService
import io.zhudy.duic.service.ServerService
import io.zhudy.duic.web.body
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import java.net.NetworkInterface

/**
 *`/servers`。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Controller
class ServerResource(
        serverProperties: ServerProperties,
        val serverService: ServerService,
        val appService: AppService
) {

    init {
        Config.server = Config.Server(
                host = serverProperties.address?.hostAddress ?: getLocalHost(),
                port = serverProperties.port,
                sslEnabled = serverProperties.ssl?.isEnabled ?: false
        )
    }

    /**
     *
     */
    fun refreshApp(request: ServerRequest) = appService.refresh().flatMap {
        ok().body(ServerRefreshDto(it))
    }

    /**
     *
     */
    fun getLastDataTime(request: ServerRequest) = ok().body(ServerRefreshDto(appService.getMemoryLastDataTime()))

    private fun getLocalHost(): String {
        val e = NetworkInterface.getNetworkInterfaces()
        while (e.hasMoreElements()) {
            val ni = e.nextElement()
            val addr = ni.interfaceAddresses.find { it.address.isSiteLocalAddress }
            if (addr != null) {
                return addr.address.hostAddress
            }
        }
        throw IllegalStateException("未找到本机可访问的 IP 地址，请使用 server.address 配置上明确指定服务 IP")
    }

}