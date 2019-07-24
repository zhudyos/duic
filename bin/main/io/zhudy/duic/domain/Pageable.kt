/**
 * Copyright 2017-2019 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zhudy.duic.domain

/**
 * 分页查询参数对象。
 *
 * @property page 页码
 * @property size 每页条数
 * @property offset 分页偏移量
 * @property begin 开始行数
 * @property end 结束行数
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
data class Pageable(
        val page: Int = 1,
        val size: Int = 15
) {

    val offset = (page - 1) * size
    val begin = offset + 1
    val end = offset + size
}