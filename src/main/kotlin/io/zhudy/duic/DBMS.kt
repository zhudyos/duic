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
 * duic 支持数据库名称。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
enum class DBMS {

    /**
     * MySQL/MariaDB 数据库。
     */
    MySQL,
    /**
     * PostgreSQL 数据库。
     */
    PostgreSQL,
    /**
     * 无数据库，应用于单元测试环境。
     */
    None,
    ;

    companion object {

        /**
         *
         */
        @JvmStatic
        fun forName(name: String) = values().firstOrNull {
            it.name.toLowerCase() == name.toLowerCase()
        } ?: throw IllegalArgumentException("not found dbms: $name")
    }
}