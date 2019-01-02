package io.zhudy.duic.env

import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment

/**
 * 启动服务前检查依赖环境是否可用。全部检查通过用启动服务，或者返回具体的错误信息。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
class CheckEnvironmentPostProcessor : EnvironmentPostProcessor {

    override fun postProcessEnvironment(env: ConfigurableEnvironment, app: SpringApplication) {
        // FIXME 在这里校验参数
        println(env)
    }
}