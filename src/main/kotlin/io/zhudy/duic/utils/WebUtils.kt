package io.zhudy.duic.utils

import io.zhudy.duic.domain.Pageable
import io.zhudy.duic.web.queryInt
import org.springframework.web.reactive.function.server.ServerRequest

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
     * 多个排序字段使用英文逗号(,)分隔, 字段前增加(`-`)表示该字段使用降序排列, 默认为升序排列.
     *
     */
    fun getPage(request: ServerRequest): Pageable {
        val p = request.queryInt("page")
        val s = request.queryInt("size")
        return Pageable(p, s)
    }

//    /**
//     * 获取排序参数.
//     *
//     * 示例:
//     * `http://examples.com/sample?sort=-id,name`
//     *
//     * 多个排序字段使用英文逗号(,)分隔, 字段前增加(`-`)表示该字段使用降序排列, 默认为升序排列.
//     */
//    fun getSort(request: ServerRequest): Sort {
//        val s = request.queryParam("sort").orElse("")
//        if (s.isNotEmpty()) {
//            val orders = arrayListOf<Sort.Order>()
//            s.split(",").forEach {
//                val f = it.trim()
//                if (f.isNotEmpty() && f != "-") {
//                    if (f[0] == '-') {
//                        orders.add(Sort.Order(Sort.Direction.DESC, f.substring(1)))
//                    } else {
//                        orders.add(Sort.Order(f))
//                    }
//                }
//            }
//
//            return Sort.by(orders)
//        }
//        return Sort.unsorted()
//    }
}