package io.zhudy.duic.domain

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
data class PageResponse(
        val total: Int = 0,
        val items: Any = emptyList<Any>()
)