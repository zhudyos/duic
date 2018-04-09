package io.zhudy.duic.domain

import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class AppContentHistory(
        var hid: String = "",
        var updatedBy: String = "",
        var content: String = "",
        var updatedAt: Date? = null
)