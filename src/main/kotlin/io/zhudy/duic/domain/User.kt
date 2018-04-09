package io.zhudy.duic.domain

import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class User(
        var email: String = "",
        var password: String = "",
        var createdAt: Date? = null,
        var updatedAt: Date? = null
)