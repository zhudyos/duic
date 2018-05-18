package io.zhudy.duic.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
data class ServerRefreshDto(
        @JsonProperty("last_data_time")
        val lastDataTime: Long
)