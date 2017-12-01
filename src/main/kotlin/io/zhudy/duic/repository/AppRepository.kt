package io.zhudy.duic.repository

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.google.common.cache.CacheBuilder
import com.memeyule.cryolite.core.BizCodeException
import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Updates.*
import io.zhudy.duic.BizCodes
import io.zhudy.duic.domain.App
import io.zhudy.duic.domain.PageResponse
import io.zhudy.duic.dto.AppDto
import org.bson.Document
import org.bson.types.ObjectId
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Repository
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Repository
class AppRepository(
        val mongoTemplate: MongoTemplate
) {

    private val log = LoggerFactory.getLogger(AppRepository::class.java)
    private val mapper = ObjectMapper(YAMLFactory())
    private val localCache = CacheBuilder.newBuilder().build<String, AppDto>()

    private val reloadExec = Executors.newSingleThreadScheduledExecutor {
        val t = Thread(it)
        t.name = "reload-latest-app"
        t.isDaemon = true
        t
    }
    private var lastUpdatedAt: DateTime? = null

    @PostConstruct
    fun init() {
        reloadExec.scheduleAtFixedRate({
            try {
                val l = lastUpdatedAt
                lastUpdatedAt = DateTime.now()

                findByUpdatedAt(l).forEach {
                    localCache.put(localKey(it.name, it.profile), it)
                }
            } catch (e: Exception) {
                log.error("加载应用信息至缓存中失败", e)
            }
        }, 1, 3, TimeUnit.SECONDS)
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
    fun updateContent(app: App) {
        val n = mongoTemplate.execute(App::class.java) { coll ->
            coll.updateOne(
                    Document("name", app.name).append("profile", app.profile).append("v", app.v),
                    combine(set("content", app.content), set("updated_at", DateTime.now()), inc("v", 1))
            )
        }.modifiedCount

        if (n < 1) {
            throw BizCodeException(BizCodes.C_1003, "修改 ${app.name}/${app.profile} 失败")
        }
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
            mapToAppDto(coll.find(and(eq("name", name), eq("profile", profile))).first())
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
            coll.find().count()
            PageResponse(coll.find().count(), list)
        }
    }

    /**
     *
     */
    fun findByUpdatedAt(updatedAt: DateTime?): List<AppDto> {
        if (updatedAt == null) {
            return findAll()
        }
        return mongoTemplate.execute(App::class.java) { coll ->
            val list = arrayListOf<AppDto>()
            coll.find(gte("updated_at", updatedAt.toDate())).forEach {
                list.add(mapToAppDto(it))
            }
            list
        }
    }

    private fun mapToAppDto(doc: Document): AppDto {
        val hashMapTypeRef = object : TypeReference<HashMap<String, Any>>() {}
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