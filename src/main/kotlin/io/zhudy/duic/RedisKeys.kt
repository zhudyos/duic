package io.zhudy.duic

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */

class RedisKey(private val prefix: String) {

    /**
     *
     */
    fun key(vararg args: Any) = args.joinToString(prefix = prefix, separator = ":")
}

object RedisKeys {

    /**
     * App 自增长ID.
     */
    const val APP_INCREMENT = "duic:app:increment"

    /**
     * App 列表名称.
     */
    const val APP_LIST = "duic:app:list"

    /**
     * App 详细信息.
     */
    val APP_DETAILS = RedisKey("duic:app:details")
}