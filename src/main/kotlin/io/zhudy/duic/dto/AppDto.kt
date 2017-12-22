package io.zhudy.duic.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import org.joda.time.DateTime

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
data class AppDto(
        val name: String,
        val profile: String,
        val token: String,
        val v: Int,
        val createdAt: DateTime,
        val updatedAt: DateTime,
        val content: String,
        @JsonIgnore
        val properties: Map<Any, Any>
)