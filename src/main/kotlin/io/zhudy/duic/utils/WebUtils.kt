package io.zhudy.duic.utils

import io.javalin.Context
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

/**
 * `Web` 工具包.
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
object WebUtils {

    /**
     * 获取分页与排序参数.
     *
     * 示例:
     * `http://examples.com/sample?page=1&size=10&sort=-id,name`
     *
     * `page` 默认值为 `1`
     *
     * `size` 默认值为 `10`
     *
     * 多个排序字段使用英文逗号(,)分隔, 字段前增加(`-`)表示该字段使用降序排列, 默认为升序排列.
     *
     */
    fun getPage(ctx: Context): Pageable {
        val p = (ctx.queryParam("page")?.trim()?.toInt() ?: 1) - 1
        val s = ctx.queryParam("size")?.trim()?.toInt() ?: 10
        return PageRequest.of(p, s, getSort(ctx))
    }

    /**
     * 获取排序参数.
     *
     * 示例:
     * `http://examples.com/sample?sort=-id,name`
     *
     * 多个排序字段使用英文逗号(,)分隔, 字段前增加(`-`)表示该字段使用降序排列, 默认为升序排列.
     */
    fun getSort(ctx: Context): Sort {
        val s = ctx.queryParam("sort")?.trim()
        if (s != null && s.isNotEmpty()) {
            val orders = arrayListOf<Sort.Order>()
            s.split(",").forEach {
                val f = it.trim()
                if (f.isNotEmpty() && f != "-") {
                    if (f[0] == '-') {
                        orders.add(Sort.Order(Sort.Direction.DESC, f.substring(1)))
                    } else {
                        orders.add(Sort.Order(f))
                    }
                }
            }

            return Sort.by(orders)
        }
        return Sort.unsorted()
    }
}