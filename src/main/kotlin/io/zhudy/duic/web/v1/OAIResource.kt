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
    private val apiData = yaml.load<Map<String, Any>>(ResourceUtils.getURL("classpath:duic-open-api.yml").openStream())

    /**
     *
     */
    fun index(request: ServerRequest): Mono<ServerResponse> {
        val uri = request.uri()
        val url = "${uri.scheme}://${uri.rawAuthority}/api/v1"
        val dumpData = apiData.toMutableMap()
        dumpData["servers"] = listOf(mapOf(
                "description" to "DuiC Server",
                "url" to url
        ))

        return ok().syncBody(yaml.dump(dumpData))
    }

}