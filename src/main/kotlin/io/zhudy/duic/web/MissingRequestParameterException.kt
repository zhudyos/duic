package io.zhudy.duic.web

/**
 * `Request` 请求中缺少相应的参数.
 *
 * @property where 在 path/query/form 中的参数
 * @property parameter 参数名称
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Suppress("MemberVisibilityCanPrivate")
class MissingRequestParameterException(val where: String,
                                       val parameter: String) : RuntimeException("""缺少参数 "$parameter" 在 "$where" 中""") {

    override fun fillInStackTrace() = this
}