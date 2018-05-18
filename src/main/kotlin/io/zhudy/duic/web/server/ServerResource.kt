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