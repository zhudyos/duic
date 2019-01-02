package io.zhudy.duic.web.v1

import org.springframework.stereotype.Controller
import org.springframework.util.ResourceUtils
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.yaml.snakeyaml.Yaml
import reactor.core.publisher.Mono

/**
 * @author Kevin Zou (yong.zou@2339.com)
 */
@Controller
class OAIResource {

    private val yaml = Yaml()
    private val apiData = yaml.load<Map<String, Any>>(ResourceUtils.getURL("classpath:duic-oas3.yml").openStream())

    /**
     *
     */
    fun index(request: ServerRequest): Mono<ServerResponse> {
        val dumpData = apiData.toMutableMap()
        dumpData.remove("servers")
        return ok().syncBody(yaml.dump(dumpData))
    }

}