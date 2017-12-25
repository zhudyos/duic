package io.zhudy.duic.service

import io.zhudy.duic.BizCode
import io.zhudy.duic.BizCodeException
import io.zhudy.duic.BizCodes
import io.zhudy.duic.UserContext
import io.zhudy.duic.domain.App
import io.zhudy.duic.domain.AppHistory
import io.zhudy.duic.domain.SingleValue
import io.zhudy.duic.dto.AppDto
import io.zhudy.duic.dto.SpringCloudPropertySource
import io.zhudy.duic.dto.SpringCloudResponseDto
import io.zhudy.duic.repository.AppRepository
import org.bson.types.ObjectId
import org.joda.time.DateTime
import org.springframework.cache.CacheManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.yaml.snakeyaml.Yaml
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Service
class AppService(val appRepository: AppRepository, cacheManager: CacheManager) {

    private val cache = cacheManager.getCache("apps")
    private var lastUpdatedAt: Date? = null
    private var lastAppHistoryCreatedAt: Date? = null
    private val yaml = Yaml()

    @Scheduled(initialDelay = 0, fixedDelayString = "\${app.watch.fixed_delay:5000}")
    protected fun watchApps() {
        // 更新 APP 配置信息
        if (lastUpdatedAt == null) {
            findAll()
        } else {
            findByUpdatedAt(lastUpdatedAt!!)
        }.toStream().forEach {
            cache.put(localKey(it.name, it.profile), it)
        }
        lastUpdatedAt = Date()

        // 清理已经删除的 APP
        if (lastAppHistoryCreatedAt != null) {
            appRepository.findAppHistoryByCreatedAt(lastAppHistoryCreatedAt!!).toStream().forEach {
                cache.evict(localKey(it.name, it.profile))
            }
        }
        lastAppHistoryCreatedAt = Date()
    }

    /**
     *
     */
    fun insert(app: App): Mono<App> {
        app.id = ObjectId().toHexString()
        app.createdAt = DateTime.now()
        app.updatedAt = DateTime.now()
        return appRepository.insert(app)
    }

    /**
     *
     */
    fun delete(app: App, userContext: UserContext) = checkPermission(app.name, app.profile, userContext).flatMap {
        appRepository.findOne(app.name, app.profile).flatMap {
            val ah = AppHistory(
                    id = it.id,
                    name = it.name,
                    profile = it.profile,
                    description = it.profile,
                    v = it.v,
                    createdAt = DateTime.now(),
                    deletedBy = userContext.email,
                    content = it.content,
                    users = it.users
            )
            appRepository.delete(app, ah)
        }
    }!!

    /**
     *
     */
    fun update(app: App, userContext: UserContext): Mono<Int> {
        return checkPermission(app.name, app.profile, userContext).flatMap {
            appRepository.update(app, userContext)
        }
    }

    /**
     *
     */
    fun updateContent(app: App, userContext: UserContext): Mono<Int> {
        try {
            yaml.load<Map<String, Any>>(app.content)
        } catch (e: Exception) {
            throw BizCodeException(BizCodes.C_1006, e)
        }

        return checkPermission(app.name, app.profile, userContext).flatMap {
            appRepository.updateContent(app, userContext)
        }
    }

    /**
     *
     */
    fun getConfigState(name: String, profiles: List<String>, configTokens: Array<String>): Mono<String> {
        return loadAndCheckApps(name, profiles, configTokens).map { apps ->
            val state = StringBuilder()
            apps.forEach {
                state.append(it.v)
            }
            state.toString()
        }
    }

    /**
     *
     */
    fun loadSpringCloudConfig(name: String, profiles: List<String>, configTokens: Array<String>): Mono<SpringCloudResponseDto> {
        return loadAndCheckApps(name, profiles, configTokens).map { apps ->
            val ps = arrayListOf<SpringCloudPropertySource>()
            val state = StringBuilder()
            apps.forEach {
                ps.add(SpringCloudPropertySource(localKey(it.name, it.profile), flattenedMap(it.properties)))
                state.append(it.v)
            }
            SpringCloudResponseDto(name = name, profiles = profiles, state = state.toString(), propertySources = ps)
        }
    }

    /**
     *
     */
    fun loadConfigByNameProfile(name: String, profiles: List<String>, configTokens: Array<String>): Mono<Map<Any, Any>> {
        return loadAndCheckApps(name, profiles, configTokens).map {
            mergeProps(it)
        }
    }

    /**
     *
     */
    fun loadConfigByNameProfileKey(name: String, profiles: List<String>, configTokens: Array<String>, key: String): Mono<Any> {
        return loadConfigByNameProfile(name, profiles, configTokens).map {
            var props = it
            var v: Any? = null
            for (k in key.split(".")) {
                v = props[k]
                if (v == null) {
                    break
                }
                if (v is Map<*, *>) {
                    props = v as Map<Any, Any>
                }
            }

            if (v is Map<*, *>
                    || v is Collection<*>
                    || v is Array<*>
                    ) {
                return@map v
            }
            SingleValue(v)
        }
    }

    /**
     *
     */
    fun loadOne(name: String, profile: String): Mono<AppDto> {
        val dto = cache.get(localKey(name, profile), { findOneDto(name, profile).block() })
                ?: throw BizCodeException(BizCodes.C_1000, "未找到应用 $name/$profile")
        return Mono.just(dto)
    }

    /**
     *
     */
    fun findOneDto(name: String, profile: String): Mono<AppDto> = appRepository.findOne(name, profile).map { mapToAppDto(it) }

    /**
     *
     */
    fun findOne(name: String, profile: String) = appRepository.findOne(name, profile)

    /**
     *
     */
    fun findAll(): Flux<AppDto> = appRepository.findAll().map { mapToAppDto(it) }

    /**
     *
     */
    fun findByUpdatedAt(updateAt: Date): Flux<AppDto> = appRepository.findByUpdatedAt(updateAt).map { mapToAppDto(it) }

    /**
     *
     */
    fun findPage(pageable: Pageable): Mono<Page<App>> = appRepository.findPage(pageable)

    /**
     *
     */
    fun findPageByUser(pageable: Pageable, userContext: UserContext): Mono<Page<App>> = if (userContext.isRoot) {
        findPage(pageable)
    } else {
        appRepository.findPageByUser(pageable, userContext)
    }

    /**
     *
     */
    fun findLast50History(name: String, profile: String, userContext: UserContext)
            = checkPermission(name, profile, userContext).flatMapMany {
        appRepository.findLast50History(name, profile)
    }!!

    private fun checkPermission(name: String, profile: String, userContext: UserContext): Mono<Unit> {
        if (userContext.isRoot) {
            return Mono.just(Unit)
        }

        return appRepository.findOne(name, profile).flatMap {
            if (!it.users.contains(userContext.email)) {
                // 用户没有修改该 APP 的权限
                throw BizCodeException(BizCode.Classic.C_403)
            }

            Mono.just(Unit)
        }
    }

    private fun loadAndCheckApps(name: String, profiles: List<String>, configTokens: Array<String>) = Flux.merge(profiles.map {
        loadOne(name, it).doOnNext {
            if (it.token.isNotEmpty() && !configTokens.contains(it.token)) {
                throw BizCodeException(BizCode.Classic.C_401, "${it.name}/${it.profile} 认证失败")
            }
        }
    }).collectList()

    private fun mergeProps(apps: List<AppDto>): Map<Any, Any> {
        if (apps.size == 1) {
            return apps.first().properties
        }

        val m = linkedMapOf<Any, Any>()
        apps.forEach {
            mergeProps(m, it.properties.toMutableMap())
        }
        return m
    }

    private fun mergeProps(a: MutableMap<Any, Any>, b: MutableMap<Any, Any>, prefix: String = "") {
        if (a.isEmpty()) {
            a.putAll(b)
            return
        }

        b.forEach { k, v ->
            if (v is Map<*, *>) {
                val field: String = if (prefix.isEmpty()) k.toString() else "$prefix.$k"

                val o = a[k]
                if (o != null && o !is Map<*, *>) {
                    throw BizCodeException(BizCodes.C_1005, "[$field] 数据类型为 [${o.javaClass}] 不能与 [Object] 合并")
                }

                val m = (o as? Map<Any, Any>)?.toMutableMap() ?: hashMapOf()
                mergeProps(m, v.toMutableMap() as MutableMap<Any, Any>, field)
                a.put(k, m)
            } else {
                a.put(k, v)
            }
        }
    }

    private fun flattenedMap(source: Map<Any, Any>): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        buildFlattenedMap(result, source, "")
        return result
    }

    private fun buildFlattenedMap(result: MutableMap<String, Any>, source: Map<Any, Any>, path: String) {
        for (entry in source.entries) {
            var key: String = entry.key as? String ?: entry.key.toString()

            if (path.isNotEmpty()) {
                key = if (key.startsWith("[")) {
                    path + key
                } else {
                    path + '.' + key
                }
            }

            val value = entry.value
            when (value) {
                is String -> result.put(key, value)
                is Map<*, *> -> {
                    // Need a compound key
                    val map = value as Map<Any, Any>
                    buildFlattenedMap(result, map, key)
                }
                is Collection<*> -> {
                    // Need a compound key
                    val collection = value as Collection<Any>
                    for ((count, o) in collection.withIndex()) {
                        buildFlattenedMap(result, mapOf("[$count]" to o), key)
                    }
                }
                else -> result.put(key, value)
            }
        }
    }

    private fun mapToAppDto(app: App): AppDto {
        val content = app.content
        val properties = if (content.isNotEmpty()) {
            yaml.load<Map<Any, Any>>(content)
        } else {
            emptyMap()
        }

        return AppDto(
                name = app.name,
                profile = app.profile,
                token = app.token,
                v = app.v,
                createdAt = app.createdAt!!,
                updatedAt = app.updatedAt!!,
                content = content,
                properties = properties
        )
    }

    private fun localKey(name: String, profile: String) = "${name}_$profile"
}