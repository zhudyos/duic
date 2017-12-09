package io.zhudy.duic.domain

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
data class PageResponse(
        val total: Long = 0,
        val items: Any = emptyList<Any>()
)