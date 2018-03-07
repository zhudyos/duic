package io.zhudy.duic.domain

import org.joda.time.DateTime
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Document(collection = "app_history")
class AppHistory(
        @Id
        var id: String = "",
        @Field
        var name: String = "",
        @Field
        var profile: String = "",
        @Field
        var description: String = "",
        @Field
        var v: Int = 1,
        @Field("created_at")
        var createdAt: DateTime? = null,
        @Field
        var deletedBy: String = "",
        @Field
        var content: String = "",
        @Field
        var users: List<String> = emptyList()
)