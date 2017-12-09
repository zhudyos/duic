package io.zhudy.duic.web

/**
 * `Request` 请求中参数类型错误.
 *
 * @property where 在 path/query/form 中的参数
 * @property parameter 参数名称
 * @property msg 详细描述
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Suppress("MemberVisibilityCanPrivate")
class RequestParameterFormatException(val where: String,
                                      val parameter: String,
                                      val msg: String) : RuntimeException("[$where] 中参数 [$parameter] $msg") {

    override fun fillInStackTrace() = this
}
