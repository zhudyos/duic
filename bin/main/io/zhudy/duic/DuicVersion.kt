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
 * duic 版本号。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
object DuicVersion {

    /**
     * 返回 duic 版本号，如果没有则返回空字符串。
     */
    val version: String by lazy {
        DuicVersion::class.java.`package`?.implementationVersion ?: ""
    }

}