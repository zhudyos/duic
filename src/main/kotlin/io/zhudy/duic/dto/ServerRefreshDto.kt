package io.zhudy.duic.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
data class ServerRefreshDto(
        @JsonProperty("last_updated_at")
        val lastUpdatedAt: Long
)