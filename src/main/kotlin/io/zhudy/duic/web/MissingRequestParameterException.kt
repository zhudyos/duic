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
package io.zhudy.duic.web

/**
 * `Request` 请求中缺少相应的参数.
 *
 * @property where 在 path/query/form 中的参数
 * @property parameter 参数名称
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Suppress("MemberVisibilityCanBePrivate")
class MissingRequestParameterException(val where: String,
                                       val parameter: String) : RuntimeException("""缺少参数 "$parameter" 在 "$where" 中""") {

    override fun fillInStackTrace() = this
}