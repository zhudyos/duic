package io.zhudy.duic.domain

import org.springframework.data.mongodb.core.mapping.Field
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class AppContentHistory(
        @Field
        var hid: String = "",
        @Field("revised_by")
        var updatedBy: String = "",
        @Field
        var content: String = "",
        @Field("updated_at")
        var updatedAt: Date = Date()
)