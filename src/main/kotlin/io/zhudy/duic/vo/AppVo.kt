package io.zhudy.duic.vo

/**
 * 应用配置值对象。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
class AppVo {

    /**
     * 创建一个新的应用配置。
     *
     * @property name 应用名称
     * @property profile 应用环境
     * @property description 应用描述
     * @property content 应用配置
     * @property token 访问令牌
     * @property ipLimit 访问 IP 限制
     * @property users 拥有配置管理权限的用户
     */
    data class NewApp(
            val name: String,
            val profile: String,
            val description: String,
            val content: String = "",
            val token: String,
            val ipLimit: String,
            val users: List<String>
    )

    /**
     * @property description 应用描述
     * @property token 访问令牌
     * @property ipLimit IP 访问限制
     * @property v 应用版本
     * @property users 拥有操作权限的用户
     */
    data class UpdateBasicInfo(
            val description: String,
            val token: String = "",
            val ipLimit: String = "",
            val v: Int,
            val users: List<String>
    )

    /**
     * @property content 修改的配置内容
     * @property v 当前的配置版本
     */
    data class UpdateContent(
            val content: String,
            val v: Int
    )

    /**
     * 用户应用配置搜索。
     *
     * @property q 关键字
     * @property email 指定用户
     */
    data class UserQuery(
            val q: String? = null,
            val email: String? = null
    )
}