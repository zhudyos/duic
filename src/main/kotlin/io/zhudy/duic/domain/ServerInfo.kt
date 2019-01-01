package io.zhudy.duic.domain

/**
 * 服务信息实体。
 *
 * @property name 应用名称
 * @property version 应用版本
 * @property dbName 数据库名称
 * @property dbVersion 数据库版本
 * @property osName 操作系统名称
 * @property osVersion 操作系统版本
 * @property osArch 操作系统位
 * @property javaVersion Java版本
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
data class ServerInfo(
        val name: String,
        val version: String,
        val dbName: String,
        val dbVersion: String,
        val osName: String,
        val osVersion: String,
        val osArch: String,
        val javaVersion: String
)