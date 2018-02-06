package io.zhudy.duic.utils

/**
 * IP 工具包。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
object IpUtils {

    val LOCALHOST = 2130706433L


    /**
     * 将 IPv4 转换为 `long` 类型。
     */
    fun ipv4ToLong(ip: String): Long {
        if (ip == "localhost" || ip == "127.0.0.1") {
            return LOCALHOST
        }

        var r: Long = 0
        val arr = ip.trim().split(".")
        for (i in 3 downTo 0) {
            r = arr[3 - i].toLong() shl (i * 8) or r
        }
        return r
    }

}