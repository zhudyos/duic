package io.zhudy.duic.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Document(collection = "app")
@CompoundIndex(name = "name_profile", def = """{"name":1, "profile":1}""", unique = true)
class App(
        @Id
        var id: Int = 0,
        @Field
        var name: String,
        @Field
        var profile: String,
        @Field("created_at")
        var createdAt: LocalDateTime? = null,
        @Field("updated_at")
        var updatedAt: LocalDateTime? = null,
        @Field("properties")
        var properties: org.bson.Document = org.bson.Document()
)