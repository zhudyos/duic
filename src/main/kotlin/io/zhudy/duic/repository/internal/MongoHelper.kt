package io.zhudy.duic.repository.internal

import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates
import org.bson.Document
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Repository

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Repository
class MongoHelper(val mongoTemplate: MongoTemplate) {

    /**
     *
     */
    fun getNext(name: String): Long {
        return mongoTemplate.execute("counter") {
            it.findOneAndUpdate(Document("_id", name), Updates.inc("count", 1L),
                    FindOneAndUpdateOptions().projection(Document("count", 1)).returnDocument(ReturnDocument.AFTER).upsert(true)).getLong("count")
        }
    }

}