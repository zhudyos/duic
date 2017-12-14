package io.zhudy.duic.repository

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Updates
import com.mongodb.client.model.Updates.combine
import io.zhudy.duic.domain.PageResponse
import io.zhudy.duic.domain.User
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Repository
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Repository
class UserRepository(val mongoTemplate: MongoTemplate, val passwordEncoder: PasswordEncoder) {

    /**
     * 保存用户.
     */
    fun save(user: User) = mongoTemplate.save(user)

    /**
     * 删除用户.
     */
    fun delete(email: String) = mongoTemplate.remove(Query(Criteria.where("email").isEqualTo(email)), User::class.java)

    /**
     * 更新用户密码.
     */
    fun updatePassword(email: String, password: String) {
        mongoTemplate.execute(User::class.java) { coll ->
            coll.updateOne(eq("email", email),
                    combine(Updates.set("password", password), Updates.set("updated_at", Date()))
            )
        }
    }

    /**
     *
     */
    fun findByEmailOrNull(email: String): User? = mongoTemplate.findOne(Query(where("email").isEqualTo(email)), User::class.java)

    /**
     *
     */
    fun findPage(page: Pageable): PageResponse {
        val items = mongoTemplate.find(Query().with(page), User::class.java)
        val count = mongoTemplate.count(Query(), User::class.java)
        return PageResponse(count, items)
    }

    /**
     *
     */
    fun findAllEmail(): List<String> {
        return mongoTemplate.execute(User::class.java) { coll ->
            coll.find().projection(Projections.include("email")).toList().map { it.getString("email") }
        }
    }

}