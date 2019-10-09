package io.zhudy.duic.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.Instant

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
data class UserDto(
        val email: String,
        @JsonIgnore
        val password: String,
        val createdAt: Instant,
        val updatedAt: Instant
)