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
 * @author Kevin Zou (kevinz@weghst.com)
 */
class AppHistory(
        var id: String = "",
        var name: String = "",
        var profile: String = "",
        var description: String = "",
        var content: String = "",
        var token: String = "",
        var ipLimit: String = "",
        var v: Int = 1,
        var createdAt: DateTime? = null,
        var updatedBy: String = "",
        var deletedBy: String = "",
        var users: List<String> = emptyList()
)