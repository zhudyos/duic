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
package io.zhudy.duic.web.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.CacheControl
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import reactor.core.publisher.Hooks
import java.util.concurrent.TimeUnit

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Configuration
@EnableWebFlux
class WebConfig(private val objectMapper: ObjectMapper) : WebFluxConfigurer {

    private val log = LoggerFactory.getLogger(WebConfig::class.java)

    init {
        Hooks.onErrorDropped {
            log.error("{}", RuntimeException(it))
        }
    }

    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        val defaults = configurer.defaultCodecs()
        defaults.jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper))
        defaults.jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper))
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // 单页应用，对 html 资源不进行缓存
        registry.addResourceHandler("/*.html")
                .addResourceLocations("classpath:/public-web-resources/")
                .setCacheControl(CacheControl.noCache())

        // 缓存 js css img 等静态资源
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/public-web-resources/")
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
    }

    @Bean
    fun corsWebFilter(): CorsWebFilter {
        val c = CorsConfiguration()
        c.addAllowedOrigin(CorsConfiguration.ALL)
        c.addAllowedMethod(CorsConfiguration.ALL)
        c.addAllowedHeader(CorsConfiguration.ALL)
        c.maxAge = 1800

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/api/**", c)
        return CorsWebFilter(source)
    }
}