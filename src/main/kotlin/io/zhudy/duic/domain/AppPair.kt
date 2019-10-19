package io.zhudy.duic.domain

/**
 * 包括 `name`，`profile` 的属性定义。
 *
 * @property name 应用名称
 * @property profile 应用环境
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
data class AppPair(
        val name: String,
        val profile: String
)