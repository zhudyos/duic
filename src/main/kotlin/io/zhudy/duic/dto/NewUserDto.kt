package io.zhudy.duic.dto

/**
 * @property email 登录邮箱
 * @property password 初始登录密码
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
data class NewUserDto(
        val email: String,
        val password: String
)