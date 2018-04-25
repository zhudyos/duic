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
     * MongoDB。
     */
    MongoDB(true),
    /**
     * MySQL/MariaDB。
     */
    MySQL
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