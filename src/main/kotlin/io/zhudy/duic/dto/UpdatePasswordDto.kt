package io.zhudy.duic.dto

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
data class UpdatePasswordDto(
        val email: String,
        val password: String
)