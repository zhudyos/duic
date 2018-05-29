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
package io.zhudy.duic.domain

import org.joda.time.DateTime

/**
 * 服务主机信息。
 *
 * @property host 主机名
 * @property port 主机端口
 * @property initAt 初始化时间
 * @property activeAt 最后活跃时间如果于当前时间相关超过1分钟则认为该服务不处于活跃中
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
class Server(
        val host: String = "",
        val port: Int = 0,
        val initAt: DateTime,
        val activeAt: DateTime
)