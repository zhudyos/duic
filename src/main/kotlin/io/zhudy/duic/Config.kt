package io.zhudy.duic

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.annotation.Order

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Order(0)
@ConfigurationProperties(prefix = "duic")
object Config {

    var address: String = "0.0.0.0"
    var port: Int = 7777

    /**
     * 超级管理员用户名.
     */
    var rootEmail: String = ""
    var rootPassword: String = ""
    var jwt = Jwt

    object Jwt {
        var secret: String = ""
        var expiresIn: Int = 12 * 60 // 默认12小时过期
    }
}