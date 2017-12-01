package io.zhudy.duic.service

import io.zhudy.duic.domain.App
import io.zhudy.duic.domain.PageResponse
import io.zhudy.duic.domain.SingleValue
import io.zhudy.duic.dto.AppDto
import io.zhudy.duic.repository.AppRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

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

    private fun mergeProps(a: MutableMap<String, Any>, b: MutableMap<String, Any>) {
        if (a.isEmpty()) {
            a.putAll(b)
            return
        }

        b.forEach { k, v ->
            if (v is Map<*, *>) {
                val m = hashMapOf<String, Any>()
                mergeProps(m, v.toMutableMap() as MutableMap<String, Any>)
                a.put(k, m)
            } else {
                a.put(k, v)
            }
        }
    }

}