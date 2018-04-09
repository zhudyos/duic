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