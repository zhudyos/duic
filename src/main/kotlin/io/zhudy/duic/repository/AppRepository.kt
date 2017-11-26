package io.zhudy.duic.repository

import io.zhudy.duic.domain.App
import io.zhudy.duic.repository.internal.MongoHelper
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Repository
class AppRepository(
        val mongoTemplate: MongoTemplate,
        val mongoHelper: MongoHelper
) {

    /**
     *
     */
    fun save(app: App) {
        app.id = mongoHelper.getNext("app").toInt()
        app.createdAt = LocalDateTime.now()
        mongoTemplate.save(app)
    }

}