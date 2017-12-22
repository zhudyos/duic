package io.zhudy.duic.dto

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
data class ResetPasswordDto(
        val email: String,
        val password: String
)