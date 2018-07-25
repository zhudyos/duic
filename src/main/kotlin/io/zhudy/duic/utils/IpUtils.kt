/**
 * Copyright 2017-2018 the original author or authors
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
package io.zhudy.duic.utils

/**
 * IP 工具包。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
object IpUtils {

    private const val LOCALHOST = 2130706433L


    /**
     * 将 IPv4 转换为 `long` 类型。
     */
    fun ipv4ToLong(ip: String): Long {
        if (ip == "localhost" || ip == "127.0.0.1") {
            return LOCALHOST
        }

        var r: Long = 0
        val arr = ip.trim().split(".")
        for (i in 3 downTo 0) {
            r = arr[3 - i].toLong() shl (i * 8) or r
        }
        return r
    }

}