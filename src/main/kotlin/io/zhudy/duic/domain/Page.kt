package io.zhudy.duic.domain

import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
data class Page<out T>(
        val items: List<T> = emptyList(),
        val totalItems: Int = 0,
        @JsonIgnore
        val pageable: Pageable
) {
    val totalPages: Int = if (pageable.size == 0) 1 else Math.ceil(totalItems.toDouble() / pageable.size.toDouble()).toInt()
}