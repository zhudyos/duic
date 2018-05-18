package io.zhudy.duic.dto

import org.joda.time.DateTime

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
data class ServerStateDto(
        val host: String = "",
        val port: Int = 0,
        val initAt: DateTime,
        val activeAt: DateTime,
        val lastDataTime: Long
)