package io.zhudy.duic.service

import com.memeyule.cryolite.core.BizCodeException
import io.zhudy.duic.BizCodes
import io.zhudy.duic.domain.App
import io.zhudy.duic.domain.PageResponse
import io.zhudy.duic.domain.SingleValue
import io.zhudy.duic.dto.AppDto
import io.zhudy.duic.dto.SpringCloudPropertySource
import io.zhudy.duic.dto.SpringCloudResponseDto
import io.zhudy.duic.repository.AppRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.lang.StringBuilder


/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Service
class AppService(val appRepository: AppRepository) {

    /**
     * @param app 应用实例
     */
    fun save(app: App) {
        appRepository.save(app)
    }

    /**
     * 更新配置内容.
     */
    fun updateContent(app: App) {
        appRepository.updateContent(app)
    }

    /**
     *
     */
    fun findPage(page: Pageable): PageResponse {
        return appRepository.findPage(page)
    }

    /**
     *
     */
    fun findOne(name: String, profile: String): AppDto {
        return appRepository.findOne(name, profile)
    }

    /**
     *
     */
    fun loadSpringCloudConfig(name: String, profiles: List<String>): SpringCloudResponseDto {
        val apps = arrayListOf<AppDto>()
        profiles.forEach {
            apps.add(appRepository.findOne(name, it))
        }

        val ps = arrayListOf<SpringCloudPropertySource>()
        val state = StringBuilder()
        apps.forEach {
            ps.add(SpringCloudPropertySource(name = "${it.name}_${it.profile}", source = flattenedMap(it.properties)))
            state.append(it.v)
        }
        return SpringCloudResponseDto(name = name, profiles = profiles, state = state.toString(), propertySources = ps)
    }

    /**
     *
     */
    fun loadConfigByNp(name: String, profiles: List<String>): Map<String, Any> {
        val apps = arrayListOf<AppDto>()
        profiles.forEach {
            apps.add(appRepository.findOne(name, it))
        }
        return mergeProps(apps)
    }

    /**
     *
     */
    fun loadConfigByNpAndKey(name: String, profiles: List<String>, key: String): Any? {
        var props = loadConfigByNp(name, profiles)
        var v: Any? = null
        for (k in key.split(".")) {
            v = props[k]
            if (v == null) {
                break
            }
            if (v is Map<*, *>) {
                props = v as Map<String, Any>
            }
        }

        if (v is Map<*, *>
                || v is Collection<*>
                || v is Array<*>
                ) {
            return v
        }
        return SingleValue(v)
    }

    private fun mergeProps(apps: List<AppDto>): Map<String, Any> {
        if (apps.size == 1) {
            return apps.first().properties
        }

        val m = hashMapOf<String, Any>()
        apps.forEach {
            mergeProps(m, it.properties.toMutableMap())
        }
        return m
    }

    private fun mergeProps(a: MutableMap<String, Any>, b: MutableMap<String, Any>, prefix: String = "") {
        if (a.isEmpty()) {
            a.putAll(b)
            return
        }

        b.forEach { k, v ->
            if (v is Map<*, *>) {
                val field = if (prefix.isEmpty()) k else "$prefix.$k"

                val o = a[k]
                if (o != null && o !is Map<*, *>) {
                    throw BizCodeException(BizCodes.C_1005, "[$field] 数据类型为 [${o.javaClass}] 不能与 [Object] 合并")
                }

                val m = (o as? Map<String, Any>)?.toMutableMap() ?: hashMapOf()
                mergeProps(m, v.toMutableMap() as MutableMap<String, Any>, field)
                a.put(k, m)
            } else {
                a.put(k, v)
            }
        }
    }

    private fun flattenedMap(source: Map<String, Any>): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        buildFlattenedMap(result, source, "")
        return result
    }

    private fun buildFlattenedMap(result: MutableMap<String, Any>, source: Map<String, Any>, path: String) {
        for (entry in source.entries) {
            var key = entry.key
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
                    val map = value as Map<String, Any>
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

}