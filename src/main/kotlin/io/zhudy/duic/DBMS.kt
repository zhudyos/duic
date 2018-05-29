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
package io.zhudy.duic

/**
 * duic 支持数据库名称。
 *
 * @property isNoSQL 如果是 NoSQL 数据库则为 `true` 反之为 `false`
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
enum class DBMS(@JvmField val isNoSQL: Boolean = false) {

    /**
     * MongoDB 数据库。
     */
    MongoDB(true),
    /**
     * MySQL/MariaDB 数据库。
     */
    MySQL,
    /**
     * PostgreSQL 数据库。
     */
    PostgreSQL,
    /**
     * Oracle 数据库。
     */
    Oracle
    ;

    companion object {

        /**
         *
         */
        @JvmStatic
        fun forName(name: String) = DBMS.values().first {
            it.name.toLowerCase() == name.toLowerCase()
        }
    }
}