package io.zhudy.duic.domain

/**
 *
 * @property page
 * @property size
 * @property offset
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
data class Pageable(
        val page: Int = 1,
        val size: Int = 15
) {

    val offset = (page - 1) * size
}