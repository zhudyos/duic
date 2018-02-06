package io.zhudy.duic.vo

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
data class RequestConfigVo(
        var name: String = "",
        var profiles: List<String> = emptyList(),
        var configTokens: List<String> = emptyList(),
        var key: String = "",
        var clientIpv4: String = ""
)