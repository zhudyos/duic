package io.zhudy.duic.service

/**
 * 健康检查错误。
 *
 * @property code 响应的错误码
 * @property description 错误描述
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
class HealthCheckException(val code: Int, val description: String) : RuntimeException("$code: $description")