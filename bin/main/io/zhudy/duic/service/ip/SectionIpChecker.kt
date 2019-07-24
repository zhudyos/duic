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
package io.zhudy.duic.service.ip

import io.zhudy.duic.service.IpChecker

/**
 * 一段 IP 校验器(192.168.0.1-192.168.0.255)。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
class SectionIpChecker(val from: Long, val to: Long) : IpChecker {

    /**
     * 包涵该 IP 则返回 `true` 反之返回 `false`。
     */
    override fun check(ip: Long) = ip in from..to
}