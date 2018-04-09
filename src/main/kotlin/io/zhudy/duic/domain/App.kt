package io.zhudy.duic.domain

import org.joda.time.DateTime

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class App(
        var id: String = "",
        var name: String = "",
        var profile: String = "",
        var token: String = "",
        var ipLimit: String = "",
        var description: String = "",
        var v: Int = 1,
        var createdAt: DateTime? = null,
        var updatedAt: DateTime? = null,
        var content: String = "",
        var users: List<String> = emptyList()
)