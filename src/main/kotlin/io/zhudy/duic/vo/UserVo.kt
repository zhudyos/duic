package io.zhudy.duic.vo

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class UserVo {

    /**
     * @property email 登录邮箱
     * @property password 初始登录密码
     */
    data class NewUser(
            val email: String,
            val password: String
    )

    /**
     * 修改密码。
     *
     * @property email 用户邮箱
     * @property oldPassword 原密码
     * @property newPassword 新密码
     */
    data class UpdatePassword(
            val email: String,
            val oldPassword: String,
            val newPassword: String
    )

    /**
     * 重置密码。
     *
     * @property email 登录邮箱
     * @property password 初始登录密码
     */
    data class ResetPassword(
            val email: String,
            val password: String
    )

}