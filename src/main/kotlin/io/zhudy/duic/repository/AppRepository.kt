package io.zhudy.duic.repository

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.google.common.cache.CacheBuilder
import com.memeyule.cryolite.core.BizCodeException
import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Updates.*
import io.zhudy.duic.BizCodes
import io.zhudy.duic.UserContext
import io.zhudy.duic.domain.App
import io.zhudy.duic.domain.PageResponse
import io.zhudy.duic.dto.AppDto
import org.bson.Document
import org.bson.types.ObjectId
import org.hashids.Hashids
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Repository
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Repository
class AppRepository(
        val mongoTemplate: MongoTemplate
) {

    private val hashids = Hashids()
    private val log = LoggerFactory.getLogger(AppRepository::class.java)
    private val mapper = ObjectMapper(YAMLFactory())
    private val localCache = CacheBuilder.newBuilder().build<String, AppDto>()
    private val hashMapTypeRef = object : TypeReference<HashMap<String, Any>>() {}
    private var lastUpdatedAt: DateTime? = null

    @Scheduled(initialDelay = 0, fixedDelayString = "\${app.watch.fixed_delay:5000}")
    protected fun watchApps() {
        try {
            if (lastUpdatedAt == null) {
                findAll()
            } else {
                findByUpdatedAt(lastUpdatedAt!!)
            }.forEach {
                localCache.put(localKey(it.name, it.profile), it)
            }
            lastUpdatedAt = DateTime.now()
        } catch (e: Exception) {
            log.error("加载应用信息至缓存中失败", e)
        }
    }

    /**
     *
     */
    fun save(app: App) {
        app.id = ObjectId().toHexString()
        app.createdAt = DateTime.now()
        app.updatedAt = DateTime.now()
        mongoTemplate.save(app)
    }

    /**
     *
     */
    fun findApp(name: String, profile: String): App {
        return mongoTemplate.findOne(
                Query(Criteria.where("name").isEqualTo(name).and("profile").isEqualTo(profile)),
                App::class.java
        )
    }

    /**
     *
     * 错误:
     * - [BizCodes.C_1003]
     * - [BizCodes.C_1004]
     * - [BizCodes.C_1006]
     */
    fun updateContent(app: App, userContext: UserContext): Int {
        try {
            mapper.readValue<HashMap<String, Any>>(app.content, hashMapTypeRef)
        } catch (e: Exception) {
            throw BizCodeException(BizCodes.C_1006, e)
        }

        val old = loadOne(app.name, app.profile)
        val n = mongoTemplate.execute(App::class.java) { coll ->
            val updatedAt = Date()
            coll.updateOne(
                    Document("name", app.name).append("profile", app.profile).append("v", app.v),
                    combine(
                            set("content", app.content), set("updated_at", updatedAt), inc("v", 1),
                            push("histories", mapOf<String, Any>(
                                    "hid" to hashids.encode(app.id.hashCode().toLong(), System.currentTimeMillis(), app.v.toLong()),
                                    "content" to old.content,
                                    "revised_by" to userContext.email,
                                    "updated_at" to updatedAt
                            ))
                    )
            )
        }.modifiedCount

        if (n < 1) {
            if (app.v != old.v) {
                throw BizCodeException(BizCodes.C_1004)
            }
            throw BizCodeException(BizCodes.C_1003, "修改 ${app.name}/${app.profile} 失败")
        }
        return app.v + 1
    }

    /**
     *
     */
    fun loadOne(name: String, profile: String): AppDto {
        val k = localKey(name, profile)
        return localCache.get(k) {
            findOne(name, profile)
        }
    }

    /**
     *
     */
    fun findOne(name: String, profile: String): AppDto {
        return mongoTemplate.execute(App::class.java) { coll ->
            mapToAppDto(coll.find(and(eq("name", name), eq("profile", profile)))
                    .projection(Projections.include("name", "profile", "v", "created_at", "updated_at", "content", "users"))
                    .first()
            )
        }
    }

    /**
     *
     */
    fun findAll(): List<AppDto> {
        return mongoTemplate.execute(App::class.java) { coll ->
            val list = arrayListOf<AppDto>()
            coll.find().forEach {
                list.add(mapToAppDto(it))
            }
            list
        }
    }

    /**
     * 分页查询.
     */
    fun findPage(page: Pageable): PageResponse {
        return mongoTemplate.execute(App::class.java) { coll ->
            val list = arrayListOf<AppDto>()
            coll.find().limit(page.pageSize).skip(page.offset.toInt()).forEach {
                list.add(mapToAppDto(it))
            }
            PageResponse(coll.count(), list)
        }
    }

    /**
     *
     */
    fun findPageByUser(page: Pageable, userContext: UserContext): PageResponse {
        return mongoTemplate.execute(App::class.java) { coll ->
            val list = arrayListOf<AppDto>()

            coll.find(elemMatch("users", eq("\$eq", userContext.email))).limit(page.pageSize).skip(page.offset.toInt()).forEach {
                list.add(mapToAppDto(it))
            }
            PageResponse(coll.count(), list)
        }
    }

    /**
     *
     */
    fun findByUpdatedAt(updatedAt: DateTime): List<AppDto> {
        return mongoTemplate.execute(App::class.java) { coll ->
            val list = arrayListOf<AppDto>()
            coll.find(gte("updated_at", updatedAt.toDate())).forEach {
                list.add(mapToAppDto(it))
            }
            list
        }
    }

    private fun mapToAppDto(doc: Document): AppDto {
        val content = doc.getString("content") ?: ""
        val properties = if (content.isNotEmpty()) {
            mapper.readValue(content, hashMapTypeRef)
        } else {
            emptyMap<String, Any>()
        }

        return AppDto(
                name = doc.getString("name") ?: "",
                profile = doc.getString("profile") ?: "",
                v = doc.getInteger("v", 0),
                createdAt = DateTime(doc["created_at"] as Date),
                updatedAt = DateTime(doc["updated_at"] as Date),
                content = content,
                properties = properties
        )
    }

    private fun localKey(name: String, profile: String) = "$name/$profile"
}