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
package io.zhudy.duic

/**
 * 业务码接口.
 *
 * @author Kevin Zou <kevinz@weghst.com>
 */
interface BizCode {

    /**
     * 业务码(1000)以内的为保留业务码.
     */
    val code: Int

    /**
     * 描述.
     */
    val msg: String

    /**
     * HTTP Status.
     */
    val status: Int

    /**
     * 经典错误码.
     */
    enum class Classic(override val code: Int, override val msg: String, override val status: Int = 400) : BizCode {
        /**
         * 未知的系统错误.
         */
        C_0(0, "未知错误"),
        /**
         * 未认证.
         */
        C_401(401, "未认证", 401),
        /**
         *
         */
        C_403(403, "拒绝访问", 403),
        /**
         * 服务器内部错误.
         */
        C_500(500, "服务器内部错误", 500),
        C_995(995, "唯一索引冲突"),
        C_996(996, "不是可选的枚举值"),
        C_997(997, "获取第三方数据失败"),
        /**
         * HTTP Request 参数错误.
         */
        C_998(998, "HTTP Request 参数类型错误"),
        C_999(999, "HTTP Request 参数错误"),
        ;
    }
}