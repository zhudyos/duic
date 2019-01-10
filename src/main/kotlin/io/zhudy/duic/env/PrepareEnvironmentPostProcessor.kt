package io.zhudy.duic.env

import io.zhudy.duic.DBMS
import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource

/**
 * 启动服务前检查依赖环境是否可用。全部检查通过用启动服务，或者返回具体的错误信息。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
class PrepareEnvironmentPostProcessor : EnvironmentPostProcessor {

    override fun postProcessEnvironment(env: ConfigurableEnvironment, app: SpringApplication) {
        val dbms = DBMS.forName(env.getRequiredProperty("duic.dbms"))
        if (!dbms.isNoSQL) {
            val dbUrl = env.getRequiredProperty("duic.${dbms.name.toLowerCase()}.url")
            val ps = mapOf<String, Any>(
                    "spring.datasource.url" to dbUrl
            )
            env.propertySources.addLast(MapPropertySource("manual", ps))
        }
    }
}