package io.zhudy.duic.dto

/**
 * 更新应用配置的基本信息。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
data class AppUpdateBasicInfoDto(
        val description: String,
        val token: String = "",
        val ipLimit: String = "",
        val v: Int,
        val users: List<String>
)