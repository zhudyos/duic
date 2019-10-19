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

}