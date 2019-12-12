package io.zhudy.duic.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.LocalDateTime

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
data class UserDto(
        val email: String,
        @JsonIgnore
        val password: String,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime = LocalDateTime.MIN
)